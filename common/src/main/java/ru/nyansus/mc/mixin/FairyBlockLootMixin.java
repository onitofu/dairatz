package ru.nyansus.mc.mixin;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.nyansus.mc.loot.FairyLootSources;

@Mixin(Block.class)
public abstract class FairyBlockLootMixin {
    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server"
        + "/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block"
        + "/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item"
        + "/ItemStack;)Ljava/util/List;",
            at = @At("RETURN"))
    private static void dairatzBlockLoot(BlockState state, ServerLevel level, BlockPos pos, BlockEntity blockEntity,
            Entity actor, ItemStack tool, CallbackInfoReturnable<List<ItemStack>> ci) {
        FairyLootSources.markBlocks(actor, ci.getReturnValue());
    }
}
