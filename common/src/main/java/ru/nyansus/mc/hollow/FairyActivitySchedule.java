package ru.nyansus.mc.hollow;

import net.minecraft.world.level.Level;

public final class FairyActivitySchedule {
    private FairyActivitySchedule() {
    }

    public static boolean isRestTime(Level level) {
        return level.dimension().equals(Level.OVERWORLD)
                && (level.getDayTime() % 24000 >= 12000 || level.isRaining());
    }
}
