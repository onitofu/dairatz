package ru.nyansus.mc.entity;

import java.util.Locale;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import ru.nyansus.mc.config.DairatzConfig;
import ru.nyansus.mc.config.FairySpeciesSettings;

public enum FairyKind {
    STANDARD("oak", Items.POPPY, Blocks.OAK_LOG, Blocks.OAK_LEAVES),
    WINTER("spruce", Items.SWEET_BERRIES, Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES),
    JUNGLE("jungle", Items.COCOA_BEANS, Blocks.JUNGLE_LOG, Blocks.JUNGLE_LEAVES),
    CHERRY("cherry", Items.PINK_PETALS, Blocks.CHERRY_LOG, Blocks.CHERRY_LEAVES),
    PALE("pale_oak", Items.RESIN_CLUMP, Blocks.PALE_OAK_LOG, Blocks.PALE_OAK_LEAVES),
    ENDER("ender", Items.CHORUS_FRUIT, Blocks.END_STONE, Blocks.AIR);

    private final String wood;
    private final Item food;
    private final Block log;
    private final Block leaves;

    FairyKind(String wood, Item food, Block log, Block leaves) {
        this.wood = wood;
        this.food = food;
        this.log = log;
        this.leaves = leaves;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String hollowId() {
        return wood + "_fairy_hollow";
    }

    public Item food() {
        return food;
    }

    public Block log() {
        return log;
    }

    public Block leaves() {
        return leaves;
    }

    public boolean hasVariants() {
        return this == JUNGLE || this == PALE;
    }

    public FairySpeciesSettings settings() {
        return switch (this) {
            case JUNGLE -> DairatzConfig.current().jungle();
            case CHERRY -> DairatzConfig.current().cherry();
            case PALE -> DairatzConfig.current().pale();
            case ENDER -> DairatzConfig.current().ender();
            default -> throw new IllegalStateException("Legacy species uses its original settings");
        };
    }
}
