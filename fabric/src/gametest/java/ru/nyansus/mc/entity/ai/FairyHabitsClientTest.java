package ru.nyansus.mc.entity.ai;

import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import ru.nyansus.mc.entity.AbstractFairyEntity;
import ru.nyansus.mc.entity.FairyKind;
import ru.nyansus.mc.entity.HousedFairyEntity;
import ru.nyansus.mc.hollow.FairyHollowBlockEntity;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.ModHollows;

public final class FairyHabitsClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            int winter = world.getServer().computeOnServer(server -> {
                ServerLevel level = server.overworld();
                level.setDayTime(1000);
                for (int x = -5; x <= 15; x++) {
                    for (int z = -5; z <= 6; z++) {
                        level.setBlock(new BlockPos(x, 64, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                    }
                }
                level.setBlock(new BlockPos(0, 65, 2), Blocks.ICE.defaultBlockState(), 3);
                var fairy = ModEntities.winterFairy().get().create(level, EntitySpawnReason.COMMAND);
                fairy.setNoGravity(true);
                fairy.setPos(0.5, 66, -0.5);
                level.addFreshEntity(fairy);
                return fairy.getId();
            });
            world.getServer().runCommand("gamemode spectator @a");
            world.getServer().runCommand("tp @a 0.5 66 -2.5 0 18");
            context.runOnClient(client -> client.options.hideGui = true);
            context.waitFor(client -> client.level.getEntity(winter) instanceof AbstractFairyEntity fairy
                    && fairy.isResting() && client.level.getBlockState(fairy.blockPosition().below()).is(Blocks.ICE));
            world.getServer().runOnServer(server -> ((AbstractFairyEntity) server.overworld().getEntity(winter))
                    .setNoAi(true));
            world.getClientWorld().waitForChunksRender();
            context.takeScreenshot("winter-resting-on-ice");
            int pale = world.getServer().computeOnServer(server -> {
                ServerLevel level = server.overworld();
                level.setBlock(new BlockPos(3, 68, 2), Blocks.PALE_OAK_LEAVES.defaultBlockState(), 3);
                var fairy = ModEntities.fairy(FairyKind.PALE).create(level, EntitySpawnReason.COMMAND);
                fairy.setNoGravity(true);
                fairy.setPos(3.5, 66, 0.5);
                level.addFreshEntity(fairy);
                return fairy.getId();
            });
            world.getServer().runCommand("tp @a 3.5 67 -1.5 0 16");
            context.waitFor(client -> client.level.getEntity(pale) instanceof AbstractFairyEntity fairy
                    && fairy.isResting());
            world.getServer().runOnServer(server -> ((AbstractFairyEntity) server.overworld().getEntity(pale))
                    .setNoAi(true));
            world.getClientWorld().waitForChunksRender();
            context.takeScreenshot("pale-resting-under-canopy");
            List<Integer> visitors = world.getServer().computeOnServer(server -> {
                ServerLevel level = server.overworld();
                var owner = server.getPlayerList().getPlayers().getFirst();
                BlockPos home = new BlockPos(6, 65, 1);
                level.setBlock(home, ModHollows.hollow(FairyKind.CHERRY).defaultBlockState(), 3);
                FairyHollowBlockEntity hollow = (FairyHollowBlockEntity) level.getBlockEntity(home);
                HousedFairyEntity incoming = ModEntities.fairy(FairyKind.CHERRY)
                        .create(level, EntitySpawnReason.COMMAND);
                incoming.tame(owner);
                incoming.setNoGravity(true);
                incoming.setPos(hollow.entrance());
                level.addFreshEntity(incoming);
                if (!hollow.enter(incoming)) {
                    throw new AssertionError("Fixture must settle Cherry fairy");
                }
                HousedFairyEntity fairy = hollow.letResidentOut().fairy();
                if (fairy == null) {
                    throw new AssertionError("Fixture must release Cherry resident");
                }
                var wolf = EntityType.WOLF.create(level, EntitySpawnReason.COMMAND);
                wolf.setNoAi(true);
                wolf.tame(owner);
                wolf.setHealth(4);
                wolf.setPos(11.5, 65, 1.5);
                level.addFreshEntity(wolf);
                return List.of(fairy.getId(), wolf.getId());
            });
            world.getServer().runCommand("tp @a 9.5 66 -3.5 0 20");
            context.waitFor(client -> client.level.getEntity(visitors.get(1)) instanceof LivingEntity pet
                    && pet.getHealth() > 4);
            world.getServer().runOnServer(server -> {
                AbstractFairyEntity fairy = (AbstractFairyEntity) server.overworld().getEntity(visitors.getFirst());
                LivingEntity pet = (LivingEntity) server.overworld().getEntity(visitors.get(1));
                if (fairy.distanceToSqr(pet) > 6.25) {
                    throw new AssertionError("Cherry fairy must approach patient before healing");
                }
                fairy.setNoAi(true);
            });
            context.takeScreenshot("cherry-visiting-patient");
        }
    }
}
