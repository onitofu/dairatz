package ru.nyansus.mc.fabric;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import ru.nyansus.mc.Dairatz;
import ru.nyansus.mc.registry.ContentRegistrar;
import ru.nyansus.mc.registry.RegistryEntry;

import java.util.function.Function;

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
