package ru.nyansus.mc.neoforge;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import ru.nyansus.mc.models.DairatzModel;
import ru.nyansus.mc.models.WinterFairyModel;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.renderer.DairatzRenderer;
import ru.nyansus.mc.renderer.WinterFairyRenderer;

final class DairatzNeoForgeClient {
    private DairatzNeoForgeClient() {
    }

    static void registerEventListeners(IEventBus modEventBus) {
        modEventBus.addListener(DairatzNeoForgeClient::registerLayers);
        modEventBus.addListener(DairatzNeoForgeClient::registerRenderers);
    }

    private static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                DairatzModel.LAYER_LOCATION, DairatzModel::createBodyLayer);
        event.registerLayerDefinition(
                WinterFairyModel.LAYER_LOCATION, WinterFairyModel::createBodyLayer);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.dairatzEntity().get(), DairatzRenderer::new);
        event.registerEntityRenderer(
                ModEntities.winterFairy().get(), WinterFairyRenderer::new);
        event.registerEntityRenderer(ModEntities.furball().get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.frostball().get(), ThrownItemRenderer::new);
    }
}
