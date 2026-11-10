package ru.nyansus.mc.registry;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;

public interface ContentRegistrar {
    <T extends Entity> RegistryEntry<EntityType<T>> registerEntityType(
            String name,
            Function<ResourceKey<EntityType<?>>, EntityType<T>> factory
    );

    RegistryEntry<Block> registerBlock(String name, Function<ResourceKey<Block>, Block> factory);

    <T extends BlockEntity> RegistryEntry<BlockEntityType<T>> registerBlockEntityType(
            String name, Supplier<BlockEntityType<T>> factory);

    RegistryEntry<Feature<?>> registerFeature(String name, Supplier<Feature<?>> factory);

    RegistryEntry<Item> registerItem(
            String name,
            Function<ResourceKey<Item>, Item> factory
    );
}
