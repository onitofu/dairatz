package ru.nyansus.mc.hollow;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import ru.nyansus.mc.entity.FairyKind;
import ru.nyansus.mc.registry.ModHollows;

public final class FairyWorldgenGameTests {
    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:tall_pollination", skyAccess = true)
    public void everyWoodGetsMatchingHollow(GameTestHelper test) {
        ServerLevel level = test.getLevel();
        BlockPos base = center(test);
        FairyHollowFeature feature = new FairyHollowFeature();
        for (FairyKind kind : FairyKind.values()) {
            if (kind == FairyKind.ENDER) {
                continue;
            }
            tree(level, base, kind, 1, 6);
            boolean placed = false;
            for (int seed = 0; seed < 128 && !placed; seed++) {
                placed = feature.place(NoneFeatureConfiguration.INSTANCE, level,
                        level.getChunkSource().getGenerator(), RandomSource.create(seed), base);
            }
            test.assertTrue(placed, "Matching natural-looking tree must receive a hollow: " + kind);
            test.assertTrue(count(level, base, kind, 1) == 1, "A tree must get one matching hollow: " + kind);
            clear(level, base, 1, 6);
        }
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:tall_pollination", skyAccess = true)
    public void tallDoubleTrunkGetsOnlyOneHollow(GameTestHelper test) {
        ServerLevel level = test.getLevel();
        BlockPos base = center(test);
        tree(level, base, FairyKind.JUNGLE, 2, 40);
        FairyHollowFeature feature = new FairyHollowFeature();
        for (int seed = 0; seed < 128; seed++) {
            feature.place(NoneFeatureConfiguration.INSTANCE, level,
                    level.getChunkSource().getGenerator(), RandomSource.create(seed), base);
        }
        test.assertTrue(count(level, base, FairyKind.JUNGLE, 2) == 1,
                "Repeated generation must create only one hollow in a tall 2x2 jungle trunk");
        test.succeed();
    }

    private static BlockPos center(GameTestHelper test) {
        BlockPos origin = test.absolutePos(new BlockPos(10, 3, 10));
        return new BlockPos((origin.getX() & ~15) + 8, origin.getY(), (origin.getZ() & ~15) + 8);
    }

    private static void tree(ServerLevel level, BlockPos base, FairyKind kind, int width, int height) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                level.setBlock(base.offset(x, -1, z), Blocks.DIRT.defaultBlockState(), 3);
                for (int y = 0; y < height; y++) {
                    level.setBlock(base.offset(x, y, z), kind.log().defaultBlockState(), 3);
                }
                level.setBlock(base.offset(x, height, z), kind.leaves().defaultBlockState(), 3);
            }
        }
    }

    private static int count(ServerLevel level, BlockPos base, FairyKind kind, int width) {
        int count = 0;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                for (int y = 1; y <= 3; y++) {
                    if (level.getBlockState(base.offset(x, y, z)).is(ModHollows.hollow(kind))) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static void clear(ServerLevel level, BlockPos base, int width, int height) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                for (int y = -1; y <= height; y++) {
                    level.setBlock(base.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }
}
