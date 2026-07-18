package ru.nyansus.mc.neoforge;

import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
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
        items.register(modEventBus);
    }
}
