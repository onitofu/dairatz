package ru.nyansus.mc.forge;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import ru.nyansus.mc.models.DairatzModel;
import ru.nyansus.mc.models.WinterFairyModel;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.renderer.DairatzRenderer;
import ru.nyansus.mc.renderer.WinterFairyRenderer;

final class DairatzForgeClient {
    private DairatzForgeClient() {
    }

    static void registerEventListeners() {
        EntityRenderersEvent.RegisterLayerDefinitions.BUS.addListener(
                DairatzForgeClient::registerLayers);
        EntityRenderersEvent.RegisterRenderers.BUS.addListener(
                DairatzForgeClient::registerRenderers);
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
