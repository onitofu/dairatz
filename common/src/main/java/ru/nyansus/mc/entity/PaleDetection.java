package ru.nyansus.mc.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import ru.nyansus.mc.network.FairyNetwork;
import ru.nyansus.mc.network.PaleDetectionPayload;
import ru.nyansus.mc.network.PaleTarget;

public final class PaleDetection {
    private PaleDetection() {
    }

    public static void tick(MinecraftServer server) {
        if (server.overworld().getGameTime() % 20 != 0) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            FairyNetwork.send(player, snapshotFor(player));
        }
    }

    public static PaleDetectionPayload snapshotFor(ServerPlayer player) {
        ServerLevel level = player.level();
        List<LivingEntity> targets = new ArrayList<>();
        Set<UUID> seen = new HashSet<>();
        for (AbstractFairyEntity fairy : level.getEntitiesOfClass(AbstractFairyEntity.class,
                player.getBoundingBox().inflate(18), f -> f.kind() == FairyKind.PALE && f.isOwnedBy(player))) {
            Vec3 center;
            int radius;
            if (fairy.isOnHead()) {
                center = player.position();
                radius = 8;
            } else if (fairy instanceof HousedFairyEntity housed && housed.hasHome() && !housed.wantsShelter()
                    && player.distanceToSqr(Vec3.atCenterOf(housed.home().pos())) <= 256) {
                center = Vec3.atCenterOf(housed.home().pos());
                radius = 12;
            } else {
                continue;
            }
            List<LivingEntity> found = level.getEntitiesOfClass(LivingEntity.class,
                    fairy.getBoundingBox().move(center.subtract(fairy.position())).inflate(radius),
                    target -> target.isAlive() && target.position().distanceToSqr(center) <= radius * radius
                            && threat(target, player, fairy));
            for (LivingEntity target : found) {
                if (seen.add(target.getUUID())) {
                    targets.add(target);
                }
            }
            if (!fairy.isOnHead() && fairy instanceof NewFairyEntity pale
                    && pale.noticeThreats(found.stream().limit(16).map(LivingEntity::getUUID)
                        .collect(Collectors.toSet()), FairyAbilities.time(level))) {
                level.playSound(null, fairy.blockPosition(), SoundEvents.ALLAY_AMBIENT_WITH_ITEM,
                        SoundSource.NEUTRAL, 0.3f, 0.7f);
            }
        }
        List<PaleTarget> snapshot = targets.stream().sorted(Comparator.comparingDouble(player::distanceToSqr))
                .limit(16).map(target -> new PaleTarget(target.getId(), target.getUUID())).toList();
        return new PaleDetectionPayload(level.dimension().identifier(), snapshot);
    }

    private static boolean threat(LivingEntity target, ServerPlayer owner, AbstractFairyEntity fairy) {
        if (!(target instanceof Enemy)) {
            return false;
        }
        return !(target instanceof NeutralMob neutral) || neutral.isAngryAt(owner, owner.level())
                || neutral.isAngryAt(fairy, owner.level());
    }
}
