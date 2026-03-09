package ru.nyansus.mc.models;

import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;
import ru.nyansus.mc.Dairatz;
import ru.nyansus.mc.animations.IdleAnimation;
import ru.nyansus.mc.renderer.WinterFairyRenderState;

public class WinterFairyModel extends EntityModel<WinterFairyRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(Dairatz.MOD_ID, "winter_fairy"), "main"
    );

    private final ModelPart main;
    private final KeyframeAnimation idleAnimation;

    public WinterFairyModel(ModelPart root) {
        super(root);
        this.main = root.getChild("main");
        this.idleAnimation = KeyframeAnimation.bake(root, IdleAnimation.IDLE);
    }

    public static LayerDefinition createBodyLayer() {
        return DairatzModel.createBodyLayer();
    }

    @Override
    public void setupAnim(WinterFairyRenderState state) {
        super.setupAnim(state);
        idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
    }
}
