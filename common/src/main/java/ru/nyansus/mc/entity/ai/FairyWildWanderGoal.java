package ru.nyansus.mc.entity.ai;

import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import ru.nyansus.mc.entity.AbstractFairyEntity;

public final class FairyWildWanderGoal extends WaterAvoidingRandomFlyingGoal {
    private final AbstractFairyEntity fairy;

    public FairyWildWanderGoal(AbstractFairyEntity fairy, double speed) {
        super(fairy, speed);
        this.fairy = fairy;
    }

    @Override
    public boolean canUse() {
        return !fairy.hasHome() && !fairy.isOnHead() && !fairy.isTame() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !fairy.hasHome() && !fairy.isOnHead() && !fairy.isTame() && super.canContinueToUse();
    }
}
