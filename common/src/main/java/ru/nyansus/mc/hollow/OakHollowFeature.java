package ru.nyansus.mc.hollow;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import ru.nyansus.mc.registry.ModHollows;

public final class OakHollowFeature extends Feature<NoneFeatureConfiguration> {
    public OakHollowFeature() {
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
                int bottom = Math.max(level.getMinY(), top - 32);
                for (int y = top - 1; y >= bottom; y--) {
                    BlockPos base = new BlockPos(x, y, z);
                    if (isTrunkBase(level, base)) {
                        if (random.nextInt(20) == 0) {
                            placed |= placeInTrunk(level, base, random);
                        }
                        break;
                    }
                }
            }
        }
        return placed;
    }

    private boolean isTrunkBase(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.OAK_LOG) || state.getValue(RotatedPillarBlock.AXIS) != Direction.Axis.Y
                || !level.getBlockState(pos.below()).is(BlockTags.DIRT)) {
            return false;
        }
        for (int y = 1; y <= 3; y++) {
            if (level.getBlockState(pos.above(y)).is(ModHollows.oakHollow())) {
                return false;
            }
        }
        boolean leaves = false;
        for (int y = 1; y <= 16; y++) {
            if (level.getBlockState(pos.above(y)).is(Blocks.OAK_LEAVES)) {
                leaves = true;
                break;
            }
        }
        return leaves;
    }

    private boolean placeInTrunk(WorldGenLevel level, BlockPos base, RandomSource random) {
        BlockPos pos = base.above(1 + random.nextInt(2));
        if (!level.getBlockState(pos).is(Blocks.OAK_LOG)) {
            return false;
        }
        int start = random.nextInt(4);
        for (int i = 0; i < 4; i++) {
            Direction facing = Direction.from2DDataValue((start + i) % 4);
            BlockPos entry = pos.relative(facing);
            if ((entry.getX() >> 4) != (pos.getX() >> 4) || (entry.getZ() >> 4) != (pos.getZ() >> 4)
                    || !level.getBlockState(entry).isAir()) {
                continue;
            }
            BlockState state = ModHollows.oakHollow().defaultBlockState()
                    .setValue(FairyHollowBlock.FACING, facing);
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
