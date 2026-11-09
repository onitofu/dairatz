package ru.nyansus.mc.entity.ai;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import ru.nyansus.mc.entity.DairatzEntity;

public final class FairyGardenWanderGoal extends Goal {
    private final DairatzEntity fairy;
    private BlockPos destination;

    public FairyGardenWanderGoal(DairatzEntity fairy) {
        this.fairy = fairy;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!fairy.hasHome() || fairy.wantsShelter() || fairy.isOnHead()
                || fairy.getRandom().nextInt(20) != 0) {
            return false;
        }
        BlockPos home = fairy.home().pos();
        destination = home.offset(fairy.getRandom().nextInt(15) - 7,
                fairy.getRandom().nextInt(5) - 1, fairy.getRandom().nextInt(15) - 7);
        return fairy.isWithinGarden(destination) && fairy.level().hasChunkAt(destination)
                && fairy.level().getBlockState(destination).isAir();
    }

    @Override
    public boolean canContinueToUse() {
        return fairy.hasHome() && !fairy.wantsShelter() && !fairy.getNavigation().isDone();
    }

    @Override
    public void start() {
        fairy.getNavigation().moveTo(
                destination.getX() + 0.5, destination.getY() + 0.5, destination.getZ() + 0.5, 1.0);
    }

    @Override
    public void stop() {
        fairy.getNavigation().stop();
    }
}
