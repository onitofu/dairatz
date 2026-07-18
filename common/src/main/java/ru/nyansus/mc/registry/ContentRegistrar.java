package ru.nyansus.mc.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public interface ContentRegistrar {
    <T extends Entity> RegistryEntry<EntityType<T>> registerEntityType(
            String name,
            Function<ResourceKey<EntityType<?>>, EntityType<T>> factory
    );

    RegistryEntry<Item> registerItem(
            String name,
            Function<ResourceKey<Item>, Item> factory
    );
}
