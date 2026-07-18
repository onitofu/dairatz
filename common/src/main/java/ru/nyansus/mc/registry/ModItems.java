package ru.nyansus.mc.registry;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;

public final class ModItems {
    public static final String FURBALL_NAME = "furball";
    public static final String ICE_FURBALL_NAME = "ice_furball";
    public static final String DAIRATZ_SPAWN_EGG_NAME = "dairatz_spawn_egg";
    public static final String WINTER_FAIRY_SPAWN_EGG_NAME = "winter_fairy_spawn_egg";

    private static RegistryEntry<Item> furball;
    private static RegistryEntry<Item> iceFurball;
    private static RegistryEntry<Item> dairatzSpawnEgg;
    private static RegistryEntry<Item> winterFairySpawnEgg;

    private ModItems() {
    }

    public static void register(ContentRegistrar registrar) {
        ensureNotRegistered();

        furball = registrar.registerItem(
                FURBALL_NAME,
                key -> new Item(new Item.Properties().setId(key))
        );

        iceFurball = registrar.registerItem(
                ICE_FURBALL_NAME,
                key -> new Item(new Item.Properties().setId(key))
        );

        dairatzSpawnEgg = registrar.registerItem(
                DAIRATZ_SPAWN_EGG_NAME,
                key -> new SpawnEggItem(new Item.Properties()
                        .setId(key)
                        .component(
                                DataComponents.ENTITY_DATA,
                                TypedEntityData.of(
                                        ModEntities.dairatzEntity().get(), new CompoundTag()
                                )
                        ))
        );

        winterFairySpawnEgg = registrar.registerItem(
                WINTER_FAIRY_SPAWN_EGG_NAME,
                key -> new SpawnEggItem(new Item.Properties()
                        .setId(key)
                        .component(
                                DataComponents.ENTITY_DATA,
                                TypedEntityData.of(
                                        ModEntities.winterFairy().get(), new CompoundTag()
                                )
                        ))
        );
    }

    private static void ensureNotRegistered() {
        if (furball != null) {
            throw new IllegalStateException("Dairatz items are already registered");
        }
    }

    public static RegistryEntry<Item> furball() {
        return requireRegistered(furball, FURBALL_NAME);
    }

    public static RegistryEntry<Item> iceFurball() {
        return requireRegistered(iceFurball, ICE_FURBALL_NAME);
    }

    public static RegistryEntry<Item> dairatzSpawnEgg() {
        return requireRegistered(dairatzSpawnEgg, DAIRATZ_SPAWN_EGG_NAME);
    }

    public static RegistryEntry<Item> winterFairySpawnEgg() {
        return requireRegistered(winterFairySpawnEgg, WINTER_FAIRY_SPAWN_EGG_NAME);
    }

    private static RegistryEntry<Item> requireRegistered(
            RegistryEntry<Item> entry,
            String name) {
        if (entry == null) {
            throw new IllegalStateException(name + " is not registered yet");
        }
        return entry;
    }
}
