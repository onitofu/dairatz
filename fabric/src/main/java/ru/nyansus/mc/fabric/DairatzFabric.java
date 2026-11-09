package ru.nyansus.mc.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import ru.nyansus.mc.Dairatz;
import ru.nyansus.mc.config.DairatzConfig;
import ru.nyansus.mc.entity.AbstractFairyEntity;
import ru.nyansus.mc.entity.DairatzEntity;
import ru.nyansus.mc.entity.WinterFairyEntity;
import ru.nyansus.mc.hollow.FairyRecoveryJournal;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.ModHollows;
import ru.nyansus.mc.registry.ModItems;

public final class DairatzFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Dairatz.initialize(
                new FabricContentRegistrar(),
                FabricLoader.getInstance().getConfigDir()
        );

        ServerTickEvents.END_SERVER_TICK.register(FairyRecoveryJournal::tick);
        registerAttributes();
        registerSpawnPlacements();
        registerBiomeSpawns();
        registerCreativeTabs();
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION, ModHollows.OAK_HOLLOW_PLACED);

        Dairatz.LOGGER.info("Dairatz Fabric initialized");
    }

    private static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(
                ModEntities.dairatzEntity().get(), DairatzEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(
                ModEntities.winterFairy().get(), WinterFairyEntity.createAttributes());
    }

    private static void registerSpawnPlacements() {
        SpawnPlacements.register(
                ModEntities.dairatzEntity().get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbstractFairyEntity::checkFairySpawnRules
        );
        SpawnPlacements.register(
                ModEntities.winterFairy().get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbstractFairyEntity::checkFairySpawnRules
        );
    }

    private static void registerBiomeSpawns() {
        if (DairatzConfig.current().dairatzSpawnWeight() > 0) {
            BiomeModifications.addSpawn(
                    BiomeSelectors.includeByKey(
                            Biomes.FLOWER_FOREST, Biomes.MEADOW, Biomes.SUNFLOWER_PLAINS),
                    MobCategory.CREATURE,
                    ModEntities.dairatzEntity().get(),
                    DairatzConfig.current().dairatzSpawnWeight(),
                    DairatzConfig.current().dairatzMinGroup(),
                    DairatzConfig.current().dairatzMaxGroup()
            );
        }
        if (DairatzConfig.current().winterSpawnWeight() > 0) {
            BiomeModifications.addSpawn(
                    BiomeSelectors.includeByKey(
                            Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA,
                            Biomes.FROZEN_RIVER, Biomes.SNOWY_BEACH,
                            Biomes.GROVE, Biomes.FROZEN_PEAKS,
                            Biomes.ICE_SPIKES),
                    MobCategory.CREATURE,
                    ModEntities.winterFairy().get(),
                    DairatzConfig.current().winterSpawnWeight(),
                    DairatzConfig.current().winterMinGroup(),
                    DairatzConfig.current().winterMaxGroup()
            );
        }
    }

    private static void registerCreativeTabs() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS)
                .register(entries -> entries.accept(ModHollows.oakHollowItem()));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS)
                .register(entries -> {
                    entries.accept(new ItemStack(ModItems.dairatzSpawnEgg().get()));
                    entries.accept(new ItemStack(ModItems.winterFairySpawnEgg().get()));
                });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register(entries -> {
                    entries.accept(new ItemStack(ModItems.furball().get()));
                    entries.accept(new ItemStack(ModItems.iceFurball().get()));
                });
    }
}
