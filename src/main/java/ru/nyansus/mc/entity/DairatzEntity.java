package ru.nyansus.mc.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
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
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        FurballEntity furball = new FurballEntity(level(), this);
        double dx = target.getX() - getX();
        double dy = target.getEyeY() - getEyeY();
        double dz = target.getZ() - getZ();
        furball.shoot(dx, dy, dz, 1.5f, 1.0f);
        serverLevel.addFreshEntity(furball);
        playSound(net.minecraft.sounds.SoundEvents.SNOWBALL_THROW, 1.0f,
                1.0f / (getRandom().nextFloat() * 0.4f + 0.8f));
    }

    private class PollinateGoal extends Goal {
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
                    targetFlower.getX() + 0.5,
                    targetFlower.getY() + 1.0,
                    targetFlower.getZ() + 0.5,
                    1.0
            );
            double dist = distanceToSqr(
                    targetFlower.getX() + 0.5,
                    targetFlower.getY() + 1.0,
                    targetFlower.getZ() + 0.5
            );
            if (dist < 2.5) {
                pollinatingTicks++;
                if (pollinatingTicks >= 60) {
                    growNearby();
                    cooldown = 200;
                    stop();
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return targetFlower != null && pollinatingTicks < 60 && !isTame();
        }

        private BlockPos findFlower() {
            BlockPos fairyPos = blockPosition();
            for (int i = 0; i < 10; i++) {
                BlockPos pos = fairyPos.offset(
                        random.nextIntBetweenInclusive(-8, 8),
                        random.nextIntBetweenInclusive(-3, 3),
                        random.nextIntBetweenInclusive(-8, 8)
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
            for (int dx = -3; dx <= 3; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    for (int dz = -3; dz <= 3; dz++) {
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
