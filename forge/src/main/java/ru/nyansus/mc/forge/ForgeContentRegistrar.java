package ru.nyansus.mc.forge;

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

    private final DeferredRegister<Block> blocks =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Dairatz.MOD_ID);
    private final DeferredRegister<BlockEntityType<?>> blockEntities =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Dairatz.MOD_ID);
    private final DeferredRegister<Feature<?>> features =
            DeferredRegister.create(ForgeRegistries.FEATURES, Dairatz.MOD_ID);

    @Override
    public <T extends Entity> RegistryEntry<EntityType<T>> registerEntityType(
            String name,
            Function<ResourceKey<EntityType<?>>, EntityType<T>> factory) {
        RegistryObject<EntityType<T>> entry = entityTypes.register(
                name, () -> factory.apply(entityTypes.key(name)));
        return entry::get;
    }

    @Override
    public RegistryEntry<Block> registerBlock(String name, Function<ResourceKey<Block>, Block> factory) {
        RegistryObject<Block> entry = blocks.register(name, () -> factory.apply(blocks.key(name)));
        return entry::get;
    }

    @Override
    public <T extends BlockEntity> RegistryEntry<BlockEntityType<T>> registerBlockEntityType(
            String name, Supplier<BlockEntityType<T>> factory) {
        RegistryObject<BlockEntityType<T>> entry = blockEntities.register(name, factory);
        return entry::get;
    }

    @Override
    public RegistryEntry<Feature<?>> registerFeature(String name, Supplier<Feature<?>> factory) {
        RegistryObject<Feature<?>> entry = features.register(name, factory);
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
        blocks.register(modBusGroup);
        blockEntities.register(modBusGroup);
        features.register(modBusGroup);
        items.register(modBusGroup);
    }
}
