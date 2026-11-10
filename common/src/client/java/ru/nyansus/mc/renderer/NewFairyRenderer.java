package ru.nyansus.mc.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import ru.nyansus.mc.Dairatz;
import ru.nyansus.mc.entity.NewFairyEntity;
import ru.nyansus.mc.models.DairatzModel;

public final class NewFairyRenderer extends AbstractFairyRenderer<NewFairyEntity, DairatzModel> {
    public NewFairyRenderer(EntityRendererProvider.Context context) {
        super(context, new DairatzModel(context.bakeLayer(DairatzModel.LAYER_LOCATION)),
                Identifier.fromNamespaceAndPath(Dairatz.MOD_ID, "textures/entity/cherry.png"));
    }

    @Override
    public Identifier getTextureLocation(FairyRenderState state) {
        return Identifier.fromNamespaceAndPath(Dairatz.MOD_ID,
                "textures/entity/" + state.kind.id() + (state.appearance == 1 ? "_alternate" : "") + ".png");
    }
}
