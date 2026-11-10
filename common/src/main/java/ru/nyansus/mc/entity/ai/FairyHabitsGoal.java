package ru.nyansus.mc.entity.ai;

import java.util.EnumSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import ru.nyansus.mc.entity.AbstractFairyEntity;
import ru.nyansus.mc.entity.FairyAbilities;
import ru.nyansus.mc.entity.FairyKind;

public final class FairyHabitsGoal extends Goal {
    private static final int TRAVEL_TIMEOUT_TICKS = 100;
    private final AbstractFairyEntity fairy;
    private FairyInterest interest;
    private Vec3 destination;
    private LivingEntity threat;
    private long nextSearch;
    private long started;
    private int age;
    private int travelTicks;
    private int watchingTicks;
    private int episodeDuration;
    private int pauseDuration;
    private int step;
    private int patrolIndex;
    private boolean blinked;
    private boolean originalGravity;

    public FairyHabitsGoal(AbstractFairyEntity fairy) {
        this.fairy = fairy;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        if (!(fairy.level() instanceof ServerLevel level) || !FairyInterests.canAct(fairy)
                || level.getGameTime() < nextSearch
                || FairyAbilities.time(level) - FairyAbilities.state(fairy).lastDamage < 40) {
            return false;
        }
        nextSearch = level.getGameTime() + 40;
        interest = FairyInterests.choose(fairy, patrolIndex);
        if (interest == null) {
            if (fairy.kind() == FairyKind.PALE) {
                patrolIndex = (patrolIndex + 1) % 3;
            }
            return false;
        }
        destination = FairyInterests.approach(fairy, interest, 0);
        return destination != null;
    }

    @Override
    public void start() {
        age = 0;
        travelTicks = 0;
        watchingTicks = 0;
        step = 0;
        blinked = false;
        threat = null;
        originalGravity = fairy.isNoGravity();
        started = FairyAbilities.time((ServerLevel) fairy.level());
        int temperament = Math.floorMod(fairy.getUUID().hashCode(), 41);
        episodeDuration = 160 + temperament * 2;
        pauseDuration = 35 + temperament;
    }

    @Override
    public boolean canContinueToUse() {
        return interest != null && destination != null && age < episodeDuration
                && travelTicks < TRAVEL_TIMEOUT_TICKS && FairyInterests.canAct(fairy)
                && FairyInterests.valid(fairy, interest) && FairyInterests.safe(fairy, destination)
                && FairyAbilities.state(fairy).lastDamage < started;
    }

    @Override
    public void tick() {
        age++;
        if (fairy.kind() == FairyKind.PALE && age % 20 == 0) {
            threat = FairyInterests.threat(fairy);
        }
        if (threat != null && threat.isAlive() && threat.level() == fairy.level()
                && fairy.distanceToSqr(threat) <= 144) {
            hover(false);
            face(threat.getEyePosition());
            return;
        }
        Vec3 focus = interest.currentFocus();
        face(focus);
        if (interest.subject() != null && age % 10 == 0) {
            Vec3 adjusted = FairyInterests.approach(fairy, interest, step);
            if (adjusted == null) {
                destination = null;
                return;
            }
            destination = adjusted;
        }
        boolean resting = interest.habit() == FairyHabit.WINTER_REST || interest.habit() == FairyHabit.PALE_SHELTER;
        double arrivalDistance = resting ? 0.0225 : 0.64;
        if (fairy.position().distanceToSqr(destination) > arrivalDistance) {
            fairy.setResting(false);
            watchingTicks = 0;
            travelTicks++;
            if (age % 10 == 1 || fairy.getNavigation().isDone()) {
                FairyInterests.moveTo(fairy, destination, speed());
            }
            return;
        }
        travelTicks = 0;
        watchingTicks++;
        if (resting) {
            fairy.setPos(destination);
        }
        hover(resting);
        if (interest.habit() == FairyHabit.PALE_WATCH) {
            Vec3 outward = destination.subtract(FairyInterests.center(fairy)).multiply(1, 0, 1).normalize();
            face(destination.add(outward.scale(3)));
        }
        if (watchingTicks < pauseDuration) {
            return;
        }
        if (interest.habit() == FairyHabit.WINTER_REST || interest.habit() == FairyHabit.PALE_SHELTER
                || interest.habit() == FairyHabit.PALE_WATCH || interest.habit() == FairyHabit.AVOID_HEAT) {
            destination = null;
            return;
        }
        watchingTicks = 0;
        step++;
        Vec3 next = FairyInterests.approach(fairy, interest, step);
        if (interest.habit() == FairyHabit.ENDER_INSPECT && !blinked) {
            blinked = true;
            if (next != null && next.distanceToSqr(fairy.position()) <= 9 && FairyInterests.safe(fairy, next)) {
                ServerLevel level = (ServerLevel) fairy.level();
                level.sendParticles(ParticleTypes.PORTAL,
                        fairy.getX(), fairy.getY(), fairy.getZ(), 8, 0.2, 0.2, 0.2, 0.04);
                fairy.teleportTo(next.x, next.y, next.z);
                level.sendParticles(ParticleTypes.PORTAL, next.x, next.y, next.z, 8, 0.2, 0.2, 0.2, 0.04);
            }
        }
        destination = step >= 3 ? null : next;
    }

    private double speed() {
        return switch (fairy.kind()) {
            case WINTER -> 0.65;
            case JUNGLE -> 1.15;
            case CHERRY -> 0.7;
            default -> 0.85;
        };
    }

    private void hover(boolean resting) {
        fairy.getNavigation().stop();
        fairy.setDeltaMovement(Vec3.ZERO);
        fairy.setNoGravity(true);
        fairy.setResting(resting);
    }

    private void face(Vec3 focus) {
        fairy.getLookControl().setLookAt(focus.x, focus.y, focus.z);
        Vec3 direction = focus.subtract(fairy.getEyePosition());
        if (direction.horizontalDistanceSqr() > 0.01) {
            float angle = (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90;
            float yaw = Mth.rotLerp(0.2f, fairy.getYRot(), angle);
            fairy.setYRot(yaw);
            fairy.setYBodyRot(yaw);
            fairy.setYHeadRot(yaw);
        }
    }

    @Override
    public void stop() {
        fairy.getNavigation().stop();
        fairy.setResting(false);
        fairy.setNoGravity(originalGravity);
        if (interest != null && interest.habit() == FairyHabit.PALE_WATCH) {
            patrolIndex = (patrolIndex + 1) % 3;
        }
        nextSearch = fairy.level().getGameTime() + (fairy.kind() == FairyKind.PALE ? 10
                : 60 + Math.floorMod(fairy.getUUID().hashCode(), 81));
        interest = null;
        destination = null;
        threat = null;
    }
}
