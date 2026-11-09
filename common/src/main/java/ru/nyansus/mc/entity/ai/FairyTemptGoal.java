package ru.nyansus.mc.entity.ai;

import net.minecraft.world.entity.ai.goal.TemptGoal;
import ru.nyansus.mc.entity.AbstractFairyEntity;

public final class FairyTemptGoal extends TemptGoal {
    private final AbstractFairyEntity fairy;

    public FairyTemptGoal(AbstractFairyEntity fairy, double speed) {
        super(fairy, speed, fairy::isFood, false);
        this.fairy = fairy;
    }

    @Override
    public boolean canUse() {
        return !fairy.hasHome() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !fairy.hasHome() && super.canContinueToUse();
    }
}
