package ru.nyansus.mc.entity.ai;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.CocoaBlock;
import ru.nyansus.mc.entity.FairyKind;
import ru.nyansus.mc.entity.NewFairyEntity;
import ru.nyansus.mc.entity.DairatzEntity;
import ru.nyansus.mc.registry.ModEntities;

public final class FairyPollinationGameTests {
    private static final BlockPos FLOWER = new BlockPos(9, 2, 10);
    private static final BlockPos CROP = new BlockPos(14, 2, 10);

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void pollenReachesDistantCropWithoutGrowingGrass(GameTestHelper test) {
        DairatzEntity fairy = prepare(test);
        FairyPollinateGoal goal = new FairyPollinateGoal(fairy);
        test.assertTrue(goal.canUse(), "A single flower must reliably supply pollen for a distant crop");
        goal.start();
        work(goal);
        test.assertTrue(test.getBlockState(CROP).getValue(CropBlock.AGE) == 0,
                "Gathering pollen must not grow a distant crop before the fairy visits it");
        fairy.setPos(test.absolutePos(CROP).getX() + 0.5,
                test.absolutePos(CROP).getY() + 1, test.absolutePos(CROP).getZ() + 0.5);
        work(goal);
        test.assertTrue(test.getBlockState(CROP).getValue(CropBlock.AGE) >= 2,
                "Visiting a crop with pollen must produce a visible bonemeal growth step");
        for (int x = 8; x <= 15; x++) {
            for (int z = 8; z <= 14; z++) {
                BlockPos pos = new BlockPos(x, 2, z);
                if (!pos.equals(FLOWER) && !pos.equals(CROP)) {
                    test.assertTrue(test.getBlockState(pos).isAir(), "Pollination must not spread grass at " + pos);
                }
            }
        }
        test.assertTrue(test.getBlockState(FLOWER).is(Blocks.POPPY), "The pollen flower must remain unchanged");
        test.assertTrue(!goal.canUse(), "Successful work must have a cooldown");
        goal.stop();
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void flowerOnlyGardenDoesNotGrowVegetation(GameTestHelper test) {
        DairatzEntity fairy = prepare(test);
        test.setBlock(CROP, Blocks.AIR);
        FairyPollinateGoal goal = new FairyPollinateGoal(fairy);
        test.assertTrue(!goal.canUse(), "A flower without a growing crop must not trigger bonemeal on grass");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void ripeCropDoesNotWastePollen(GameTestHelper test) {
        DairatzEntity fairy = prepare(test);
        test.setBlock(CROP, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, CropBlock.MAX_AGE));
        FairyPollinateGoal goal = new FairyPollinateGoal(fairy);
        test.assertTrue(!goal.canUse(), "A fully grown crop must not trigger another pollination cycle");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void cropsStillRequireFlowers(GameTestHelper test) {
        DairatzEntity fairy = prepare(test);
        test.setBlock(FLOWER, Blocks.AIR);
        FairyPollinateGoal goal = new FairyPollinateGoal(fairy);
        test.assertTrue(!goal.canUse(), "A crop without a flower must not grow by fairy pollination");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void harvestedCropCancelsPollination(GameTestHelper test) {
        DairatzEntity fairy = prepare(test);
        FairyPollinateGoal goal = new FairyPollinateGoal(fairy);
        test.assertTrue(goal.canUse(), "The garden must initially be workable");
        goal.start();
        work(goal);
        test.setBlock(CROP, Blocks.SHORT_GRASS);
        test.assertTrue(!goal.canContinueToUse(), "Replacing a crop with grass must cancel the pending effect");
        goal.stop();
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void existingGrassAndFlowersAreNotTargets(GameTestHelper test) {
        DairatzEntity fairy = prepare(test);
        test.setBlock(CROP, Blocks.SHORT_GRASS);
        test.setBlock(new BlockPos(10, 2, 10), Blocks.DANDELION);
        FairyPollinateGoal goal = new FairyPollinateGoal(fairy);
        test.assertTrue(!goal.canUse(), "Grass and decorative flowers must never be growth targets");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void junglePollenGrowsCocoaInsteadOfFieldCrops(GameTestHelper test) {
        DairatzEntity ordinary = prepare(test);
        ordinary.discard();
        test.setBlock(CROP.east(), Blocks.JUNGLE_LOG);
        test.setBlock(CROP, Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.FACING, Direction.EAST));
        BlockPos wheat = FLOWER.south();
        test.setBlock(wheat.below(), Blocks.FARMLAND);
        test.setBlock(wheat, Blocks.WHEAT);
        NewFairyEntity fairy = test.spawn(ModEntities.fairy(FairyKind.JUNGLE), FLOWER.above());
        fairy.setNoAi(true);
        BlockPos flower = test.absolutePos(FLOWER);
        fairy.setPos(flower.getX() + 0.5, flower.getY() + 1, flower.getZ() + 0.5);
        FairyPollinateGoal goal = new FairyPollinateGoal(fairy);
        test.assertTrue(goal.canUse(), "Jungle fairy must find flower and cocoa");
        goal.start();
        work(goal);
        BlockPos cocoa = test.absolutePos(CROP);
        fairy.setPos(cocoa.getX() + 0.5, cocoa.getY() + 1, cocoa.getZ() + 0.5);
        work(goal);
        test.assertTrue(test.getBlockState(CROP).getValue(CocoaBlock.AGE) > 0,
                "Jungle pollination must grow cocoa");
        test.assertTrue(test.getBlockState(wheat).getValue(CropBlock.AGE) == 0,
                "Jungle pollination must leave ordinary field crops to the standard fairy");
        goal.stop();
        test.succeed();
    }

    private static DairatzEntity prepare(GameTestHelper test) {
        for (int x = 8; x <= 15; x++) {
            for (int z = 8; z <= 14; z++) {
                test.setBlock(new BlockPos(x, 1, z), Blocks.GRASS_BLOCK);
                test.setBlock(new BlockPos(x, 2, z), Blocks.AIR);
            }
        }
        test.setBlock(FLOWER, Blocks.POPPY);
        test.setBlock(CROP.below(), Blocks.FARMLAND);
        test.setBlock(CROP, Blocks.WHEAT);
        DairatzEntity fairy = test.spawn(ModEntities.dairatzEntity().get(), FLOWER.above());
        fairy.setNoAi(true);
        BlockPos flower = test.absolutePos(FLOWER);
        fairy.setPos(flower.getX() + 0.5, flower.getY() + 1, flower.getZ() + 0.5);
        return fairy;
    }

    private static void work(FairyPollinateGoal goal) {
        for (int i = 0; i < 120 && goal.canContinueToUse(); i++) {
            goal.tick();
        }
    }
}
