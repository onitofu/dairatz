package ru.nyansus.mc.models;

import net.minecraft.client.model.geom.ModelPart;
import ru.nyansus.mc.renderer.FairyRenderState;

final class FairyRestPose {
    private FairyRestPose() {
    }

    static void apply(ModelPart main, FairyRenderState state) {
        if (!state.resting) {
            return;
        }
        ModelPart body = main.getChild("body");
        ModelPart wings = body.getChild("wings");
        wings.getChild("rightWing").yRot += 1.1f;
        wings.getChild("leftWing").yRot -= 1.1f;
        body.xRot += 0.15f;
    }
}
