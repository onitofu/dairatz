package ru.nyansus.mc.neoforge;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import ru.nyansus.mc.Dairatz;
import ru.nyansus.mc.entity.AbstractFairyEntity;
import ru.nyansus.mc.entity.DairatzEntity;
import ru.nyansus.mc.entity.WinterFairyEntity;
import ru.nyansus.mc.entity.FairyKind;
import ru.nyansus.mc.entity.NewFairyEntity;
import ru.nyansus.mc.hollow.FairyRecoveryJournal;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.ModHollows;
import ru.nyansus.mc.registry.ModItems;

@Mod(Dairatz.MOD_ID)
public final class DairatzNeoForge {
    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIERS =
            DeferredRegister.create(
                    NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Dairatz.MOD_ID);
    static final DeferredHolder<
            MapCodec<? extends BiomeModifier>,
            MapCodec<ConfiguredFairySpawnsBiomeModifier>> FAIRY_SPAWNS_CODEC =
            BIOME_MODIFIERS.register(
                    "configured_fairy_spawns", ConfiguredFairySpawnsBiomeModifier::codecInstance);

    public DairatzNeoForge(IEventBus modEventBus) {
        NeoForgeContentRegistrar registrar = new NeoForgeContentRegistrar();

        Dairatz.initialize(registrar, FMLPaths.CONFIGDIR.get());
        registrar.register(modEventBus);
        NeoForgeFairyNetwork.register(modEventBus);
        BIOME_MODIFIERS.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(DairatzNeoForge::onServerTick);
        modEventBus.addListener(DairatzNeoForge::registerAttributes);
        modEventBus.addListener(DairatzNeoForge::registerSpawnPlacements);
        modEventBus.addListener(DairatzNeoForge::addCreativeItems);

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            DairatzNeoForgeClient.registerEventListeners(modEventBus);
        }

        Dairatz.LOGGER.info("Dairatz NeoForge initialized");
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        FairyRecoveryJournal.tick(event.getServer());
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.dairatzEntity().get(), DairatzEntity.createAttributes().build());
        event.put(ModEntities.winterFairy().get(), WinterFairyEntity.createAttributes().build());

        for (FairyKind kind : ModEntities.newKinds()) {
            event.put(ModEntities.fairy(kind), NewFairyEntity.createAttributes(kind).build());
        }
    }

    private static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                ModEntities.dairatzEntity().get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbstractFairyEntity::checkFairySpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(
                ModEntities.winterFairy().get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbstractFairyEntity::checkFairySpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);

        for (FairyKind kind : ModEntities.newKinds()) {
            event.register(ModEntities.fairy(kind), SpawnPlacementTypes.NO_RESTRICTIONS,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractFairyEntity::checkFairySpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
    }

    private static void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(ModHollows.oakHollowItem());
            for (FairyKind kind : FairyKind.values()) {
                if (kind != FairyKind.STANDARD && kind != FairyKind.ENDER) {
                    event.accept(ModHollows.hollowItem(kind));
                }
            }
        } else if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.dairatzSpawnEgg().get());
            event.accept(ModItems.winterFairySpawnEgg().get());
            ModEntities.newKinds().forEach(kind -> event.accept(ModItems.egg(kind)));
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.furball().get());
            event.accept(ModItems.iceFurball().get());
            ModEntities.newKinds().forEach(kind -> event.accept(ModItems.projectile(kind)));
        }
    }
}
