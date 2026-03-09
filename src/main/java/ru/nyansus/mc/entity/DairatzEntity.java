package ru.nyansus.mc.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import ru.nyansus.mc.config.DairatzConfig;

public class DairatzEntity extends AbstractFairyEntity {
    public DairatzEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected Item getTameItem() {
        return Items.POPPY;
    }

    @Override
    protected int getFireRate() {
        return DairatzConfig.fireRate;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(4, new PollinateGoal());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createFairyAttributes(DairatzConfig.fairyHealth, DairatzConfig.flySpeed);
    }

    @Override
    protected ThrowableItemProjectile createProjectile() {
        return new FurballEntity(level(), this);
    }

    private class PollinateGoal extends Goal {
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
        private int cooldown;

        @Override
        public boolean canUse() {
            if (isTame() || isOnHead()) {
                return false;
            }
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            targetFlower = findFlower();
            return targetFlower != null;
        }

        @Override
        public void start() {
            pollinatingTicks = 0;
        }

        @Override
        public void tick() {
            if (targetFlower == null) {
                return;
            }
            getNavigation().moveTo(
                    targetFlower.getX() + BLOCK_CENTER_OFFSET,
                    targetFlower.getY() + HOVER_HEIGHT,
                    targetFlower.getZ() + BLOCK_CENTER_OFFSET,
                    1.0
            );
            double dist = distanceToSqr(
                    targetFlower.getX() + BLOCK_CENTER_OFFSET,
                    targetFlower.getY() + HOVER_HEIGHT,
                    targetFlower.getZ() + BLOCK_CENTER_OFFSET
            );
            if (dist < ARRIVAL_DISTANCE_SQ) {
                pollinatingTicks++;
                if (pollinatingTicks >= POLLINATE_DURATION_TICKS) {
                    growNearby();
                    cooldown = POLLINATE_COOLDOWN_TICKS;
                    stop();
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return targetFlower != null
                    && pollinatingTicks < POLLINATE_DURATION_TICKS && !isTame();
        }

        private BlockPos findFlower() {
            BlockPos fairyPos = blockPosition();
            for (int i = 0; i < FLOWER_SEARCH_ATTEMPTS; i++) {
                BlockPos pos = fairyPos.offset(
                        random.nextIntBetweenInclusive(
                                -FLOWER_SEARCH_RANGE_XZ, FLOWER_SEARCH_RANGE_XZ),
                        random.nextIntBetweenInclusive(
                                -FLOWER_SEARCH_RANGE_Y, FLOWER_SEARCH_RANGE_Y),
                        random.nextIntBetweenInclusive(
                                -FLOWER_SEARCH_RANGE_XZ, FLOWER_SEARCH_RANGE_XZ)
                );
                if (level().getBlockState(pos).is(BlockTags.FLOWERS)) {
                    return pos;
                }
            }
            return null;
        }

        private void growNearby() {
            Level world = level();
            if (world.isClientSide()) {
                return;
            }
            BlockPos center = blockPosition();
            for (int dx = -GROW_RANGE_XZ; dx <= GROW_RANGE_XZ; dx++) {
                for (int dy = -GROW_RANGE_Y; dy <= GROW_RANGE_Y; dy++) {
                    for (int dz = -GROW_RANGE_XZ; dz <= GROW_RANGE_XZ; dz++) {
                        BlockPos pos = center.offset(dx, dy, dz);
                        BlockState state = world.getBlockState(pos);
                        if (state.getBlock() instanceof BonemealableBlock growable) {
                            if (growable.isValidBonemealTarget(world, pos, state)) {
                                growable.performBonemeal(
                                        (ServerLevel) world, random, pos, state);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
