package ru.nyansus.mc.hollow;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import ru.nyansus.mc.entity.FairyKind;
import ru.nyansus.mc.registry.ModHollows;

public final class FairyHollowFeature extends Feature<NoneFeatureConfiguration> {
    public FairyHollowFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int chunkX = context.origin().getX() & ~15;
        int chunkZ = context.origin().getZ() & ~15;
        boolean placed = false;
        for (int x = chunkX; x < chunkX + 16; x++) {
            for (int z = chunkZ; z < chunkZ + 16; z++) {
                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                for (int y = top - 1; y >= Math.max(level.getMinY(), top - 64); y--) {
                    BlockPos base = new BlockPos(x, y, z);
                    FairyKind kind = trunkKind(level, base);
                    if (kind == null || !level.getBlockState(base.below()).is(BlockTags.DIRT)) {
                        continue;
                    }
                    BlockPos canonical = canonicalBase(level, base, kind, chunkX, chunkZ);
                    if (canonical != null && canonical.equals(base) && hasCanopy(level, base, kind)
                            && random.nextInt(20) == 0) {
                        placed |= placeInTrunk(level, base, kind, random, chunkX, chunkZ);
                    }
                    break;
                }
            }
        }
        return placed;
    }

    private FairyKind trunkKind(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        for (FairyKind kind : FairyKind.values()) {
            if (kind != FairyKind.ENDER && state.is(kind.log())
                    && state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y) {
                return kind;
            }
        }
        return null;
    }

    private boolean inChunk(BlockPos pos, int chunkX, int chunkZ) {
        return pos.getX() >= chunkX && pos.getX() < chunkX + 16
                && pos.getZ() >= chunkZ && pos.getZ() < chunkZ + 16;
    }

    private BlockPos canonicalBase(WorldGenLevel level, BlockPos base, FairyKind kind, int chunkX, int chunkZ) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = base.relative(direction);
            if (!inChunk(neighbor, chunkX, chunkZ) && (!level.hasChunkAt(neighbor)
                    || trunkKind(level, neighbor) == kind
                        && level.getBlockState(neighbor.below()).is(BlockTags.DIRT))) {
                return null;
            }
        }
        for (int dx = -1; dx <= 0; dx++) {
            for (int dz = -1; dz <= 0; dz++) {
                BlockPos corner = base.offset(dx, 0, dz);
                if (square(level, corner, kind, chunkX, chunkZ)) {
                    return corner;
                }
            }
        }
        return base;
    }

    private boolean square(WorldGenLevel level, BlockPos corner, FairyKind kind, int chunkX, int chunkZ) {
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                BlockPos pos = corner.offset(dx, 0, dz);
                if (!inChunk(pos, chunkX, chunkZ) || trunkKind(level, pos) != kind
                        || !level.getBlockState(pos.below()).is(BlockTags.DIRT)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasCanopy(WorldGenLevel level, BlockPos base, FairyKind kind) {
        for (int y = 1; y <= 64 && base.getY() + y <= level.getMaxY(); y++) {
            BlockState state = level.getBlockState(base.above(y));
            if (state.getBlock() instanceof FairyHollowBlock) {
                return false;
            }
            if (state.is(kind.leaves())) {
                return true;
            }
        }
        return false;
    }

    private boolean placeInTrunk(
            WorldGenLevel level, BlockPos base, FairyKind kind, RandomSource random, int chunkX, int chunkZ
    ) {
        int width = square(level, base, kind, chunkX, chunkZ) ? 2 : 1;
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < width; dz++) {
                for (int y = 1; y <= 3; y++) {
                    if (level.getBlockState(base.offset(dx, y, dz)).getBlock() instanceof FairyHollowBlock) {
                        return false;
                    }
                }
            }
        }
        BlockPos pos = base.offset(random.nextInt(width), 1 + random.nextInt(2), random.nextInt(width));
        if (!level.getBlockState(pos).is(kind.log())) {
            return false;
        }
        int start = random.nextInt(4);
        for (int i = 0; i < 4; i++) {
            Direction facing = Direction.from2DDataValue((start + i) % 4);
            BlockPos entry = pos.relative(facing);
            if (!inChunk(entry, chunkX, chunkZ) || !level.getBlockState(entry).isAir()) {
                continue;
            }
            BlockState state = ModHollows.hollow(kind).defaultBlockState().setValue(FairyHollowBlock.FACING, facing);
            if (level.setBlock(pos, state, 2)
                    && level.getBlockEntity(pos) instanceof FairyHollowBlockEntity hollow) {
                if (random.nextBoolean()) {
                    hollow.prepareNaturalResident();
                }
                return true;
            }
        }
        return false;
    }
}
