package ru.nyansus.mc.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.nyansus.mc.entity.FairyAbilities;
import ru.nyansus.mc.entity.FairyEffectState;
import ru.nyansus.mc.entity.FairyLivingAccess;

@Mixin(LivingEntity.class)
public abstract class FairyLivingMixin implements FairyLivingAccess {
    @Unique
    private final FairyEffectState dairatzEffects = new FairyEffectState();

    @Override
    public FairyEffectState fairyEffects() {
        return dairatzEffects;
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void dairatzDamage(ServerLevel level, DamageSource source, float amount,
            CallbackInfoReturnable<Boolean> ci) {
        if (ci.getReturnValue()) {
            FairyAbilities.afterDamage((LivingEntity) (Object) this, level, source);
        }
    }

    @ModifyVariable(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;"
            + "Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"), argsOnly = true)
    private MobEffectInstance dairatzPoison(MobEffectInstance effect) {
        return FairyAbilities.protectFromPoison((LivingEntity) (Object) this, effect);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void dairatzSave(ValueOutput output, CallbackInfo ci) {
        dairatzEffects.save(output);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void dairatzLoad(ValueInput input, CallbackInfo ci) {
        dairatzEffects.load(input);
    }
}
