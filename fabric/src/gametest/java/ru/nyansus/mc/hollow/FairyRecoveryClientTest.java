package ru.nyansus.mc.hollow;

import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import ru.nyansus.mc.registry.ModHollows;
import ru.nyansus.mc.entity.DairatzEntity;

public final class FairyRecoveryClientTest implements FabricClientGameTest {
    private static final BlockPos HOME = new BlockPos(0, -58, 0);

    @Override
    public void runTest(ClientGameTestContext context) {
        TestWorldSave save;
        UUID resident;
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            save = world.getWorldSave();
            resident = world.getServer().computeOnServer(server -> {
                ServerLevel level = server.overworld();
                for (BlockPos pos : BlockPos.betweenClosed(HOME.offset(-2, 0, -2), HOME.offset(2, 2, 2))) {
                    level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
                }
                level.setBlock(HOME, ModHollows.oakHollow().defaultBlockState(), 3);
                FairyHollowBlockEntity hollow = (FairyHollowBlockEntity) level.getBlockEntity(HOME);
                hollow.prepareNaturalResident();
                hollow.interact(server.getPlayerList().getPlayers().getFirst());
                UUID id = hollow.residentId();
                if (id == null || !hollow.isInside()) {
                    throw new AssertionError("Recovery fixture did not create a stored resident");
                }
                level.setBlock(HOME, Blocks.STONE.defaultBlockState(), 3);
                if (!FairyRecoveryJournal.get(server).hasRecovery(id)) {
                    throw new AssertionError("Blocked destruction did not retain a recovery record");
                }
                return id;
            });
        }
        try (TestSingleplayerContext world = save.open()) {
            world.getServer().runOnServer(server -> {
                if (!FairyRecoveryJournal.get(server).hasRecovery(resident)) {
                    throw new AssertionError("Restart lost a pending fairy recovery");
                }
                ServerLevel level = server.overworld();
                for (BlockPos pos : BlockPos.betweenClosed(HOME.offset(-2, 0, -2), HOME.offset(2, 2, 2))) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            });
            for (int tick = 0; tick < 220; tick++) {
                boolean recovered = world.getServer().computeOnServer(server -> {
                    var entity = server.overworld().getEntity(resident);
                    return entity instanceof DairatzEntity fairy
                            && !fairy.hasHome() && !FairyRecoveryJournal.get(server).hasRecovery(resident);
                });
                if (recovered) {
                    return;
                }
                context.waitTick();
            }
            throw new AssertionError("Server ticks did not automatically restore the saved fairy recovery");
        }
    }

}
