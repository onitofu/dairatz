package ru.nyansus.mc.entity.ai;

import java.util.EnumSet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import ru.nyansus.mc.entity.AbstractFairyEntity;

/** Keeps a ranged fairy mobile while it pursues and attacks its target. */
public final class FairyRangedCombatGoal extends Goal {
    private static final double PREFERRED_DISTANCE = 6.0;
    private static final double MIN_DISTANCE = 3.5;
    private static final double DISTANCE_VARIATION = 1.0;
    private static final double MIN_ORBIT_STEP = 0.55;
    private static final double ORBIT_STEP_VARIATION = 0.45;
    private static final double DODGE_ORBIT_STEP = 1.2;
    private static final double DODGE_DISTANCE_BONUS = 2.0;
    private static final double VERTICAL_CENTER_OFFSET = 0.5;
    private static final double VERTICAL_VARIATION = 1.25;
    private static final double MIN_HEIGHT_ABOVE_TARGET = 1.0;
    private static final float LOOK_ROTATION_SPEED = 30.0f;
    private static final int MIN_MANEUVER_TICKS = 12;
    private static final int MANEUVER_TICK_VARIATION = 13;
    private static final int DODGE_MANEUVER_TICKS = 5;
    private static final int ORBIT_DIRECTION_CHANGE_CHANCE = 4;

    private final AbstractFairyEntity fairy;
    private final double speedModifier;
    private final int attackInterval;
    private final float attackRange;
    private final double attackRangeSqr;

    private LivingEntity target;
    private int attackCooldown;
    private int maneuverCooldown;
    private int orbitDirection = 1;
    private float observedHealth;

    public FairyRangedCombatGoal(
            AbstractFairyEntity fairy,
            double speedModifier,
            int attackInterval,
            float attackRange) {
        this.fairy = fairy;
        this.speedModifier = speedModifier;
        this.attackInterval = attackInterval;
        this.attackRange = attackRange;
        this.attackRangeSqr = attackRange * attackRange;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity currentTarget = fairy.getTarget();
        if (fairy.isOnHead() || currentTarget == null || !currentTarget.isAlive()) {
            return false;
        }
        target = currentTarget;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !fairy.isOnHead()
                && target != null
                && target.isAlive()
                && fairy.getTarget() == target;
    }

    @Override
    public void start() {
        attackCooldown = Math.min(attackInterval, MIN_MANEUVER_TICKS);
        maneuverCooldown = 0;
        orbitDirection = fairy.getRandom().nextBoolean() ? 1 : -1;
        observedHealth = fairy.getHealth();
    }

    @Override
    public void stop() {
        target = null;
        fairy.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (target == null) {
            return;
        }

        double distanceSqr = fairy.distanceToSqr(target);
        boolean canSeeTarget = fairy.getSensing().hasLineOfSight(target);
        boolean tookDamage = fairy.getHealth() < observedHealth;
        observedHealth = fairy.getHealth();

        fairy.getLookControl().setLookAt(
                target, LOOK_ROTATION_SPEED, LOOK_ROTATION_SPEED);

        if (attackCooldown > 0) {
            attackCooldown--;
        }
        if (attackCooldown <= 0 && canSeeTarget && distanceSqr <= attackRangeSqr) {
            float distanceFactor = (float) Math.min(
                    1.0, Math.sqrt(distanceSqr) / attackRange);
            fairy.performRangedAttack(target, distanceFactor);
            attackCooldown = attackInterval;
        }

        if (maneuverCooldown > 0) {
            maneuverCooldown--;
        }
        if (tookDamage || maneuverCooldown <= 0 || fairy.getNavigation().isDone()) {
            updateMovement(distanceSqr, canSeeTarget, tookDamage);
        }
    }

    private void updateMovement(
            double distanceSqr, boolean canSeeTarget, boolean dodge) {
        if (target == null) {
            return;
        }

        RandomSource random = fairy.getRandom();
        if (dodge || random.nextInt(ORBIT_DIRECTION_CHANGE_CHANCE) == 0) {
            orbitDirection = -orbitDirection;
        }

        if (!canSeeTarget || distanceSqr > attackRangeSqr) {
            fairy.getNavigation().moveTo(target, speedModifier);
        } else {
            moveAroundTarget(distanceSqr, dodge, random);
        }

        maneuverCooldown = dodge
                ? DODGE_MANEUVER_TICKS
                : MIN_MANEUVER_TICKS + random.nextInt(MANEUVER_TICK_VARIATION);
    }

    private void moveAroundTarget(
            double distanceSqr, boolean dodge, RandomSource random) {
        double currentAngle = Math.atan2(
                fairy.getZ() - target.getZ(), fairy.getX() - target.getX());
        double orbitStep = dodge
                ? DODGE_ORBIT_STEP
                : MIN_ORBIT_STEP + random.nextDouble() * ORBIT_STEP_VARIATION;
        double destinationAngle = currentAngle + orbitDirection * orbitStep;

        double destinationRadius = PREFERRED_DISTANCE
                + (random.nextDouble() * 2.0 - 1.0) * DISTANCE_VARIATION;
        if (distanceSqr < MIN_DISTANCE * MIN_DISTANCE || dodge) {
            destinationRadius += DODGE_DISTANCE_BONUS;
        }

        double destinationX = target.getX()
                + Math.cos(destinationAngle) * destinationRadius;
        double destinationZ = target.getZ()
                + Math.sin(destinationAngle) * destinationRadius;
        double destinationY = Math.max(
                target.getY() + MIN_HEIGHT_ABOVE_TARGET,
                target.getEyeY() + VERTICAL_CENTER_OFFSET
                        + (random.nextDouble() * 2.0 - 1.0) * VERTICAL_VARIATION);

        if (!fairy.getNavigation().moveTo(
                destinationX, destinationY, destinationZ, speedModifier)) {
            fairy.getMoveControl().setWantedPosition(
                    destinationX, destinationY, destinationZ, speedModifier);
        }
    }
}
