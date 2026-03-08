package ru.nyansus.mc.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import ru.nyansus.mc.config.DairatzConfig;
import ru.nyansus.mc.registry.ModEntities;

public class DairatzEntity extends TamableAnimal implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> ON_HEAD = SynchedEntityData.defineId(
            DairatzEntity.class, EntityDataSerializers.BOOLEAN
    );

    public final AnimationState idleAnimationState = new AnimationState();
    private int mountCooldown = 0;

    public DairatzEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0, DairatzConfig.fireRate, 16.0f) {
            @Override
            public boolean canUse() {
                return !DairatzEntity.this.isOnHead() && super.canUse();
            }
        });
        goalSelector.addGoal(2, new TemptGoal(this, 1.2, stack -> stack.is(Items.POPPY), false));
        goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0, 10.0f, 3.0f) {
            @Override
            public boolean canUse() {
                return !DairatzEntity.this.isOnHead() && super.canUse();
            }
        });
        goalSelector.addGoal(4, new PollinateGoal());
        goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0) {
            @Override
            public boolean canUse() {
                return !DairatzEntity.this.isOnHead() && !DairatzEntity.this.isTame() && super.canUse();
            }
        });
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return !DairatzEntity.this.isOnHead() && super.canUse();
            }
        });
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, DairatzConfig.fairyHealth)
                .add(Attributes.FLYING_SPEED, 0.4)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.TEMPT_RANGE, 16.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ON_HEAD, false);
    }

    public boolean isOnHead() {
        return entityData.get(ON_HEAD);
    }

    public void setOnHead(boolean onHead) {
        entityData.set(ON_HEAD, onHead);
    }

    @Override
    public void tick() {
        super.tick();
        if (mountCooldown > 0) mountCooldown--;

        if (isOnHead() && isTame()) {
            LivingEntity owner = getOwner();
            if (owner != null && owner.isAlive()) {
                setPos(owner.getX(), owner.getEyeY() + 0.3, owner.getZ());
                setYRot(owner.getYRot());
                setDeltaMovement(Vec3.ZERO);
                fallDistance = 0;
                getNavigation().stop();
                setTarget(null);

                if (owner instanceof Player player && player.isShiftKeyDown() && mountCooldown <= 0) {
                    if (!level().isClientSide()) {
                        setOnHead(false);
                    }
                }
            } else {
                setOnHead(false);
            }
        }

        if (level().isClientSide()) {
            if (!idleAnimationState.isStarted()) {
                idleAnimationState.start(tickCount);
            }
        }
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!isTame() && stack.is(Items.POPPY)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (!level().isClientSide()) {
                if (random.nextInt(3) == 0) {
                    tame(player);
                    level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (isTame() && isOwnedBy(player) && !isOnHead()) {
            if (!level().isClientSide()) {
                setOnHead(true);
                mountCooldown = 10;
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        FurballEntity furball = new FurballEntity(level(), this);
        double dx = target.getX() - getX();
        double dy = target.getEyeY() - getEyeY();
        double dz = target.getZ() - getZ();
        furball.shoot(dx, dy, dz, 1.5f, 1.0f);
        serverLevel.addFreshEntity(furball);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.POPPY);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("OnHead", isOnHead());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setOnHead(input.getBooleanOr("OnHead", false));
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !isTame();
    }

    @Override
    protected boolean canFlyToOwner() {
        return true;
    }

    public static boolean checkFairySpawnRules(EntityType<DairatzEntity> type, LevelAccessor level,
                                                EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON);
    }

    private class PollinateGoal extends Goal {
        private BlockPos targetFlower;
        private int pollinatingTicks;
        private int cooldown;

        @Override
        public boolean canUse() {
            if (isTame() || isOnHead()) return false;
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
            if (targetFlower == null) return;
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
            if (world.isClientSide()) return;
            BlockPos center = blockPosition();
            for (int dx = -3; dx <= 3; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        BlockPos pos = center.offset(dx, dy, dz);
                        BlockState state = world.getBlockState(pos);
                        if (state.getBlock() instanceof BonemealableBlock growable) {
                            if (growable.isValidBonemealTarget(world, pos, state)) {
                                growable.performBonemeal((ServerLevel) world, random, pos, state);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
