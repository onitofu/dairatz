package ru.nyansus.mc.hollow;

import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import ru.nyansus.mc.entity.DairatzEntity;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.ModHollows;

public final class FairyHollowClientTest implements FabricClientGameTest {
    private static final BlockPos HOME = new BlockPos(0, 66, 0);

    @Override
    public void runTest(ClientGameTestContext context) {
        TestWorldSave save;
        UUID resident;
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            save = world.getWorldSave();
            world.getServer().runOnServer(server -> {
                ServerLevel level = server.overworld();
                level.setDayTime(13000);
                for (int x = -5; x <= 5; x++) {
                    for (int z = -5; z <= 5; z++) {
                        level.setBlock(new BlockPos(x, 64, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                    }
                }
                for (int y = 65; y <= 69; y++) {
                    level.setBlock(new BlockPos(0, y, 0), Blocks.OAK_LOG.defaultBlockState(), 3);
                }
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        level.setBlock(new BlockPos(x, 70, z), Blocks.OAK_LEAVES.defaultBlockState(), 3);
                    }
                }
                level.setBlock(HOME, ModHollows.oakHollow().defaultBlockState(), 3);
                level.setBlock(new BlockPos(-2, 65, -2), Blocks.GLOWSTONE.defaultBlockState(), 3);
                level.setBlock(new BlockPos(2, 65, -2), Blocks.POPPY.defaultBlockState(), 3);
            });
            world.getServer().runCommand("gamemode spectator @a");
            world.getServer().runCommand("tp @a 0.5 65.5 -3.5 0 12");
            context.runOnClient(client -> client.options.hideGui = true);
            world.getClientWorld().waitForChunksRender();
            context.takeScreenshot("oak-hollow-empty");
            resident = world.getServer().computeOnServer(server -> {
                ServerLevel level = server.overworld();
                FairyHollowBlockEntity hollow = (FairyHollowBlockEntity) level.getBlockEntity(HOME);
                ServerPlayer owner = server.getPlayerList().getPlayers().getFirst();
                DairatzEntity fairy = ModEntities.dairatzEntity().get().create(level, EntitySpawnReason.COMMAND);
                fairy.tame(owner);
                fairy.setNoAi(true);
                fairy.setHealth(7);
                fairy.setCustomName(Component.literal("Oak caretaker"));
                fairy.setPos(hollow.entrance());
                level.addFreshEntity(fairy);
                if (!hollow.enter(fairy)) {
                    throw new AssertionError("The client test fairy could not enter its hollow");
                }
                return fairy.getUUID();
            });
            context.waitFor(client -> client.level.getBlockState(HOME).getValue(FairyHollowBlock.OCCUPIED));
            world.getClientWorld().waitForChunksRender();
            context.takeScreenshot("oak-hollow-occupied");
        }
        try (TestSingleplayerContext world = save.open()) {
            world.getServer().runOnServer(server -> {
                FairyHollowBlockEntity hollow = (FairyHollowBlockEntity) server.overworld().getBlockEntity(HOME);
                if (hollow == null || !hollow.isInside() || !hollow.isResident(resident)) {
                    throw new AssertionError("A real world restart lost the stored resident");
                }
                DairatzEntity fairy = hollow.letResidentOut().fairy();
                ServerPlayer owner = server.getPlayerList().getPlayers().getFirst();
                verifyResident(fairy, owner, resident);
                server.overworld().setDayTime(1000);
            });
            context.waitFor(client -> !client.level.getBlockState(HOME).getValue(FairyHollowBlock.OCCUPIED));
            context.waitTicks(30);
        }
        try (TestSingleplayerContext world = save.open()) {
            world.getServer().runOnServer(server -> {
                DairatzEntity fairy = (DairatzEntity) server.overworld().getEntity(resident);
                verifyResident(fairy, server.getPlayerList().getPlayers().getFirst(), resident);
                FairyHollowBlockEntity hollow = (FairyHollowBlockEntity) server.overworld().getBlockEntity(HOME);
                if (hollow == null || hollow.isInside() || !hollow.isResident(resident)) {
                    throw new AssertionError("A real world restart lost the outside reservation");
                }
            });
        }
        verifyNaturalGeneration(context);
    }

    private static void verifyNaturalGeneration(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(false)
                .adjustSettings(settings -> {
                    settings.setSeed("0");
                    settings.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
                    settings.setAllowCommands(true);
                }).create()) {
            BlockPos found = world.getServer().computeOnServer(server -> {
                ServerLevel level = server.overworld();
                BlockPos center = server.getPlayerList().getPlayers().getFirst().blockPosition();
                int oakLogs = 0;
                int enabledBiomes = 0;
                for (int cx = (center.getX() >> 4) - 4; cx <= (center.getX() >> 4) + 4; cx++) {
                    for (int cz = (center.getZ() >> 4) - 4; cz <= (center.getZ() >> 4) + 4; cz++) {
                        level.getChunk(cx, cz);
                        if (level.getBiome(new BlockPos(cx * 16 + 8, center.getY(), cz * 16 + 8))
                                .value().getGenerationSettings().features().stream()
                                .flatMap(set -> set.stream())
                                .anyMatch(holder -> holder.is(ModHollows.OAK_HOLLOW_PLACED))) {
                            enabledBiomes++;
                        }
                        for (int x = cx * 16; x < cx * 16 + 16; x++) {
                            for (int z = cz * 16; z < cz * 16 + 16; z++) {
                                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                                for (int y = top - 1; y >= Math.max(level.getMinY(), top - 32); y--) {
                                    BlockPos pos = new BlockPos(x, y, z);
                                    if (level.getBlockState(pos).is(Blocks.OAK_LOG)) {
                                        oakLogs++;
                                    }
                                    if (level.getBlockState(pos).is(ModHollows.oakHollow())) {
                                        return pos;
                                    }
                                }
                            }
                        }
                    }
                }
                throw new AssertionError("Seed 0 must generate at least one natural oak hollow near spawn; oak logs="
                        + oakLogs + "; biomes with hollow feature=" + enabledBiomes + "; spawn=" + center);
            });
            Direction facing = world.getServer().computeOnServer(server ->
                    server.overworld().getBlockState(found).getValue(FairyHollowBlock.FACING));
            BlockPos camera = found.relative(facing, 4);
            world.getServer().runCommand("gamemode spectator @a");
            world.getServer().runCommand("tp @a " + (camera.getX() + 0.5) + " " + (camera.getY() - 1)
                    + " " + (camera.getZ() + 0.5) + " facing " + (found.getX() + 0.5)
                    + " " + (found.getY() + 0.5) + " " + (found.getZ() + 0.5));
            world.getClientWorld().waitForChunksRender();
            context.takeScreenshot("oak-hollow-natural");
        }
    }

    private static void verifyResident(DairatzEntity fairy, ServerPlayer owner, UUID resident) {
        if (fairy == null || !fairy.getUUID().equals(resident) || !fairy.isOwnedBy(owner)
                || !fairy.hasHome() || fairy.getHealth() != 7
                || !fairy.getName().getString().equals("Oak caretaker")) {
            throw new AssertionError("A real world restart changed the resident's identity or data");
        }
    }
}
