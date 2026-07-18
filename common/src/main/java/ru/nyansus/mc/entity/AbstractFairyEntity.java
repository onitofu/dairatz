package ru.nyansus.mc.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import ru.nyansus.mc.config.DairatzConfig;
import ru.nyansus.mc.entity.ai.FairyRangedCombatGoal;

public abstract class AbstractFairyEntity extends TamableAnimal implements RangedAttackMob {

    private static final int FLY_CONTROL_MAX_TURN = 20;

    private static final double RANGED_ATTACK_SPEED = 1.15;
    private static final float RANGED_ATTACK_RANGE = 10.0f;
    private static final double TEMPT_SPEED = 1.2;
    private static final double FOLLOW_OWNER_SPEED = 1.0;
    private static final float FOLLOW_START_DISTANCE = 10.0f;
    private static final float FOLLOW_STOP_DISTANCE = 3.0f;
    private static final double WANDER_SPEED = 1.0;
    private static final float LOOK_AT_PLAYER_RANGE = 6.0f;

    private static final double DEFAULT_MOVEMENT_SPEED = 0.3;
    private static final double DEFAULT_FOLLOW_RANGE = 48.0;
    private static final double DEFAULT_TEMPT_RANGE = 16.0;

    private static final double HEAD_Y_OFFSET = 0.15;
    private static final int MOUNT_COOLDOWN_TICKS = 10;
    private static final double HEAD_CHECK_INFLATE = 1.0;

    private static final double MAX_TARGET_DISTANCE = 32.0;
    private static final double MAX_TARGET_DISTANCE_SQ = MAX_TARGET_DISTANCE * MAX_TARGET_DISTANCE;
    private static final double HARD_TELEPORT_DISTANCE = 48.0;
    private static final double HARD_TELEPORT_DISTANCE_SQ =
            HARD_TELEPORT_DISTANCE * HARD_TELEPORT_DISTANCE;
    private static final int TELEPORT_CHECK_INTERVAL = 40;
    private static final double TELEPORT_SCATTER_RANGE = 4.0;
    private static final double TELEPORT_Y_OFFSET = 1.0;

    private static final float PROJECTILE_SPEED = 1.5f;
    private static final float PROJECTILE_INACCURACY = 1.0f;
    private static final float THROW_SOUND_VOLUME = 1.0f;
    private static final float THROW_PITCH_RANGE = 0.4f;
    private static final float THROW_PITCH_BASE = 0.8f;

    private static final EntityDataAccessor<Boolean> ON_HEAD = SynchedEntityData.defineId(
            AbstractFairyEntity.class, EntityDataSerializers.BOOLEAN
    );

    public final AnimationState idleAnimationState = new AnimationState();
    private int mountCooldown = 0;

    protected AbstractFairyEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, FLY_CONTROL_MAX_TURN, true);
    }

    protected abstract Item getTameItem();

    protected abstract int getFireRate();

    protected abstract ThrowableItemProjectile createProjectile();

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new FairyRangedCombatGoal(
                this, RANGED_ATTACK_SPEED, getFireRate(), RANGED_ATTACK_RANGE));
        goalSelector.addGoal(2, new TemptGoal(
                this, TEMPT_SPEED, stack -> stack.is(getTameItem()), false));
        FollowOwnerGoal followGoal = new FollowOwnerGoal(
                this, FOLLOW_OWNER_SPEED,
                FOLLOW_START_DISTANCE, FOLLOW_STOP_DISTANCE) {
            @Override
            public boolean canUse() {
                return !AbstractFairyEntity.this.isOnHead() && super.canUse();
            }
        };
        goalSelector.addGoal(3, followGoal);
        goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, WANDER_SPEED) {
            @Override
            public boolean canUse() {
                return !AbstractFairyEntity.this.isOnHead()
                        && !AbstractFairyEntity.this.isTame() && super.canUse();
            }
        });
        goalSelector.addGoal(6,
                new LookAtPlayerGoal(this, Player.class, LOOK_AT_PLAYER_RANGE));
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
                .add(Attributes.MOVEMENT_SPEED, DEFAULT_MOVEMENT_SPEED)
                .add(Attributes.FOLLOW_RANGE, DEFAULT_FOLLOW_RANGE)
                .add(Attributes.TEMPT_RANGE, DEFAULT_TEMPT_RANGE);
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
                setPos(owner.getX(), owner.getEyeY() + HEAD_Y_OFFSET, owner.getZ());
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
                if (distSq > HARD_TELEPORT_DISTANCE_SQ
                        && tickCount % TELEPORT_CHECK_INTERVAL == 0) {
                    double x = owner.getX()
                            + (random.nextDouble() - 0.5) * TELEPORT_SCATTER_RANGE;
                    double y = owner.getY() + TELEPORT_Y_OFFSET;
                    double z = owner.getZ()
                            + (random.nextDouble() - 0.5) * TELEPORT_SCATTER_RANGE;
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
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ThrowableItemProjectile projectile = createProjectile();
        double dx = target.getX() - getX();
        double dy = target.getEyeY() - getEyeY();
        double dz = target.getZ() - getZ();
        projectile.shoot(dx, dy, dz, PROJECTILE_SPEED, PROJECTILE_INACCURACY);
        serverLevel.addFreshEntity(projectile);
        playSound(SoundEvents.SNOWBALL_THROW, THROW_SOUND_VOLUME,
                1.0f / (getRandom().nextFloat() * THROW_PITCH_RANGE + THROW_PITCH_BASE));
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
                if (random.nextInt(DairatzConfig.tameChance) == 0) {
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
                heal((float) DairatzConfig.healAmount);
                level().broadcastEntityEvent(this, (byte) 7);
            }
            return InteractionResult.SUCCESS;
        }

        if (isTame() && isOwnedBy(player) && !isOnHead()) {
            if (!level().isClientSide()) {
                boolean alreadyHasFairy = !level().getEntitiesOfClass(
                        AbstractFairyEntity.class,
                        player.getBoundingBox().inflate(HEAD_CHECK_INFLATE),
                        f -> f.isOnHead() && f.isOwnedBy(player)
                ).isEmpty();
                if (alreadyHasFairy) {
                    return InteractionResult.PASS;
                }
                setOnHead(true);
                mountCooldown = MOUNT_COOLDOWN_TICKS;
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
