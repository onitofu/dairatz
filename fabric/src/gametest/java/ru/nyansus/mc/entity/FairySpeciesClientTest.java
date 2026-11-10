package ru.nyansus.mc.entity;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import ru.nyansus.mc.hollow.FairyHollowBlock;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.ModHollows;
import ru.nyansus.mc.renderer.PaleClientState;

public final class FairySpeciesClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getServer().runOnServer(server -> {
                ServerLevel level = server.overworld();
                level.setDayTime(13000);
                for (int x = -4; x <= 18; x++) {
                    for (int z = -8; z <= 8; z++) {
                        level.setBlock(new BlockPos(x, 64, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                    }
                }
                int x = 0;
                for (FairyKind kind : ModEntities.newKinds()) {
                    for (int appearance = 0; appearance < (kind.hasVariants() ? 2 : 1); appearance++) {
                        NewFairyEntity fairy = ModEntities.fairy(kind).create(level, EntitySpawnReason.COMMAND);
                        fairy.setNoAi(true);
                        fairy.setNoGravity(true);
                        fairy.setAppearance(appearance);
                        fairy.setPos(x + 0.5, 66, 3.5);
                        level.addFreshEntity(fairy);
                        x += 2;
                    }
                }
                x = 0;
                for (FairyKind kind : FairyKind.values()) {
                    if (kind == FairyKind.ENDER) {
                        continue;
                    }
                    level.setBlock(new BlockPos(x++, 65, 0), ModHollows.hollow(kind).defaultBlockState(), 3);
                    level.setBlock(new BlockPos(x++, 65, 0), ModHollows.hollow(kind).defaultBlockState()
                            .setValue(FairyHollowBlock.OCCUPIED, true), 3);
                }
            });
            world.getServer().runCommand("gamemode spectator @a");
            world.getServer().runCommand("tp @a 5.5 67 -5.5 0 12");
            context.runOnClient(client -> client.options.hideGui = true);
            world.getClientWorld().waitForChunksRender();
            context.takeScreenshot("fairy-species-and-hollows");
            int target = world.getServer().computeOnServer(server -> {
                ServerLevel level = server.overworld();
                var owner = server.getPlayerList().getPlayers().getFirst();
                NewFairyEntity pale = ModEntities.fairy(FairyKind.PALE).create(level, EntitySpawnReason.COMMAND);
                pale.setNoAi(true);
                pale.tame(owner);
                pale.setOnHead(true);
                pale.setPos(owner.position());
                level.addFreshEntity(pale);
                for (int x = 4; x <= 6; x++) {
                    for (int y = 65; y <= 68; y++) {
                        level.setBlock(new BlockPos(x, y, -2), Blocks.STONE.defaultBlockState(), 3);
                    }
                }
                var zombie = EntityType.ZOMBIE.create(level, EntitySpawnReason.COMMAND);
                zombie.setNoAi(true);
                zombie.setNoGravity(true);
                zombie.setPos(5.5, 65, 0.5);
                level.addFreshEntity(zombie);
                return zombie.getId();
            });
            context.waitFor(client -> client.level.getEntity(target) != null
                    && client.level.getEntity(target).isCurrentlyGlowing());
            context.takeScreenshot("pale-private-detection-through-wall");
            world.getServer().runOnServer(server -> server.overworld().getEntitiesOfClass(
                    NewFairyEntity.class, server.getPlayerList().getPlayers().getFirst().getBoundingBox().inflate(3),
                    fairy -> fairy.isOnHead()).forEach(fairy -> fairy.setOnHead(false)));
            context.waitFor(client -> client.level.getEntity(target) != null
                    && !client.level.getEntity(target).isCurrentlyGlowing());
            context.runOnClient(client -> {
                if (PaleClientState.detected(client.level.getEntity(target))) {
                    throw new AssertionError("Removing the head fairy must clear private detection");
                }
            });
        }
        context.runOnClient(client -> {
            PaleClientState.clear();
            if (client.level != null) {
                throw new AssertionError("Test world must be disconnected");
            }
        });
    }
}
