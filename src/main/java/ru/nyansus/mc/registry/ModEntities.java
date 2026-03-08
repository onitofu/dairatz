package ru.nyansus.mc.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import ru.nyansus.mc.Dairatz;
import ru.nyansus.mc.entity.DairatzEntity;
import ru.nyansus.mc.entity.FurballEntity;

public class ModEntities {
    public static final ResourceKey<EntityType<?>> DAIRATZ_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Dairatz.MOD_ID, "dairatz_entity")
    );

    public static final ResourceKey<EntityType<?>> FURBALL_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Dairatz.MOD_ID, "furball")
    );

    public static final EntityType<DairatzEntity> DAIRATZ_ENTITY = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            DAIRATZ_KEY,
            EntityType.Builder.of(DairatzEntity::new, MobCategory.CREATURE)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(8)
                    .build(DAIRATZ_KEY)
    );

    public static final EntityType<FurballEntity> FURBALL = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            FURBALL_KEY,
            EntityType.Builder.<FurballEntity>of(FurballEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(FURBALL_KEY)
    );

    public static void register() {
    }
}
