package ru.nyansus.mc.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import ru.nyansus.mc.network.PaleDetectionPayload;
import ru.nyansus.mc.renderer.PaleClientState;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import ru.nyansus.mc.models.DairatzModel;
import ru.nyansus.mc.models.WinterFairyModel;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.entity.FairyKind;
import ru.nyansus.mc.renderer.NewFairyRenderer;
import ru.nyansus.mc.renderer.DairatzRenderer;
import ru.nyansus.mc.renderer.WinterFairyRenderer;

public final class DairatzFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> PaleClientState.clear());
        ClientPlayNetworking.registerGlobalReceiver(PaleDetectionPayload.TYPE,
                (payload, context) -> context.client().execute(() -> PaleClientState.receive(payload)));
        EntityModelLayerRegistry.registerModelLayer(
                DairatzModel.LAYER_LOCATION, DairatzModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(
                WinterFairyModel.LAYER_LOCATION, WinterFairyModel::createBodyLayer);

        EntityRendererRegistry.register(ModEntities.dairatzEntity().get(), DairatzRenderer::new);
        EntityRendererRegistry.register(
                ModEntities.winterFairy().get(), WinterFairyRenderer::new);
        EntityRendererRegistry.register(ModEntities.furball().get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(
                ModEntities.iceFurball().get(), ThrownItemRenderer::new);

        for (FairyKind kind : ModEntities.newKinds()) {
            EntityRendererRegistry.register(ModEntities.fairy(kind), NewFairyRenderer::new);
            EntityRendererRegistry.register(ModEntities.projectile(kind), ThrownItemRenderer::new);
        }
    }
}
