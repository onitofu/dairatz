package ru.nyansus.mc.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import ru.nyansus.mc.Dairatz;
import ru.nyansus.mc.config.DairatzConfig;
import ru.nyansus.mc.entity.AbstractFairyEntity;
import ru.nyansus.mc.entity.DairatzEntity;
import ru.nyansus.mc.entity.WinterFairyEntity;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.ModItems;

public final class DairatzFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Dairatz.initialize(
                new FabricContentRegistrar(),
                FabricLoader.getInstance().getConfigDir()
        );

        registerAttributes();
        registerSpawnPlacements();
        registerBiomeSpawns();
        registerCreativeTabs();

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
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(
                        Biomes.FLOWER_FOREST, Biomes.MEADOW, Biomes.SUNFLOWER_PLAINS),
                MobCategory.CREATURE,
                ModEntities.dairatzEntity().get(),
                DairatzConfig.dairatzSpawnWeight,
                DairatzConfig.dairatzMinGroup,
                DairatzConfig.dairatzMaxGroup
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(
                        Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA,
                        Biomes.FROZEN_RIVER, Biomes.SNOWY_BEACH,
                        Biomes.GROVE, Biomes.FROZEN_PEAKS,
                        Biomes.ICE_SPIKES),
                MobCategory.CREATURE,
                ModEntities.winterFairy().get(),
                DairatzConfig.winterSpawnWeight,
                DairatzConfig.winterMinGroup,
                DairatzConfig.winterMaxGroup
        );
    }

    private static void registerCreativeTabs() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS)
                .register(entries -> {
                    entries.accept(new ItemStack(ModItems.dairatzSpawnEgg().get()));
                    entries.accept(new ItemStack(ModItems.winterFairySpawnEgg().get()));
                });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register(entries -> entries.accept(new ItemStack(ModItems.furball().get())));
    }
}
