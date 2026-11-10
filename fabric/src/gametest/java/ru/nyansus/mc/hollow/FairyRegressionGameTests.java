package ru.nyansus.mc.hollow;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import ru.nyansus.mc.entity.DairatzEntity;
import ru.nyansus.mc.entity.WinterFairyEntity;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.ModHollows;

public final class FairyRegressionGameTests {
    private static final BlockPos HOME = new BlockPos(3, 3, 3);

    @GameTest(environment = "dairatz-test:night", maxTicks = 80)
    public void dimensionChangeMustFreeOldHome(GameTestHelper test) {
        test.setBlock(HOME, ModHollows.oakHollow().defaultBlockState()
                .setValue(FairyHollowBlock.FACING, Direction.NORTH));
        FairyHollowBlockEntity hollow = test.getBlockEntity(HOME, FairyHollowBlockEntity.class);
        DairatzEntity fairy = test.spawn(ModEntities.dairatzEntity().get(), HOME.north());
        fairy.setPos(hollow.entrance());
        fairy.setNoAi(true);
        test.assertTrue(hollow.enter(fairy), "Probe setup failed: cannot settle fairy");
        test.runAfterDelay(2, () -> {
            DairatzEntity outside = (DairatzEntity) hollow.letResidentOut().fairy();
            test.assertTrue(outside != null, "Probe setup failed: cannot release fairy");
            ServerLevel nether = test.getLevel().getServer().getLevel(Level.NETHER);
            DairatzEntity moved = (DairatzEntity) outside.teleport(new TeleportTransition(
                    nether, new Vec3(0.5, 130, 0.5), Vec3.ZERO, 0, 0, TeleportTransition.DO_NOTHING));
            test.assertTrue(moved != null && moved.hasHome(), "Probe setup failed: cannot move resident");
            moved.releaseHome();
            test.assertTrue(!hollow.hasResident(), "Old hollow remains permanently reserved after dimension change");
            test.succeed();
        });
    }

    @GameTest(environment = "dairatz-test:night")
    public void hardTeleportMustAvoidSolidBlocks(GameTestHelper test) {
        ServerPlayer owner = test.makeMockServerPlayerInLevel();
        Vec3 center = test.absoluteVec(new Vec3(3.5, 3, 3.5));
        owner.setPos(center);
        for (int x = 1; x <= 5; x++) {
            for (int z = 1; z <= 5; z++) {
                for (int y = 3; y <= 5; y++) {
                    if (x != 3 || z != 3) {
                        test.setBlock(new BlockPos(x, y, z), Blocks.STONE);
                    }
                }
            }
        }
        DairatzEntity fairy = test.spawn(ModEntities.dairatzEntity().get(), new BlockPos(7, 3, 7));
        fairy.tame(owner);
        fairy.setNoAi(true);
        fairy.setPos(center.add(60, 0, 0));
        fairy.getRandom().setSeed(0);
        fairy.tryTeleportNearOwner(owner);
        test.assertTrue(!test.getLevel().getBlockCollisions(fairy, fairy.getBoundingBox()).iterator().hasNext(),
                "Hard teleport placed fairy inside solid blocks at " + fairy.blockPosition());
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:night")
    public void winterFreezeMustNotLoadNeighborChunk(GameTestHelper test) {
        ServerLevel level = test.getLevel();
        int chunkX = (test.absolutePos(HOME).getX() >> 4) + 32;
        int chunkZ = test.absolutePos(HOME).getZ() >> 4;
        level.getChunk(chunkX, chunkZ);
        BlockPos neighbor = new BlockPos((chunkX + 1) * 16, 130, chunkZ * 16 + 8);
        test.assertTrue(!level.hasChunkAt(neighbor), "Probe setup failed: neighbor already loaded");
        WinterFairyEntity fairy = ModEntities.winterFairy().get().create(level, EntitySpawnReason.COMMAND);
        fairy.setNoAi(true);
        fairy.setPos(chunkX * 16 + 14.5, 130, chunkZ * 16 + 8.5);
        fairy.tickCount = 20;
        fairy.tick();
        test.assertTrue(!level.hasChunkAt(neighbor), "Winter freezing force-loaded a neighboring chunk");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:night")
    public void impossibleTeleportLeavesFairyInPlace(GameTestHelper test) {
        ServerPlayer owner = test.makeMockServerPlayerInLevel();
        Vec3 center = test.absoluteVec(new Vec3(3.5, 3, 3.5));
        owner.setPos(center);
        for (int x = 1; x <= 5; x++) {
            for (int z = 1; z <= 5; z++) {
                for (int y = 3; y <= 5; y++) {
                    test.setBlock(new BlockPos(x, y, z), Blocks.STONE);
                }
            }
        }
        DairatzEntity fairy = test.spawn(ModEntities.dairatzEntity().get(), new BlockPos(7, 3, 7));
        fairy.setPos(center.add(60, 0, 0));
        Vec3 original = fairy.position();
        test.assertTrue(!fairy.tryTeleportNearOwner(owner) && fairy.position().equals(original),
                "No safe candidate must leave the fairy at its original position");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:night")
    public void teleportDoesNotLoadTargetChunks(GameTestHelper test) {
        ServerPlayer owner = test.makeMockServerPlayerInLevel();
        BlockPos target = test.absolutePos(HOME).offset(8192, 0, 0);
        owner.setPos(Vec3.atBottomCenterOf(target));
        test.assertTrue(!test.getLevel().hasChunkAt(target), "Target chunk must start unloaded");
        DairatzEntity fairy = test.spawn(ModEntities.dairatzEntity().get(), HOME);
        test.assertTrue(!fairy.tryTeleportNearOwner(owner) && !test.getLevel().hasChunkAt(target),
                "Teleport must not load target chunks");
        test.succeed();
    }

}
