package ru.nyansus.mc.entity.ai;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import ru.nyansus.mc.entity.FairyKind;
import net.minecraft.world.level.block.state.BlockState;
import ru.nyansus.mc.entity.HousedFairyEntity;

public final class FairyPollinateGoal extends Goal {
    private static final int POLLINATE_DURATION_TICKS = 60;
    private static final int CROP_DURATION_TICKS = 20;
    private static final int POLLINATE_COOLDOWN_TICKS = 200;
    private static final int SEARCH_COOLDOWN_TICKS = 40;
    private static final int TRAVEL_TIMEOUT_TICKS = 200;
    private static final int PARTICLE_INTERVAL_TICKS = 10;
    private static final double ARRIVAL_DISTANCE_SQ = 2.5;
    private static final int SEARCH_RANGE_XZ = 8;
    private static final int SEARCH_RANGE_Y = 3;
    private static final double BLOCK_CENTER_OFFSET = 0.5;
    private static final double HOVER_HEIGHT = 1.0;

    private final HousedFairyEntity fairy;
    private BlockPos targetFlower;
    private BlockPos targetCrop;
    private boolean carryingPollen;
    private int workingTicks;
    private int travelTicks;

    public FairyPollinateGoal(HousedFairyEntity fairy) {
        this.fairy = fairy;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        if (!fairy.canPollinate() || fairy.level().getGameTime() < fairy.nextPollinationTick()) {
            return false;
        }
        targetFlower = findTarget(this::isFlower);
        targetCrop = targetFlower == null ? null : findTarget(this::isGrowingCrop);
        if (targetCrop == null) {
            fairy.delayPollination(SEARCH_COOLDOWN_TICKS);
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        carryingPollen = false;
        workingTicks = 0;
        travelTicks = 0;
    }

    @Override
    public void tick() {
        BlockPos target = carryingPollen ? targetCrop : targetFlower;
        if (target == null) {
            return;
        }
        if (++travelTicks > TRAVEL_TIMEOUT_TICKS) {
            targetCrop = null;
            fairy.delayPollination(SEARCH_COOLDOWN_TICKS);
            return;
        }
        double x = target.getX() + BLOCK_CENTER_OFFSET;
        double y = target.getY() + HOVER_HEIGHT;
        double z = target.getZ() + BLOCK_CENTER_OFFSET;
        fairy.getNavigation().moveTo(x, y, z, 1.0);
        fairy.getLookControl().setLookAt(x, target.getY() + BLOCK_CENTER_OFFSET, z);
        if (carryingPollen && travelTicks % PARTICLE_INTERVAL_TICKS == 0) {
            showPollen();
        }
        if (fairy.distanceToSqr(x, y, z) >= ARRIVAL_DISTANCE_SQ) {
            workingTicks = 0;
            return;
        }
        workingTicks++;
        if (!carryingPollen && workingTicks % PARTICLE_INTERVAL_TICKS == 0) {
            showPollen();
        }
        if (!carryingPollen && workingTicks >= POLLINATE_DURATION_TICKS) {
            carryingPollen = true;
            workingTicks = 0;
            travelTicks = 0;
        } else if (carryingPollen && workingTicks >= CROP_DURATION_TICKS) {
            growCrop();
            targetCrop = null;
            fairy.delayPollination(POLLINATE_COOLDOWN_TICKS);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return targetFlower != null && targetCrop != null && fairy.canPollinate()
                && isAvailable(targetFlower) && isFlower(targetFlower)
                && isAvailable(targetCrop) && isGrowingCrop(targetCrop);
    }

    @Override
    public void stop() {
        fairy.getNavigation().stop();
        targetFlower = null;
        targetCrop = null;
        carryingPollen = false;
    }

    private BlockPos findTarget(Predicate<BlockPos> suitable) {
        BlockPos center = fairy.hasHome() ? fairy.home().pos() : fairy.blockPosition();
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-SEARCH_RANGE_XZ, -SEARCH_RANGE_Y, -SEARCH_RANGE_XZ),
                center.offset(SEARCH_RANGE_XZ, SEARCH_RANGE_Y, SEARCH_RANGE_XZ)
        )) {
            if (!isAvailable(pos) || !fairy.level().getBlockState(pos.above()).isAir() || !suitable.test(pos)) {
                continue;
            }
            double distance = fairy.distanceToSqr(
                    pos.getX() + BLOCK_CENTER_OFFSET,
                    pos.getY() + HOVER_HEIGHT,
                    pos.getZ() + BLOCK_CENTER_OFFSET
            );
            if (distance < nearestDistance) {
                nearest = pos.immutable();
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private boolean isAvailable(BlockPos pos) {
        return fairy.isWithinGarden(pos) && fairy.level().hasChunkAt(pos);
    }

    private boolean isFlower(BlockPos pos) {
        return fairy.level().getBlockState(pos).is(BlockTags.FLOWERS);
    }

    private boolean isGrowingCrop(BlockPos pos) {
        BlockState state = fairy.level().getBlockState(pos);
        boolean crop = fairy.kind() == FairyKind.JUNGLE
                ? state.is(Blocks.COCOA) || state.is(Blocks.SWEET_BERRY_BUSH) : state.is(BlockTags.CROPS);
        return crop && state.getBlock() instanceof BonemealableBlock growable
                && growable.isValidBonemealTarget(fairy.level(), pos, state);
    }

    private void showPollen() {
        if (fairy.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.WAX_ON,
                    fairy.getX(), fairy.getY() + 0.25, fairy.getZ(), 3, 0.2, 0.15, 0.2, 0.01);
        }
    }

    private void growCrop() {
        if (!(fairy.level() instanceof ServerLevel level) || !isAvailable(targetCrop)
                || !isGrowingCrop(targetCrop)) {
            return;
        }
        BlockState before = level.getBlockState(targetCrop);
        BonemealableBlock growable = (BonemealableBlock) before.getBlock();
        if (!growable.isBonemealSuccess(level, fairy.getRandom(), targetCrop, before)) {
            return;
        }
        growable.performBonemeal(level, fairy.getRandom(), targetCrop, before);
        if (!level.getBlockState(targetCrop).equals(before)) {
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    targetCrop.getX() + BLOCK_CENTER_OFFSET,
                    targetCrop.getY() + BLOCK_CENTER_OFFSET,
                    targetCrop.getZ() + BLOCK_CENTER_OFFSET,
                    12, 0.35, 0.3, 0.35, 0.02);
        }
    }
}
