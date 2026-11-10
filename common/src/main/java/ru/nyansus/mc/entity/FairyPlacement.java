package ru.nyansus.mc.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class FairyPlacement {
    private FairyPlacement() {
    }

    public static boolean isSafe(Level level, AbstractFairyEntity fairy, Vec3 point) {
        AABB bounds = fairy.getBoundingBox().move(point.subtract(fairy.position()));
        BlockPos minimum = BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ);
        BlockPos maximum = BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ);
        return minimum.getY() >= level.getMinY() && maximum.getY() <= level.getMaxY()
                && level.getWorldBorder().isWithinBounds(bounds) && level.hasChunksAt(minimum, maximum)
                && level.noCollision(fairy, bounds) && !level.containsAnyLiquid(bounds);
    }
}
