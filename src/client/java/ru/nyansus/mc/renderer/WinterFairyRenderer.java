package ru.nyansus.mc.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import ru.nyansus.mc.Dairatz;
import ru.nyansus.mc.entity.WinterFairyEntity;
import ru.nyansus.mc.models.WinterFairyModel;

public class WinterFairyRenderer
        extends AbstractFairyRenderer<WinterFairyEntity, WinterFairyModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            Dairatz.MOD_ID, "textures/entity/winter.png"
    );

    public WinterFairyRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new WinterFairyModel(context.bakeLayer(WinterFairyModel.LAYER_LOCATION)),
                TEXTURE
        );
    }
}
