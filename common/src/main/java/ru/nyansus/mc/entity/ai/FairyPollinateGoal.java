package ru.nyansus.mc.entity.ai;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import ru.nyansus.mc.entity.DairatzEntity;

public final class FairyPollinateGoal extends Goal {
    private static final int POLLINATE_DURATION_TICKS = 60;
    private static final int POLLINATE_COOLDOWN_TICKS = 200;
    private static final double ARRIVAL_DISTANCE_SQ = 2.5;
    private static final int FLOWER_SEARCH_ATTEMPTS = 10;
    private static final int FLOWER_SEARCH_RANGE_XZ = 8;
    private static final int FLOWER_SEARCH_RANGE_Y = 3;
    private static final int GROW_RANGE_XZ = 3;
    private static final int GROW_RANGE_Y = 2;
    private static final double BLOCK_CENTER_OFFSET = 0.5;
    private static final double HOVER_HEIGHT = 1.0;

    private BlockPos targetFlower;
    private int pollinatingTicks;
    private int travelTicks;

    private final DairatzEntity fairy;

    public FairyPollinateGoal(DairatzEntity fairy) {
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
        targetFlower = findFlower();
        return targetFlower != null;
    }

    @Override
    public void start() {
        pollinatingTicks = 0;
        travelTicks = 0;
    }

    @Override
    public void tick() {
        if (targetFlower == null) {
            return;
        }
        if (++travelTicks > 200) {
            targetFlower = null;
            fairy.delayPollination(20);
            return;
        }
        fairy.getNavigation().moveTo(
                targetFlower.getX() + BLOCK_CENTER_OFFSET,
                targetFlower.getY() + HOVER_HEIGHT,
                targetFlower.getZ() + BLOCK_CENTER_OFFSET,
                1.0
        );
        double dist = fairy.distanceToSqr(
                targetFlower.getX() + BLOCK_CENTER_OFFSET,
                targetFlower.getY() + HOVER_HEIGHT,
                targetFlower.getZ() + BLOCK_CENTER_OFFSET
        );
        if (dist < ARRIVAL_DISTANCE_SQ) {
            pollinatingTicks++;
            if (pollinatingTicks >= POLLINATE_DURATION_TICKS) {
                growNearby();
                fairy.delayPollination(POLLINATE_COOLDOWN_TICKS);
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return targetFlower != null
                && pollinatingTicks < POLLINATE_DURATION_TICKS && fairy.canPollinate()
                && fairy.level().hasChunkAt(targetFlower)
                && fairy.level().getBlockState(targetFlower).is(BlockTags.FLOWERS);
    }

    private BlockPos findFlower() {
        BlockPos fairyPos = fairy.hasHome() ? fairy.home().pos() : fairy.blockPosition();
        for (int i = 0; i < FLOWER_SEARCH_ATTEMPTS; i++) {
            BlockPos pos = fairyPos.offset(
                    fairy.getRandom().nextIntBetweenInclusive(
                            -FLOWER_SEARCH_RANGE_XZ, FLOWER_SEARCH_RANGE_XZ),
                    fairy.getRandom().nextIntBetweenInclusive(
                            -FLOWER_SEARCH_RANGE_Y, FLOWER_SEARCH_RANGE_Y),
                    fairy.getRandom().nextIntBetweenInclusive(
                            -FLOWER_SEARCH_RANGE_XZ, FLOWER_SEARCH_RANGE_XZ)
            );
            if (fairy.isWithinGarden(pos) && fairy.level().hasChunkAt(pos)
                    && fairy.level().getBlockState(pos).is(BlockTags.FLOWERS)
                    && fairy.level().getBlockState(pos.above()).isAir()) {
                return pos;
            }
        }
        return null;
    }

    private void growNearby() {
        Level world = fairy.level();
        if (world.isClientSide()) {
            return;
        }
        BlockPos center = fairy.blockPosition();
        for (int dx = -GROW_RANGE_XZ; dx <= GROW_RANGE_XZ; dx++) {
            for (int dy = -GROW_RANGE_Y; dy <= GROW_RANGE_Y; dy++) {
                for (int dz = -GROW_RANGE_XZ; dz <= GROW_RANGE_XZ; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!fairy.isWithinGarden(pos) || !world.hasChunkAt(pos)) {
                        continue;
                    }
                    BlockState state = world.getBlockState(pos);
                    if (state.getBlock() instanceof BonemealableBlock growable) {
                        if (growable.isValidBonemealTarget(world, pos, state)) {
                            growable.performBonemeal(
                                    (ServerLevel) world, fairy.getRandom(), pos, state);
                            return;
                        }
                    }
                }
            }
        }
    }
}
