package ru.nyansus.mc.entity.ai;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import ru.nyansus.mc.entity.HousedFairyEntity;
import ru.nyansus.mc.hollow.FairyHollowBlockEntity;

public final class FairyReturnHomeGoal extends Goal {
    private final HousedFairyEntity fairy;

    public FairyReturnHomeGoal(HousedFairyEntity fairy) {
        this.fairy = fairy;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return fairy.hasHome() && !fairy.isOnHead()
                && (fairy.wantsShelter() || !fairy.isWithinGarden(fairy.blockPosition()));
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        BlockPos pos = fairy.home().pos();
        if (!fairy.level().hasChunkAt(pos)
                || !(fairy.level().getBlockEntity(pos) instanceof FairyHollowBlockEntity hollow)) {
            return;
        }
        Vec3 entrance = hollow.entrance();
        if (fairy.wantsShelter() && fairy.position().distanceToSqr(entrance) < 2.25
                && hollow.isEntranceClear()) {
            hollow.enter(fairy);
        } else if (fairy.tickCount % 10 == 0) {
            fairy.getNavigation().moveTo(entrance.x, entrance.y, entrance.z, 1.0);
        }
    }

    @Override
    public void stop() {
        fairy.getNavigation().stop();
    }
}
