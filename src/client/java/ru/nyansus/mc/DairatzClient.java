package ru.nyansus.mc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import ru.nyansus.mc.models.DairatzModel;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.renderer.DairatzRenderer;

public class DairatzClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(DairatzModel.LAYER_LOCATION, DairatzModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntities.DAIRATZ_ENTITY, DairatzRenderer::new);
        EntityRendererRegistry.register(ModEntities.FURBALL, ThrownItemRenderer::new);
    }
}
