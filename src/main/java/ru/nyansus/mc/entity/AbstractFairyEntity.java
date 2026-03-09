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
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractFairyEntity extends TamableAnimal implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> ON_HEAD = SynchedEntityData.defineId(
            AbstractFairyEntity.class, EntityDataSerializers.BOOLEAN
    );

    public final AnimationState idleAnimationState = new AnimationState();
    private int mountCooldown = 0;

    protected AbstractFairyEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    protected abstract Item getTameItem();

    protected abstract int getFireRate();

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0, getFireRate(), 16.0f) {
            @Override
            public boolean canUse() {
                return !AbstractFairyEntity.this.isOnHead() && super.canUse();
            }
        });
        goalSelector.addGoal(2, new TemptGoal(
                this, 1.2, stack -> stack.is(getTameItem()), false));
        goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0, 10.0f, 3.0f) {
            @Override
            public boolean canUse() {
                return !AbstractFairyEntity.this.isOnHead() && super.canUse();
            }
        });
        goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0) {
            @Override
            public boolean canUse() {
                return !AbstractFairyEntity.this.isOnHead()
                        && !AbstractFairyEntity.this.isTame() && super.canUse();
            }
        });
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return !AbstractFairyEntity.this.isOnHead() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return isTargetInRange() && super.canContinueToUse();
            }
        });
        targetSelector.addGoal(2, new OwnerHurtTargetGoal(this) {
            @Override
            public boolean canUse() {
                return !AbstractFairyEntity.this.isOnHead() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return isTargetInRange() && super.canContinueToUse();
            }
        });
    }

    protected static AttributeSupplier.Builder createFairyAttributes(
            double health, double flySpeed) {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.FLYING_SPEED, flySpeed)
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
        if (mountCooldown > 0) {
            mountCooldown--;
        }

        if (isOnHead() && isTame()) {
            LivingEntity owner = getOwner();
            if (owner != null && owner.isAlive()) {
                setPos(owner.getX(), owner.getEyeY() + 0.15, owner.getZ());
                setYRot(owner.getYRot());
                setDeltaMovement(Vec3.ZERO);
                fallDistance = 0;
                getNavigation().stop();
                setTarget(null);

                onHeadTick(owner);

                if (owner instanceof Player player && player.isShiftKeyDown()
                        && mountCooldown <= 0) {
                    if (!level().isClientSide()) {
                        setOnHead(false);
                    }
                }
            } else {
                setOnHead(false);
            }
        }

        if (!level().isClientSide() && isTame() && !isOnHead()) {
            LivingEntity owner = getOwner();
            if (owner != null && owner.isAlive()) {
                double distSq = distanceToSqr(owner);
                if (getTarget() != null && distSq > MAX_TARGET_DISTANCE_SQ) {
                    setTarget(null);
                }
                if (distSq > HARD_TELEPORT_DISTANCE_SQ && tickCount % 40 == 0) {
                    double x = owner.getX() + (random.nextDouble() - 0.5) * 4;
                    double y = owner.getY() + 1;
                    double z = owner.getZ() + (random.nextDouble() - 0.5) * 4;
                    teleportTo(x, y, z);
                    getNavigation().stop();
                }
            }
        }

        if (level().isClientSide()) {
            if (isOnHead()) {
                idleAnimationState.stop();
            } else if (!idleAnimationState.isStarted()) {
                idleAnimationState.start(tickCount);
            }
        }
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target instanceof AbstractFairyEntity) {
            return false;
        }
        return super.wantsToAttack(target, owner);
    }

    private static final double MAX_TARGET_DISTANCE_SQ = 32.0 * 32.0;
    private static final double HARD_TELEPORT_DISTANCE_SQ = 48.0 * 48.0;

    protected boolean isTargetInRange() {
        LivingEntity owner = getOwner();
        if (owner == null) {
            return false;
        }
        return distanceToSqr(owner) <= MAX_TARGET_DISTANCE_SQ;
    }

    protected void onHeadTick(LivingEntity owner) {
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
        Item tameItem = getTameItem();

        if (!isTame() && stack.is(tameItem)) {
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

        if (isTame() && isOwnedBy(player) && stack.is(tameItem)
                && getHealth() < getMaxHealth()) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (!level().isClientSide()) {
                heal(4.0f);
                level().broadcastEntityEvent(this, (byte) 7);
            }
            return InteractionResult.SUCCESS;
        }

        if (isTame() && isOwnedBy(player) && !isOnHead()) {
            if (!level().isClientSide()) {
                boolean alreadyHasFairy = !level().getEntitiesOfClass(
                        AbstractFairyEntity.class,
                        player.getBoundingBox().inflate(1.0),
                        f -> f.isOnHead() && f.isOwnedBy(player)
                ).isEmpty();
                if (alreadyHasFairy) {
                    return InteractionResult.PASS;
                }
                setOnHead(true);
                mountCooldown = 10;
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean isPushable() {
        return !isOnHead();
    }

    @Override
    protected void pushEntities() {
        if (!isOnHead()) {
            super.pushEntities();
        }
    }

    @Override
    public boolean causeFallDamage(
            double fallDistance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(
            double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(getTameItem());
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

    public static boolean checkFairySpawnRules(
            EntityType<? extends AbstractFairyEntity> type, LevelAccessor level,
            EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON);
    }
}
