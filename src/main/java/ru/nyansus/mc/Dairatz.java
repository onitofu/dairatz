package ru.nyansus.mc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nyansus.mc.config.DairatzConfig;
import ru.nyansus.mc.entity.DairatzEntity;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.ModItems;

public class Dairatz implements ModInitializer {
	public static final String MOD_ID = "dairatz";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		DairatzConfig.load();

		ModEntities.register();
		ModItems.register();

		FabricDefaultAttributeRegistry.register(ModEntities.DAIRATZ_ENTITY, DairatzEntity.createAttributes());

		SpawnPlacements.register(
				ModEntities.DAIRATZ_ENTITY,
				SpawnPlacementTypes.NO_RESTRICTIONS,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				DairatzEntity::checkFairySpawnRules
		);

		BiomeModifications.addSpawn(
				BiomeSelectors.includeByKey(Biomes.FLOWER_FOREST, Biomes.MEADOW, Biomes.SUNFLOWER_PLAINS),
				MobCategory.CREATURE,
				ModEntities.DAIRATZ_ENTITY,
				10, 2, 4
		);

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
			entries.accept(new ItemStack(ModItems.DAIRATZ_SPAWN_EGG));
		});
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
			entries.accept(new ItemStack(ModItems.FURBALL));
		});

		LOGGER.info("Dairatz mod initialized!");
	}
}
