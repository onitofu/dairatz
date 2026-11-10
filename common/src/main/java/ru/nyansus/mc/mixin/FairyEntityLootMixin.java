package ru.nyansus.mc.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.nyansus.mc.loot.FairyLootSources;

@Mixin(Entity.class)
public abstract class FairyEntityLootMixin {
    @Inject(method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/ite"
        + "m/ItemStack;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/entity/item/Ite"
        + "mEntity;",
            at = @At("RETURN"))
    private void dairatzMobLoot(ServerLevel level, ItemStack stack, Vec3 offset,
            CallbackInfoReturnable<ItemEntity> ci) {
        FairyLootSources.markDeath((Entity) (Object) this, ci.getReturnValue());
    }
}
