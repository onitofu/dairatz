package ru.nyansus.mc.hollow;

import net.minecraft.world.level.Level;
import ru.nyansus.mc.entity.FairyKind;

public final class FairyActivitySchedule {
    private FairyActivitySchedule() {
    }

    public static boolean isRestTime(Level level, FairyKind kind) {
        if (kind == FairyKind.PALE) {
            return !level.dimension().equals(Level.OVERWORLD)
                    || level.getDayTime() % 24000 < 12000 || level.isRaining();
        }
        return isRestTime(level);
    }

    public static boolean isRestTime(Level level) {
        return level.dimension().equals(Level.OVERWORLD)
                && (level.getDayTime() % 24000 >= 12000 || level.isRaining());
    }
}
