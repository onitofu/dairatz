package ru.nyansus.mc.entity.ai;

import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import ru.nyansus.mc.entity.AbstractFairyEntity;

public final class FairyFollowOwnerGoal extends FollowOwnerGoal {
    private final AbstractFairyEntity fairy;

    public FairyFollowOwnerGoal(AbstractFairyEntity fairy, double speed, float startDistance, float stopDistance) {
        super(fairy, speed, startDistance, stopDistance);
        this.fairy = fairy;
    }

    @Override
    public boolean canUse() {
        return !fairy.hasHome() && !fairy.isOnHead() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !fairy.hasHome() && !fairy.isOnHead() && super.canContinueToUse();
    }
}
