package ru.nyansus.mc.registry;

import java.util.Set;
import java.util.Map;
import java.util.EnumMap;
import java.util.HashSet;
import ru.nyansus.mc.entity.FairyKind;
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
import ru.nyansus.mc.hollow.FairyHollowFeature;

public final class ModHollows {
    public static final String OAK_HOLLOW_NAME = "oak_fairy_hollow";

    public static final ResourceKey<PlacedFeature> OAK_HOLLOW_PLACED = ResourceKey.create(
            Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(Dairatz.MOD_ID, OAK_HOLLOW_NAME));

    private static final Map<FairyKind, RegistryEntry<Block>> HOLLOWS = new EnumMap<>(FairyKind.class);
    private static final Map<FairyKind, RegistryEntry<Item>> ITEMS = new EnumMap<>(FairyKind.class);

    public static Block hollow(FairyKind kind) {
        return kind == FairyKind.STANDARD ? oakHollow.get() : HOLLOWS.get(kind).get();
    }

    public static Item hollowItem(FairyKind kind) {
        return kind == FairyKind.STANDARD ? oakHollowItem.get() : ITEMS.get(kind).get();
    }

    public static FairyKind kindOf(Block block) {
        for (FairyKind kind : FairyKind.values()) {
            if (kind != FairyKind.ENDER && hollow(kind) == block) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Unknown fairy hollow");
    }

    public static Set<Block> hollowBlocks() {
        HashSet<Block> blocks = new HashSet<>();
        blocks.add(oakHollow.get());
        HOLLOWS.values().forEach(entry -> blocks.add(entry.get()));
        return Set.copyOf(blocks);
    }

    private static RegistryEntry<Block> oakHollow;
    private static RegistryEntry<Item> oakHollowItem;
    private static RegistryEntry<BlockEntityType<FairyHollowBlockEntity>> hollowType;

    private ModHollows() {
    }

    public static void register(ContentRegistrar registrar) {
        oakHollow = registrar.registerBlock(OAK_HOLLOW_NAME,
                key -> new FairyHollowBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)
                        .mapColor(MapColor.WOOD).setId(key)));
        oakHollowItem = registrar.registerItem(OAK_HOLLOW_NAME,
                key -> new BlockItem(oakHollow.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));
        for (FairyKind kind : FairyKind.values()) {
            if (kind == FairyKind.STANDARD || kind == FairyKind.ENDER) {
                continue;
            }
            HOLLOWS.put(kind, registrar.registerBlock(kind.hollowId(),
                    key -> new FairyHollowBlock(BlockBehaviour.Properties.ofFullCopy(kind.log())
                            .mapColor(MapColor.WOOD).setId(key))));
            ITEMS.put(kind, registrar.registerItem(kind.hollowId(),
                    key -> new BlockItem(hollow(kind), new Item.Properties().setId(key).useBlockDescriptionPrefix())));
        }
        hollowType = registrar.registerBlockEntityType(OAK_HOLLOW_NAME,
                () -> new BlockEntityType<>(FairyHollowBlockEntity::new, hollowBlocks()));
        registrar.registerFeature(OAK_HOLLOW_NAME, FairyHollowFeature::new);
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
