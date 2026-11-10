package ru.nyansus.mc.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.nyansus.mc.renderer.PaleClientState;

@Mixin(Entity.class)
public abstract class PaleGlowMixin {
    @Inject(method = "isCurrentlyGlowing", at = @At("RETURN"), cancellable = true)
    private void dairatzGlow(CallbackInfoReturnable<Boolean> ci) {
        if (!ci.getReturnValue() && PaleClientState.detected((Entity) (Object) this)) {
            ci.setReturnValue(true);
        }
    }
}
