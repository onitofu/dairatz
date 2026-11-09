package ru.nyansus.mc.fabric;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import ru.nyansus.mc.Dairatz;
import ru.nyansus.mc.registry.ContentRegistrar;
import ru.nyansus.mc.registry.RegistryEntry;

final class FabricContentRegistrar implements ContentRegistrar {
    @Override
    public <T extends Entity> RegistryEntry<EntityType<T>> registerEntityType(
            String name,
            Function<ResourceKey<EntityType<?>>, EntityType<T>> factory
    ) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(
                Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(Dairatz.MOD_ID, name)
        );
        EntityType<T> value = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                factory.apply(key)
        );
        return () -> value;
    }

    @Override
    public RegistryEntry<Block> registerBlock(String name, Function<ResourceKey<Block>, Block> factory) {
        ResourceKey<Block> key = ResourceKey.create(
                Registries.BLOCK, Identifier.fromNamespaceAndPath(Dairatz.MOD_ID, name));
        Block value = Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(key));
        return () -> value;
    }

    @Override
    public <T extends BlockEntity> RegistryEntry<BlockEntityType<T>> registerBlockEntityType(
            String name, Supplier<BlockEntityType<T>> factory) {
        BlockEntityType<T> value = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(Dairatz.MOD_ID, name), factory.get());
        return () -> value;
    }

    @Override
    public RegistryEntry<Feature<?>> registerFeature(String name, Supplier<Feature<?>> factory) {
        Feature<?> value = Registry.register(
                BuiltInRegistries.FEATURE, Identifier.fromNamespaceAndPath(Dairatz.MOD_ID, name), factory.get());
        return () -> value;
    }

    @Override
    public RegistryEntry<Item> registerItem(
            String name,
            Function<ResourceKey<Item>, Item> factory
    ) {
        ResourceKey<Item> key = ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(Dairatz.MOD_ID, name)
        );
        Item value = Registry.register(BuiltInRegistries.ITEM, key, factory.apply(key));
        return () -> value;
    }
}
