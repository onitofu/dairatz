package ru.nyansus.mc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biomes;
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

		BiomeModifications.addSpawn(
				BiomeSelectors.includeByKey(Biomes.FLOWER_FOREST),
				MobCategory.CREATURE,
				ModEntities.DAIRATZ_ENTITY,
				10, 1, 3
		);

		LOGGER.info("Dairatz mod initialized!");
	}
}
