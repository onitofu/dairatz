package ru.nyansus.mc.forge;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.nyansus.mc.Dairatz;
import ru.nyansus.mc.entity.AbstractFairyEntity;
import ru.nyansus.mc.entity.DairatzEntity;
import ru.nyansus.mc.entity.WinterFairyEntity;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.ModItems;

@Mod(Dairatz.MOD_ID)
public final class DairatzForge {
    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIERS =
            DeferredRegister.create(
                    ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Dairatz.MOD_ID);
    static final RegistryObject<MapCodec<ConfiguredFairySpawnsBiomeModifier>>
            FAIRY_SPAWNS_CODEC = BIOME_MODIFIERS.register(
                    "configured_fairy_spawns", ConfiguredFairySpawnsBiomeModifier::codecInstance);

    public DairatzForge(FMLJavaModLoadingContext context) {
        BusGroup modBusGroup = context.getModBusGroup();
        ForgeContentRegistrar registrar = new ForgeContentRegistrar();

        Dairatz.initialize(registrar, FMLPaths.CONFIGDIR.get());
        registrar.register(modBusGroup);
        BIOME_MODIFIERS.register(modBusGroup);

        EntityAttributeCreationEvent.BUS.addListener(DairatzForge::registerAttributes);
        SpawnPlacementRegisterEvent.BUS.addListener(
                DairatzForge::registerSpawnPlacements);
        BuildCreativeModeTabContentsEvent.BUS.addListener(DairatzForge::addCreativeItems);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            DairatzForgeClient.registerEventListeners();
        }

        Dairatz.LOGGER.info("Dairatz Forge initialized");
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.dairatzEntity().get(), DairatzEntity.createAttributes().build());
        event.put(ModEntities.winterFairy().get(), WinterFairyEntity.createAttributes().build());
    }

    private static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(
                ModEntities.dairatzEntity().get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbstractFairyEntity::checkFairySpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(
                ModEntities.winterFairy().get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbstractFairyEntity::checkFairySpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    private static void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.dairatzSpawnEgg());
            event.accept(ModItems.winterFairySpawnEgg());
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.furball());
            event.accept(ModItems.iceFurball());
        }
    }
}
