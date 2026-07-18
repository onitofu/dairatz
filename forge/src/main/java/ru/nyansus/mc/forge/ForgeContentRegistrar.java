package ru.nyansus.mc.forge;

import java.util.function.Function;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.nyansus.mc.Dairatz;
import ru.nyansus.mc.registry.ContentRegistrar;
import ru.nyansus.mc.registry.RegistryEntry;

final class ForgeContentRegistrar implements ContentRegistrar {
    private final DeferredRegister<EntityType<?>> entityTypes =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Dairatz.MOD_ID);
    private final DeferredRegister<Item> items =
            DeferredRegister.create(ForgeRegistries.ITEMS, Dairatz.MOD_ID);

    @Override
    public <T extends Entity> RegistryEntry<EntityType<T>> registerEntityType(
            String name,
            Function<ResourceKey<EntityType<?>>, EntityType<T>> factory) {
        RegistryObject<EntityType<T>> entry = entityTypes.register(
                name, () -> factory.apply(entityTypes.key(name)));
        return entry::get;
    }

    @Override
    public RegistryEntry<Item> registerItem(
            String name,
            Function<ResourceKey<Item>, Item> factory) {
        RegistryObject<Item> entry = items.register(
                name, () -> factory.apply(items.key(name)));
        return entry::get;
    }

    void register(BusGroup modBusGroup) {
        entityTypes.register(modBusGroup);
        items.register(modBusGroup);
    }
}
