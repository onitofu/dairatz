package ru.nyansus.mc.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import ru.nyansus.mc.Dairatz;
import ru.nyansus.mc.entity.DairatzEntity;
import ru.nyansus.mc.models.DairatzModel;

public class DairatzRenderer extends MobRenderer<DairatzEntity, DairatzRenderState, DairatzModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            Dairatz.MOD_ID, "textures/entity/poppy.png"
    );

    public DairatzRenderer(EntityRendererProvider.Context context) {
        super(context, new DairatzModel(context.bakeLayer(DairatzModel.LAYER_LOCATION)), 0.3f);
    }

    @Override
    public Identifier getTextureLocation(DairatzRenderState state) {
        return TEXTURE;
    }

    @Override
    public DairatzRenderState createRenderState() {
        return new DairatzRenderState();
    }

    @Override
    public void extractRenderState(DairatzEntity entity, DairatzRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
    }
}
