package ru.nyansus.mc.neoforge;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import ru.nyansus.mc.config.DairatzConfig;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.FairySpawns;
import ru.nyansus.mc.entity.FairyKind;

final class ConfiguredFairySpawnsBiomeModifier implements BiomeModifier {
    private static final ConfiguredFairySpawnsBiomeModifier INSTANCE =
            new ConfiguredFairySpawnsBiomeModifier();
    private static final MapCodec<ConfiguredFairySpawnsBiomeModifier> CODEC =
            MapCodec.unit(INSTANCE);
    private static final Set<ResourceKey<Biome>> DAIRATZ_BIOMES = Set.of(
            Biomes.FLOWER_FOREST, Biomes.MEADOW, Biomes.SUNFLOWER_PLAINS);
    private static final Set<ResourceKey<Biome>> WINTER_BIOMES = Set.of(
            Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA, Biomes.FROZEN_RIVER,
            Biomes.SNOWY_BEACH, Biomes.GROVE, Biomes.FROZEN_PEAKS, Biomes.ICE_SPIKES);

    private ConfiguredFairySpawnsBiomeModifier() {
    }

    static MapCodec<ConfiguredFairySpawnsBiomeModifier> codecInstance() {
        return CODEC;
    }

    @Override
    public void modify(
            Holder<Biome> biome,
            Phase phase,
            ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD) {
            return;
        }
        if (DairatzConfig.current().dairatzSpawnWeight() > 0 && matches(biome, DAIRATZ_BIOMES)) {
            addSpawn(
                    builder,
                    ModEntities.dairatzEntity().get(),
                    DairatzConfig.current().dairatzSpawnWeight(),
                    DairatzConfig.current().dairatzMinGroup(),
                    DairatzConfig.current().dairatzMaxGroup());
        }
        if (DairatzConfig.current().winterSpawnWeight() > 0 && matches(biome, WINTER_BIOMES)) {
            addSpawn(
                    builder,
                    ModEntities.winterFairy().get(),
                    DairatzConfig.current().winterSpawnWeight(),
                    DairatzConfig.current().winterMinGroup(),
                    DairatzConfig.current().winterMaxGroup());
        }

        for (FairyKind kind : ModEntities.newKinds()) {
            if (kind.settings().spawnWeight() > 0 && FairySpawns.biomes(kind).stream().anyMatch(biome::is)) {
                addSpawn(builder, ModEntities.fairy(kind), kind.settings().spawnWeight(),
                        kind.settings().minGroup(), kind.settings().maxGroup());
            }
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return DairatzNeoForge.FAIRY_SPAWNS_CODEC.get();
    }

    private static boolean matches(
            Holder<Biome> biome,
            Set<ResourceKey<Biome>> candidates) {
        return candidates.stream().anyMatch(biome::is);
    }

    private static void addSpawn(
            ModifiableBiomeInfo.BiomeInfo.Builder builder,
            EntityType<?> entityType,
            int weight,
            int minGroup,
            int maxGroup) {
        builder.getMobSpawnSettings().addSpawn(
                MobCategory.CREATURE,
                weight,
                new MobSpawnSettings.SpawnerData(entityType, minGroup, maxGroup));
    }
}
