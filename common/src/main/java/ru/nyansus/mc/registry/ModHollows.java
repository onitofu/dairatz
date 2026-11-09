package ru.nyansus.mc.registry;

import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.material.MapColor;
import ru.nyansus.mc.Dairatz;
import ru.nyansus.mc.hollow.FairyHollowBlock;
import ru.nyansus.mc.hollow.FairyHollowBlockEntity;
import ru.nyansus.mc.hollow.OakHollowFeature;

public final class ModHollows {
    public static final String OAK_HOLLOW_NAME = "oak_fairy_hollow";

    public static final ResourceKey<PlacedFeature> OAK_HOLLOW_PLACED = ResourceKey.create(
            Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(Dairatz.MOD_ID, OAK_HOLLOW_NAME));

    private static RegistryEntry<Block> oakHollow;
    private static RegistryEntry<Item> oakHollowItem;
    private static RegistryEntry<BlockEntityType<FairyHollowBlockEntity>> hollowType;

    private ModHollows() {
    }

    public static void register(ContentRegistrar registrar) {
        oakHollow = registrar.registerBlock(OAK_HOLLOW_NAME,
                key -> new FairyHollowBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)
                        .mapColor(MapColor.WOOD).setId(key)));
        hollowType = registrar.registerBlockEntityType(OAK_HOLLOW_NAME,
                () -> new BlockEntityType<>(FairyHollowBlockEntity::new, Set.of(oakHollow.get())));
        oakHollowItem = registrar.registerItem(OAK_HOLLOW_NAME,
                key -> new BlockItem(oakHollow.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));
        registrar.registerFeature(OAK_HOLLOW_NAME, OakHollowFeature::new);
    }

    public static Block oakHollow() {
        return oakHollow.get();
    }

    public static Item oakHollowItem() {
        return oakHollowItem.get();
    }

    public static BlockEntityType<FairyHollowBlockEntity> hollowType() {
        return hollowType.get();
    }
}
