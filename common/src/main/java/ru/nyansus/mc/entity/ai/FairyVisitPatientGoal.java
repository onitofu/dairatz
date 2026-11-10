package ru.nyansus.mc.entity.ai;

import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import ru.nyansus.mc.entity.AbstractFairyEntity;
import ru.nyansus.mc.entity.FairyAbilities;
import ru.nyansus.mc.entity.FairyKind;
import ru.nyansus.mc.entity.HousedFairyEntity;

public final class FairyVisitPatientGoal extends Goal {
    private final AbstractFairyEntity fairy;
    private LivingEntity patient;
    private long nextSearch;
    private int travelTicks;

    public FairyVisitPatientGoal(AbstractFairyEntity fairy) {
        this.fairy = fairy;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        if (fairy.kind() != FairyKind.CHERRY || !(fairy.level() instanceof ServerLevel)
                || !FairyInterests.canAct(fairy) || fairy.level().getGameTime() < nextSearch
                || !(fairy instanceof HousedFairyEntity housed) || !housed.hasHome()) {
            return false;
        }
        nextSearch = fairy.level().getGameTime() + 20;
        patient = FairyAbilities.gardenPatient(housed);
        return patient != null;
    }

    @Override
    public void start() {
        travelTicks = 0;
        fairy.setResting(false);
    }

    @Override
    public boolean canContinueToUse() {
        return patient != null && travelTicks < 120 && FairyInterests.canAct(fairy)
                && FairyAbilities.isGardenPatient((HousedFairyEntity) fairy, patient);
    }

    @Override
    public void tick() {
        travelTicks++;
        fairy.getLookControl().setLookAt(patient, 30, 30);
        if (fairy.distanceToSqr(patient) <= 4 && fairy.hasLineOfSight(patient)) {
            fairy.getNavigation().stop();
            return;
        }
        if (travelTicks % 10 == 1) {
            for (int offset = 0; offset < 4; offset++) {
                double angle = offset * Math.PI / 2;
                Vec3 point = patient.position().add(Math.cos(angle), 0.7, Math.sin(angle));
                if (FairyInterests.safe(fairy, point)) {
                    FairyInterests.moveTo(fairy, point, 0.8);
                    return;
                }
            }
        }
    }

    @Override
    public void stop() {
        fairy.getNavigation().stop();
        patient = null;
        nextSearch = fairy.level().getGameTime() + (travelTicks >= 120 ? 100 : 20);
    }
}
