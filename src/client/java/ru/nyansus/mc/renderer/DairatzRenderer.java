package ru.nyansus.mc.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import ru.nyansus.mc.Dairatz;
import ru.nyansus.mc.entity.DairatzEntity;
import ru.nyansus.mc.models.DairatzModel;

public class DairatzRenderer extends AbstractFairyRenderer<DairatzEntity, DairatzModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            Dairatz.MOD_ID, "textures/entity/poppy.png"
    );

    public DairatzRenderer(EntityRendererProvider.Context context) {
        super(context, new DairatzModel(context.bakeLayer(DairatzModel.LAYER_LOCATION)), TEXTURE);
    }
}
