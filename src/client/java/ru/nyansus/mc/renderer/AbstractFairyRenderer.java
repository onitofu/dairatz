package ru.nyansus.mc.renderer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import ru.nyansus.mc.entity.AbstractFairyEntity;

public abstract class AbstractFairyRenderer<
        E extends AbstractFairyEntity,
        M extends EntityModel<? super FairyRenderState>>
        extends MobRenderer<E, FairyRenderState, M> {

    private static final float SHADOW_RADIUS = 0.3f;
    private final Identifier texture;

    protected AbstractFairyRenderer(
            EntityRendererProvider.Context context, M model, Identifier texture) {
        super(context, model, SHADOW_RADIUS);
        this.texture = texture;
    }

    @Override
    public Identifier getTextureLocation(FairyRenderState state) {
        return texture;
    }

    @Override
    public FairyRenderState createRenderState() {
        return new FairyRenderState();
    }

    @Override
    public void extractRenderState(E entity, FairyRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
    }
}
