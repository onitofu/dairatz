package ru.nyansus.mc.entity.ai;

import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import ru.nyansus.mc.entity.AbstractFairyEntity;

public final class FairyOwnerAttackGoal extends OwnerHurtTargetGoal {
    private final AbstractFairyEntity fairy;

    public FairyOwnerAttackGoal(AbstractFairyEntity fairy) {
        super(fairy);
        this.fairy = fairy;
    }

    @Override
    public boolean canUse() {
        return !fairy.hasHome() && !fairy.isOnHead() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !fairy.hasHome() && fairy.isTargetInRange() && super.canContinueToUse();
    }
}
