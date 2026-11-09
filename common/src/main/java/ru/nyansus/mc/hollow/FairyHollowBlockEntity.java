package ru.nyansus.mc.hollow;

import java.util.UUID;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import ru.nyansus.mc.Dairatz;
import ru.nyansus.mc.entity.AbstractFairyEntity;
import ru.nyansus.mc.entity.DairatzEntity;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.ModHollows;

public final class FairyHollowBlockEntity extends BlockEntity {
    private HollowResident occupancy = HollowResident.empty();
    private boolean releaseFailureReported;
    private final BiPredicate<ServerLevel, DairatzEntity> spawn;

    public FairyHollowBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, ServerLevel::addFreshEntity);
    }

    FairyHollowBlockEntity(BlockPos pos, BlockState state, BiPredicate<ServerLevel, DairatzEntity> spawn) {
        super(ModHollows.hollowType(), pos, state);
        this.spawn = spawn;
    }

    public UUID residentId() {
        return occupancy.id();
    }

    public boolean isResident(UUID id) {
        return id != null && id.equals(occupancy.id());
    }

    public boolean hasResident() {
        return occupancy.status() != HollowStatus.EMPTY;
    }

    public boolean isInside() {
        return occupancy.status() == HollowStatus.INSIDE;
    }

    public Vec3 entrance() {
        return Vec3.atCenterOf(worldPosition.relative(getBlockState().getValue(FairyHollowBlock.FACING)));
    }

    public boolean isEntranceClear() {
        Vec3 point = entrance();
        BlockPos pos = BlockPos.containing(point);
        return level != null && level.hasChunkAt(pos)
                && level.noCollision(new AABB(point.x - 0.25, point.y - 0.25, point.z - 0.25,
                        point.x + 0.25, point.y + 0.25, point.z + 0.25))
                && level.getFluidState(pos).isEmpty();
    }

    public void prepareNaturalResident() {
        if (!hasResident()) {
            occupancy = HollowResident.naturalPending();
            setChanged();
        }
    }

    public void forgetResident(UUID id) {
        if (isResident(id) && !isInside()) {
            occupancy = HollowResident.empty();
            setChanged();
        }
    }

    public boolean enter(DairatzEntity fairy) {
        if (!(level instanceof ServerLevel) || isInside() || occupancy.status() == HollowStatus.NATURAL_PENDING
                || fairy.isRemoved() || !fairy.isAlive() || fairy.isPassenger() || fairy.level() != level
                || (fairy.hasHome() && (!fairy.home().dimension().equals(level.dimension())
                        || !fairy.home().pos().equals(worldPosition)))
                || (occupancy.id() != null && !isResident(fairy.getUUID())) || !isEntranceClear()
                || fairy.position().distanceToSqr(entrance()) > 9.0) {
            return false;
        }
        fairy.assignHome(worldPosition);
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        if (!fairy.save(output)) {
            return false;
        }
        occupancy = HollowResident.inside(fairy.getUUID(), output.buildResult());
        releaseFailureReported = false;
        fairy.discard();
        updateOccupied();
        return true;
    }

    public FairyReleaseResult letResidentOut() {
        if (!(level instanceof ServerLevel server) || !isInside()) {
            return FairyReleaseResult.refused(FairyReleaseStatus.EMPTY);
        }
        if (!isEntranceClear()) {
            return FairyReleaseResult.refused(FairyReleaseStatus.BLOCKED);
        }
        if (server.getEntity(occupancy.id()) != null) {
            return failedRelease();
        }
        Entity entity;
        try {
            entity = EntityType.loadEntityRecursive(
                occupancy.snapshot(), server, EntitySpawnReason.LOAD, value -> value
            );
        } catch (RuntimeException error) {
            return failedRelease();
        }
        if (!(entity instanceof DairatzEntity fairy) || !fairy.getUUID().equals(occupancy.id())) {
            return failedRelease();
        }
        Vec3 point = entrance();
        fairy.snapTo(point.x, point.y - 0.25, point.z);
        fairy.stayOutside();
        try {
            if (!spawn.test(server, fairy)) {
                return failedRelease();
            }
        } catch (RuntimeException error) {
            return failedRelease();
        }
        releaseFailureReported = false;
        occupancy = HollowResident.outside(fairy.getUUID());
        updateOccupied();
        return FairyReleaseResult.released(fairy);
    }

    private FairyReleaseResult failedRelease() {
        if (!releaseFailureReported) {
            Dairatz.LOGGER.warn("Fairy release refused for resident {}", occupancy.id());
            releaseFailureReported = true;
        }
        return FairyReleaseResult.refused(FairyReleaseStatus.FAILED);
    }

    private FairyReleaseResult recallStoredResident() {
        FairyReleaseResult result = letResidentOut();
        if (result.succeeded()) {
            result.fairy().releaseHome();
        }
        return result;
    }

    public FairyReleaseResult releaseOnDestruction() {
        if (!(level instanceof ServerLevel server)) {
            return FairyReleaseResult.refused(FairyReleaseStatus.EMPTY);
        }
        initializeNaturalResident(server);
        if (!isInside()) {
            if (occupancy.id() != null && server.getEntity(occupancy.id()) instanceof DairatzEntity fairy) {
                fairy.releaseHome();
            }
            occupancy = HollowResident.empty();
            return FairyReleaseResult.refused(FairyReleaseStatus.EMPTY);
        }
        FairyRecoveryJournal journal = FairyRecoveryJournal.get(server.getServer());
        UUID id = occupancy.id();
        journal.enqueue(id, GlobalPos.of(server.dimension(), worldPosition), occupancy.snapshot());
        FairyReleaseResult result = journal.recoverNow(server.getServer(), id, spawn);
        occupancy = HollowResident.empty();
        setChanged();
        return result;
    }

    public void interact(Player player) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        FairyRecoveryJournal.get(server.getServer()).applyHomeReleases(
            GlobalPos.of(server.dimension(), worldPosition), this
        );
        initializeNaturalResident(server);
        if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
            recall(player, server);
            return;
        }
        if (!hasResident()) {
            AbstractFairyEntity passenger = headFairy(player);
            if (passenger instanceof DairatzEntity fairy) {
                if (!isEntranceClear()) {
                    message(player, "blocked");
                    return;
                }
                Vec3 previous = fairy.position();
                fairy.setPos(entrance());
                if (!enter(fairy)) {
                    fairy.setPos(previous);
                    message(player, "unavailable");
                } else {
                    message(player, "settled");
                }
            } else {
                message(player, passenger == null ? "empty" : "wrong_fairy");
            }
            return;
        }
        if (!player.getMainHandItem().isEmpty()) {
            return;
        }
        if (isInside()) {
            FairyReleaseResult result = letResidentOut();
            message(player, result.succeeded() ? "knocked"
                    : result.status() == FairyReleaseStatus.BLOCKED ? "blocked" : "unavailable");
        } else {
            message(player, "away");
        }
    }

    private void recall(Player player, ServerLevel server) {
        if (headFairy(player) != null) {
            message(player, "head_full");
            return;
        }
        DairatzEntity fairy;
        if (isInside()) {
            Entity preview = EntityType.loadEntityRecursive(
                    occupancy.snapshot(), server, EntitySpawnReason.LOAD, value -> value);
            if (!(preview instanceof DairatzEntity candidate) || !candidate.isOwnedBy(player)) {
                message(player, "not_owner");
                return;
            }
            fairy = recallStoredResident().fairy();
        } else {
            Entity entity = occupancy.id() == null ? null : server.getEntity(occupancy.id());
            if (!(entity instanceof DairatzEntity candidate)
                    || candidate.distanceToSqr(Vec3.atCenterOf(worldPosition)) > 256.0) {
                message(player, "unavailable");
                return;
            }
            if (!candidate.isOwnedBy(player)) {
                message(player, "not_owner");
                return;
            }
            fairy = candidate;
            fairy.releaseHome();
        }
        if (fairy == null) {
            message(player, "blocked");
            return;
        }
        fairy.setOnHead(true);
        fairy.setPos(player.getX(), player.getEyeY() + 0.15, player.getZ());
        fairy.getNavigation().stop();
        message(player, "recalled");
    }

    private AbstractFairyEntity headFairy(Player player) {
        return level.getEntitiesOfClass(AbstractFairyEntity.class, player.getBoundingBox().inflate(2),
                fairy -> fairy.isOnHead() && fairy.isOwnedBy(player)).stream().findFirst().orElse(null);
    }

    private static void message(Player player, String key) {
        player.displayClientMessage(Component.translatable("message.dairatz.hollow." + key), true);
    }

    private void initializeNaturalResident(ServerLevel server) {
        if (occupancy.status() != HollowStatus.NATURAL_PENDING) {
            return;
        }
        DairatzEntity fairy = ModEntities.dairatzEntity().get().create(server, EntitySpawnReason.NATURAL);
        if (fairy == null) {
            return;
        }
        fairy.assignHome(worldPosition);
        fairy.setPos(Vec3.atCenterOf(worldPosition));
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, server.registryAccess());
        if (fairy.save(output)) {
            occupancy = HollowResident.inside(fairy.getUUID(), output.buildResult());
            updateOccupied();
        }
    }

    private void updateOccupied() {
        setChanged();
        if (level != null && !isRemoved()) {
            BlockState current = level.getBlockState(worldPosition);
            if (current.is(ModHollows.oakHollow())) {
                level.setBlock(worldPosition, current.setValue(FairyHollowBlock.OCCUPIED, isInside()), 3);
            }
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FairyHollowBlockEntity hollow) {
        if (level.getGameTime() % 20 != 0 || !(level instanceof ServerLevel server)) {
            return;
        }
        hollow.initializeNaturalResident(server);
        FairyRecoveryJournal.get(server.getServer()).applyHomeReleases(
            GlobalPos.of(server.dimension(), pos), hollow
        );
        if (hollow.isInside() && !FairyActivitySchedule.isRestTime(level)) {
            hollow.letResidentOut();
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        releaseOnDestruction();
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (occupancy.id() != null) {
            output.store("Resident", UUIDUtil.CODEC, occupancy.id());
        }
        if (isInside()) {
            output.store("StoredFairy", CompoundTag.CODEC, occupancy.snapshot());
        }
        output.putBoolean("PendingNaturalResident", occupancy.status() == HollowStatus.NATURAL_PENDING);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        occupancy = HollowResident.restore(
            input.read("Resident", UUIDUtil.CODEC).orElse(null),
            input.read("StoredFairy", CompoundTag.CODEC).orElse(null),
            input.getBooleanOr("PendingNaturalResident", false)
        );
    }
}
