package ru.nyansus.mc.entity;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Blocks;
import ru.nyansus.mc.registry.ModEntities;

public final class FairyParticlesClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getServer().runOnServer(server -> {
                ServerLevel level = server.overworld();
                level.setDayTime(1000);
                for (int x = -3; x <= 3; x++) {
                    for (int z = -3; z <= 3; z++) {
                        level.setBlock(new BlockPos(x, 64, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                    }
                }
            });
            world.getServer().runCommand("gamemode spectator @a");
            world.getServer().runCommand("tp @a 0.5 65.5 -2.5 0 16");
            context.runOnClient(client -> client.options.hideGui = true);
            world.getClientWorld().waitForChunksRender();
            for (FairyKind kind : FairyKind.values()) {
                int id = world.getServer().computeOnServer(server -> {
                    ServerLevel level = server.overworld();
                    AbstractFairyEntity fairy = switch (kind) {
                        case STANDARD -> ModEntities.dairatzEntity().get().create(level, EntitySpawnReason.COMMAND);
                        case WINTER -> ModEntities.winterFairy().get().create(level, EntitySpawnReason.COMMAND);
                        default -> ModEntities.fairy(kind).create(level, EntitySpawnReason.COMMAND);
                    };
                    fairy.setNoAi(true);
                    fairy.setNoGravity(true);
                    fairy.setPos(0.5, 66, 0.5);
                    level.addFreshEntity(fairy);
                    return fairy.getId();
                });
                context.waitFor(client -> client.level.getEntity(id) != null);
                context.runOnClient(client -> client.particleEngine.clearParticles());
                context.waitFor(client -> Integer.parseInt(client.particleEngine.countParticles()) > 0);
                context.waitTicks(20);
                context.takeScreenshot("fairy-ambient-" + kind.id());
                if (kind == FairyKind.ENDER) {
                    verifyMountedAndInvisible(context, world, id);
                }
                world.getServer().runOnServer(server -> server.overworld().getEntity(id).discard());
                context.waitFor(client -> client.level.getEntity(id) == null);
            }
        }
    }

    private static void verifyMountedAndInvisible(
            ClientGameTestContext context, TestSingleplayerContext world, int id
    ) {
        world.getServer().runOnServer(server -> {
            AbstractFairyEntity fairy = (AbstractFairyEntity) server.overworld().getEntity(id);
            fairy.tame(server.getPlayerList().getPlayers().getFirst());
            fairy.setOnHead(true);
        });
        context.waitFor(client -> ((AbstractFairyEntity) client.level.getEntity(id)).isOnHead());
        context.runOnClient(client -> client.particleEngine.clearParticles());
        context.waitTicks(32);
        context.runOnClient(client -> {
            if (!client.particleEngine.countParticles().equals("0")) {
                throw new AssertionError("A mounted fairy must not emit ambient particles into the camera");
            }
        });
        world.getServer().runOnServer(server -> {
            AbstractFairyEntity fairy = (AbstractFairyEntity) server.overworld().getEntity(id);
            fairy.setOnHead(false);
            fairy.setInvisible(true);
        });
        context.waitFor(client -> client.level.getEntity(id).isInvisible());
        context.runOnClient(client -> client.particleEngine.clearParticles());
        context.waitTicks(32);
        context.runOnClient(client -> {
            if (!client.particleEngine.countParticles().equals("0")) {
                throw new AssertionError("Ambient particles must not reveal an invisible fairy");
            }
        });
    }
}
