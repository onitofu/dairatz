package ru.nyansus.mc.hollow;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;
import ru.nyansus.mc.Dairatz;
import ru.nyansus.mc.loot.FairyLootSources;
import ru.nyansus.mc.entity.PaleDetection;
import ru.nyansus.mc.entity.HousedFairyEntity;
import ru.nyansus.mc.entity.FairyPlacement;

public final class FairyRecoveryJournal extends SavedData {
    public static final Codec<FairyRecoveryJournal> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        PendingFairyRecovery.CODEC.listOf().optionalFieldOf("Recoveries", List.of())
            .forGetter(FairyRecoveryJournal::recoverySnapshots),
        PendingHomeRelease.CODEC.listOf().optionalFieldOf("HomeReleases", List.of())
            .forGetter(FairyRecoveryJournal::homeReleases)
    ).apply(instance, FairyRecoveryJournal::new));
    public static final SavedDataType<FairyRecoveryJournal> TYPE = new SavedDataType<>(
        "dairatz_fairy_recovery", FairyRecoveryJournal::new, CODEC, null
    );

    private final Map<UUID, PendingFairyRecovery> recoveries = new LinkedHashMap<>();
    private final Map<GlobalPos, Set<UUID>> releases = new HashMap<>();
    private final ArrayDeque<UUID> work = new ArrayDeque<>();
    private final Map<UUID, Long> nextAttempt = new HashMap<>();
    private final Set<UUID> reported = new HashSet<>();

    public FairyRecoveryJournal() {
    }

    private FairyRecoveryJournal(List<PendingFairyRecovery> recoveries, List<PendingHomeRelease> releases) {
        for (PendingFairyRecovery recovery : recoveries) {
            this.recoveries.putIfAbsent(recovery.resident(), recovery);
        }
        for (PendingHomeRelease release : releases) {
            this.releases.computeIfAbsent(release.home(), key -> new HashSet<>()).add(release.resident());
        }
        work.addAll(this.recoveries.keySet());
    }

    public static FairyRecoveryJournal get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public List<PendingFairyRecovery> recoverySnapshots() {
        return List.copyOf(recoveries.values());
    }

    public List<PendingHomeRelease> homeReleases() {
        return releases.entrySet().stream().flatMap(entry -> entry.getValue().stream()
                .map(id -> new PendingHomeRelease(entry.getKey(), id))).toList();
    }

    public boolean hasRecovery(UUID id) {
        return recoveries.containsKey(id);
    }

    public void releaseHome(MinecraftServer server, GlobalPos home, UUID resident) {
        ServerLevel target = server.getLevel(home.dimension());
        if (target != null && target.hasChunkAt(home.pos())) {
            if (target.getBlockEntity(home.pos()) instanceof FairyHollowBlockEntity hollow) {
                hollow.forgetResident(resident);
            }
            return;
        }
        if (releases.computeIfAbsent(home, key -> new HashSet<>()).add(resident)) {
            setDirty();
        }
    }

    public void applyHomeReleases(GlobalPos home, FairyHollowBlockEntity hollow) {
        Set<UUID> residents = releases.remove(home);
        if (residents != null) {
            if (residents.contains(hollow.residentId())) {
                hollow.forgetResident(hollow.residentId());
            }
            setDirty();
        }
    }

    public void enqueue(UUID id, GlobalPos origin, CompoundTag snapshot) {
        if (!recoveries.containsKey(id)) {
            recoveries.put(id, new PendingFairyRecovery(id, origin, snapshot));
            work.addLast(id);
            setDirty();
        }
    }

    public FairyReleaseResult recoverNow(
        MinecraftServer server, UUID id, BiPredicate<ServerLevel, HousedFairyEntity> spawn
    ) {
        PendingFairyRecovery recovery = recoveries.get(id);
        if (recovery == null) {
            return FairyReleaseResult.refused(FairyReleaseStatus.EMPTY);
        }
        ServerLevel level = server.getLevel(recovery.origin().dimension());
        if (level == null || !level.hasChunkAt(recovery.origin().pos())) {
            return FairyReleaseResult.refused(FairyReleaseStatus.BLOCKED);
        }
        for (ServerLevel loaded : server.getAllLevels()) {
            Entity existing = loaded.getEntity(id);
            if (existing instanceof HousedFairyEntity fairy) {
                if (recovery.origin().equals(fairy.home())) {
                    fairy.releaseHome();
                }
                complete(id);
                return FairyReleaseResult.released(fairy);
            }
            if (existing != null) {
                return failed(id);
            }
        }
        Entity restored;
        try {
            restored = EntityType.loadEntityRecursive(
                recovery.snapshot(), level, EntitySpawnReason.LOAD, value -> value
            );
        } catch (RuntimeException error) {
            return failed(id);
        }
        if (!(restored instanceof HousedFairyEntity fairy) || !fairy.getUUID().equals(id)) {
            return failed(id);
        }
        BlockPos center = recovery.origin().pos();
        boolean positioned = false;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, 0, -2), center.offset(2, 2, 2))) {
            Vec3 point = Vec3.atBottomCenterOf(pos);
            if (FairyPlacement.isSafe(level, fairy, point)) {
                fairy.snapTo(point.x, point.y, point.z);
                positioned = true;
                break;
            }
        }
        if (!positioned) {
            return FairyReleaseResult.refused(FairyReleaseStatus.BLOCKED);
        }
        fairy.releaseHome();
        fairy.stayOutside();
        try {
            if (!spawn.test(level, fairy)) {
                return failed(id);
            }
        } catch (RuntimeException error) {
            return failed(id);
        }
        complete(id);
        return FairyReleaseResult.released(fairy);
    }

    private FairyReleaseResult failed(UUID id) {
        if (reported.add(id)) {
            Dairatz.LOGGER.warn("Fairy recovery remains pending for {}", id);
        }
        return FairyReleaseResult.refused(FairyReleaseStatus.FAILED);
    }

    private void complete(UUID id) {
        recoveries.remove(id);
        nextAttempt.remove(id);
        reported.remove(id);
        setDirty();
    }

    public static void tick(MinecraftServer server) {
        get(server).retry(server, ServerLevel::addFreshEntity);
        FairyLootSources.clear();
        PaleDetection.tick(server);
    }

    public void retry(MinecraftServer server, BiPredicate<ServerLevel, HousedFairyEntity> spawn) {
        long now = server.overworld().getGameTime();
        int attempts = 0;
        int inspected = Math.min(32, work.size());
        for (int i = 0; i < inspected && attempts < 4; i++) {
            UUID id = work.removeFirst();
            PendingFairyRecovery recovery = recoveries.get(id);
            if (recovery == null) {
                continue;
            }
            work.addLast(id);
            if (now < nextAttempt.getOrDefault(id, 0L)) {
                continue;
            }
            ServerLevel level = server.getLevel(recovery.origin().dimension());
            if (level == null || !level.hasChunkAt(recovery.origin().pos())) {
                continue;
            }
            if (level.getBlockEntity(recovery.origin().pos()) instanceof FairyHollowBlockEntity hollow
                    && hollow.isInside() && hollow.isResident(id)) {
                complete(id);
                continue;
            }
            nextAttempt.put(id, now + 100);
            recoverNow(server, id, spawn);
            attempts++;
        }
    }
}
