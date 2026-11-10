package ru.nyansus.mc.mixin;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.nyansus.mc.loot.FairyLootAccess;
import ru.nyansus.mc.loot.FairyLootSources;

@Mixin(ItemEntity.class)
public abstract class FairyItemLootMixin implements FairyLootAccess {
    @Shadow
    private UUID target;

    @Unique
    private UUID dairatzLootOwner;

    @Override
    public UUID fairyPickupOwner() {
        return target;
    }

    @Override
    public UUID fairyLootOwner() {
        return dairatzLootOwner;
    }

    @Override
    public void setFairyLootOwner(UUID owner) {
        dairatzLootOwner = owner;
    }

    @Inject(method = "setItem", at = @At("TAIL"))
    private void dairatzOrigin(ItemStack stack, CallbackInfo ci) {
        UUID owner = FairyLootSources.take(stack);
        if (owner != null) {
            dairatzLootOwner = owner;
        }
    }

    @Inject(method = "merge(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/Item"
        + "Stack;Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/Item"
        + "Stack;)V",
            at = @At("HEAD"))
    private static void dairatzMerge(ItemEntity into, ItemStack intoStack, ItemEntity from, ItemStack fromStack,
            CallbackInfo ci) {
        FairyLootAccess target = (FairyLootAccess) into;
        if (!Objects.equals(target.fairyLootOwner(), ((FairyLootAccess) from).fairyLootOwner())) {
            target.setFairyLootOwner(null);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void dairatzSave(ValueOutput output, CallbackInfo ci) {
        if (dairatzLootOwner != null) {
            output.store("DairatzLootOwner", UUIDUtil.CODEC, dairatzLootOwner);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void dairatzLoad(ValueInput input, CallbackInfo ci) {
        dairatzLootOwner = input.read("DairatzLootOwner", UUIDUtil.CODEC).orElse(null);
    }
}
