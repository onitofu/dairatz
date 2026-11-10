package ru.nyansus.mc.entity.ai;

import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import ru.nyansus.mc.entity.DairatzEntity;
import ru.nyansus.mc.registry.ModEntities;

public final class FairyPollinationClientTest implements FabricClientGameTest {
    private static final BlockPos FLOWER = new BlockPos(0, 65, 0);
    private static final BlockPos CROP = new BlockPos(4, 65, 0);

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            UUID id = world.getServer().computeOnServer(server -> {
                ServerLevel level = server.overworld();
                level.setDayTime(1000);
                for (int x = -3; x <= 7; x++) {
                    for (int z = -3; z <= 3; z++) {
                        level.setBlock(new BlockPos(x, 64, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                    }
                }
                level.setBlock(FLOWER, Blocks.POPPY.defaultBlockState(), 3);
                level.setBlock(CROP.below(), Blocks.FARMLAND.defaultBlockState(), 3);
                level.setBlock(CROP, Blocks.WHEAT.defaultBlockState(), 3);
                DairatzEntity fairy = ModEntities.dairatzEntity().get().create(level, EntitySpawnReason.COMMAND);
                fairy.setNoAi(true);
                fairy.setPos(FLOWER.getX() + 0.5, FLOWER.getY() + 1, FLOWER.getZ() + 0.5);
                level.addFreshEntity(fairy);
                return fairy.getUUID();
            });
            world.getServer().runCommand("gamemode spectator @a");
            world.getServer().runCommand("tp @a 2.5 67 -4.5 0 25");
            world.getClientWorld().waitForChunksRender();
            context.runOnClient(client -> {
                client.options.hideGui = true;
                client.particleEngine.clearParticles();
            });
            world.getServer().runOnServer(server -> {
                DairatzEntity fairy = (DairatzEntity) server.overworld().getEntity(id);
                FairyPollinateGoal goal = begin(fairy);
                for (int i = 0; i < 60; i++) {
                    goal.tick();
                }
            });
            context.waitFor(client -> Integer.parseInt(client.particleEngine.countParticles()) > 0);
            context.takeScreenshot("fairy-gathering-pollen");
            context.runOnClient(client -> client.particleEngine.clearParticles());
            world.getServer().runOnServer(server -> {
                DairatzEntity fairy = (DairatzEntity) server.overworld().getEntity(id);
                FairyPollinateGoal goal = begin(fairy);
                for (int i = 0; i < 60; i++) {
                    goal.tick();
                }
                fairy.setPos(CROP.getX() + 0.5, CROP.getY() + 1, CROP.getZ() + 0.5);
                for (int i = 0; i < 20; i++) {
                    goal.tick();
                }
                if (server.overworld().getBlockState(CROP).getValue(CropBlock.AGE) == 0) {
                    throw new AssertionError("Client fixture crop must grow through pollination");
                }
                goal.stop();
            });
            context.waitFor(client -> client.level.getBlockState(CROP).getValue(CropBlock.AGE) > 0
                    && Integer.parseInt(client.particleEngine.countParticles()) > 0);
            context.takeScreenshot("fairy-pollinated-crop");
        }
    }

    private static FairyPollinateGoal begin(DairatzEntity fairy) {
        FairyPollinateGoal goal = new FairyPollinateGoal(fairy);
        if (!goal.canUse()) {
            throw new AssertionError("Client fixture must provide a flower and a growing crop");
        }
        goal.start();
        return goal;
    }
}
