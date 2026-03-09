package ru.nyansus.mc.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import ru.nyansus.mc.Dairatz;
import ru.nyansus.mc.entity.WinterFairyEntity;
import ru.nyansus.mc.models.WinterFairyModel;

public class WinterFairyRenderer
        extends MobRenderer<WinterFairyEntity, WinterFairyRenderState, WinterFairyModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            Dairatz.MOD_ID, "textures/entity/winter.png"
    );

    public WinterFairyRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new WinterFairyModel(context.bakeLayer(WinterFairyModel.LAYER_LOCATION)),
                0.3f
        );
    }

    @Override
    public Identifier getTextureLocation(WinterFairyRenderState state) {
        return TEXTURE;
    }

    @Override
    public WinterFairyRenderState createRenderState() {
        return new WinterFairyRenderState();
    }

    @Override
    public void extractRenderState(
            WinterFairyEntity entity, WinterFairyRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
    }
}
