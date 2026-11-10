package ru.nyansus.mc.neoforge;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.nyansus.mc.Dairatz;
import ru.nyansus.mc.registry.ContentRegistrar;
import ru.nyansus.mc.registry.RegistryEntry;

final class NeoForgeContentRegistrar implements ContentRegistrar {
    private final DeferredRegister<EntityType<?>> entityTypes =
            DeferredRegister.create(Registries.ENTITY_TYPE, Dairatz.MOD_ID);
    private final DeferredRegister<Item> items =
            DeferredRegister.create(Registries.ITEM, Dairatz.MOD_ID);

    private final DeferredRegister<Block> blocks =
            DeferredRegister.create(Registries.BLOCK, Dairatz.MOD_ID);
    private final DeferredRegister<BlockEntityType<?>> blockEntities =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Dairatz.MOD_ID);
    private final DeferredRegister<Feature<?>> features =
            DeferredRegister.create(Registries.FEATURE, Dairatz.MOD_ID);

    @Override
    public <T extends Entity> RegistryEntry<EntityType<T>> registerEntityType(
            String name,
            Function<ResourceKey<EntityType<?>>, EntityType<T>> factory) {
        DeferredHolder<EntityType<?>, EntityType<T>> entry = entityTypes.register(
                name,
                id -> factory.apply(ResourceKey.create(Registries.ENTITY_TYPE, id)));
        return entry::get;
    }

    @Override
    public RegistryEntry<Block> registerBlock(String name, Function<ResourceKey<Block>, Block> factory) {
        DeferredHolder<Block, Block> entry = blocks.register(
                name, id -> factory.apply(ResourceKey.create(Registries.BLOCK, id)));
        return entry::get;
    }

    @Override
    public <T extends BlockEntity> RegistryEntry<BlockEntityType<T>> registerBlockEntityType(
            String name, Supplier<BlockEntityType<T>> factory) {
        DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> entry = blockEntities.register(name, factory);
        return entry::get;
    }

    @Override
    public RegistryEntry<Feature<?>> registerFeature(String name, Supplier<Feature<?>> factory) {
        DeferredHolder<Feature<?>, Feature<?>> entry = features.register(name, factory);
        return entry::get;
    }

    @Override
    public RegistryEntry<Item> registerItem(
            String name,
            Function<ResourceKey<Item>, Item> factory) {
        DeferredHolder<Item, Item> entry = items.register(
                name,
                id -> factory.apply(ResourceKey.create(Registries.ITEM, id)));
        return entry::get;
    }

    void register(IEventBus modEventBus) {
        entityTypes.register(modEventBus);
        blocks.register(modEventBus);
        blockEntities.register(modEventBus);
        features.register(modEventBus);
        items.register(modEventBus);
    }
}
