package ru.nyansus.mc.config;

import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import ru.nyansus.mc.entity.DairatzEntity;
import ru.nyansus.mc.registry.ModEntities;

public final class DairatzConfigGameTests {
    @GameTest
    public void invalidSnapshotsKeepPreviousSettingsAndFile(GameTestHelper test) throws Exception {
        Path path = Files.createTempFile("dairatz-config-test", ".json");
        try {
            Files.writeString(path, "{\"tameChance\":1,\"healAmount\":6}");
            test.assertTrue(DairatzConfig.load(path), "Valid settings must load");
            DairatzSettings previous = DairatzConfig.current();
            for (String json : new String[]{
                "{\"tameChance\":0}", "{\"winterFreezeRadius\":9}",
                "{\"dairatzMinGroup\":6,\"dairatzMaxGroup\":2}",
                "{\"winterMinGroup\":0}", "{\"winterMaxGroup\":65}",
                "{\"dairatzSpawnWeight\":2147483647}", "{\"winterSpawnWeight\":-1}",
                "{\"fairyHealth\":0}", "{\"flySpeed\":-1}", "{\"furballDamage\":1e400}",
                "{\"fireRate\":0}", "{\"winterSlownessLevel\":257}",
                "{\"tameChance\":null}", "[]", "{broken", "null"
            }) {
                Files.writeString(path, json);
                test.assertTrue(!DairatzConfig.load(path), "Invalid settings must be rejected: " + json);
                test.assertTrue(DairatzConfig.current() == previous, "Rejected snapshots must not replace settings");
                test.assertTrue(Files.readString(path).equals(json), "Rejected files must remain unchanged");
            }
            test.succeed();
        } finally {
            restoreConfiguration();
            Files.deleteIfExists(path);
        }
    }

    @GameTest
    public void partialSettingsUseDefaultsAndSupportDisabledFeatures(GameTestHelper test) throws Exception {
        Path path = Files.createTempFile("dairatz-config-test", ".json");
        try {
            Files.writeString(path, "{\"tameChance\":1,\"winterFreezeRadius\":0,\"dairatzSpawnWeight\":0,"
                    + "\"futureOption\":true}");
            test.assertTrue(DairatzConfig.load(path), "Partial settings with unknown fields must load");
            test.assertTrue(DairatzConfig.current().tameChance() == 1
                    && DairatzConfig.current().winterFreezeRadius() == 0
                    && DairatzConfig.current().dairatzSpawnWeight() == 0,
                    "Supported zero settings must disable optional features");
            test.assertTrue(DairatzConfig.current().fairyHealth() == DairatzSettings.DEFAULTS.fairyHealth(),
                    "Omitted fields must retain defaults");
            DairatzConfig.save();
            test.assertTrue(DairatzConfig.load(path), "Atomically saved settings must load again");
            test.succeed();
        } finally {
            restoreConfiguration();
            Files.deleteIfExists(path);
        }
    }

    @GameTest
    public void zeroTameChanceCannotBreakFeeding(GameTestHelper test) throws Exception {
        Path path = Files.createTempFile("dairatz-config-test", ".json");
        try {
            Files.writeString(path, "{\"tameChance\":0}");
            test.assertTrue(!DairatzConfig.load(path), "Zero tame denominator must be rejected");
            DairatzEntity fairy = test.spawn(ModEntities.dairatzEntity().get(), new BlockPos(3, 3, 3));
            ServerPlayer player = test.makeMockServerPlayerInLevel();
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.POPPY));
            fairy.mobInteract(player, InteractionHand.MAIN_HAND);
            test.assertTrue(DairatzConfig.current().tameChance() > 0, "Feeding must use safe settings");
            test.succeed();
        } finally {
            restoreConfiguration();
            Files.deleteIfExists(path);
        }
    }

    private static void restoreConfiguration() {
        DairatzConfig.load(FabricLoader.getInstance().getConfigDir().resolve("dairatz.json"));
    }
}
