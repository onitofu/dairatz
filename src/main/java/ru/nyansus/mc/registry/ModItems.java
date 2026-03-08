package ru.nyansus.mc.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;
import ru.nyansus.mc.Dairatz;

public class ModItems {
    private static final ResourceKey<Item> FURBALL_KEY = ResourceKey.create(
            Registries.ITEM, Identifier.fromNamespaceAndPath(Dairatz.MOD_ID, "furball")
    );

    private static final ResourceKey<Item> SPAWN_EGG_KEY = ResourceKey.create(
            Registries.ITEM, Identifier.fromNamespaceAndPath(Dairatz.MOD_ID, "dairatz_spawn_egg")
    );

    public static final Item FURBALL = Registry.register(
            BuiltInRegistries.ITEM,
            FURBALL_KEY,
            new Item(new Item.Properties().setId(FURBALL_KEY))
    );

    public static final Item DAIRATZ_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            SPAWN_EGG_KEY,
            new SpawnEggItem(new Item.Properties()
                    .setId(SPAWN_EGG_KEY)
                    .component(DataComponents.ENTITY_DATA,
                            TypedEntityData.of(ModEntities.DAIRATZ_ENTITY, new CompoundTag())))
    );

    public static void register() {
    }
}
