package ru.nyansus.mc.registry;

import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import ru.nyansus.mc.entity.FairyKind;

public final class FairySpawns {
    private FairySpawns() {
    }

    public static List<ResourceKey<Biome>> biomes(FairyKind kind) {
        return switch (kind) {
            case JUNGLE -> List.of(Biomes.JUNGLE, Biomes.SPARSE_JUNGLE, Biomes.BAMBOO_JUNGLE);
            case CHERRY -> List.of(Biomes.CHERRY_GROVE);
            case PALE -> List.of(Biomes.PALE_GARDEN);
            case ENDER -> List.of(Biomes.END_HIGHLANDS, Biomes.END_MIDLANDS);
            default -> List.of();
        };
    }
}
