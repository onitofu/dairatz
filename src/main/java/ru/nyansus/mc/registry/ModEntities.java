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
import ru.nyansus.mc.entity.FrostballEntity;
import ru.nyansus.mc.entity.FurballEntity;
import ru.nyansus.mc.entity.WinterFairyEntity;

public class ModEntities {
    private static final float FAIRY_HITBOX_WIDTH = 0.5f;
    private static final float FAIRY_HITBOX_HEIGHT = 0.5f;
    private static final int FAIRY_TRACKING_RANGE = 8;

    private static final float PROJECTILE_HITBOX_WIDTH = 0.25f;
    private static final float PROJECTILE_HITBOX_HEIGHT = 0.25f;
    private static final int PROJECTILE_TRACKING_RANGE = 4;
    private static final int PROJECTILE_UPDATE_INTERVAL = 10;

    public static final ResourceKey<EntityType<?>> DAIRATZ_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Dairatz.MOD_ID, "dairatz_entity")
    );

    public static final ResourceKey<EntityType<?>> FURBALL_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Dairatz.MOD_ID, "furball")
    );

    public static final ResourceKey<EntityType<?>> WINTER_FAIRY_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Dairatz.MOD_ID, "winter_fairy")
    );

    public static final ResourceKey<EntityType<?>> FROSTBALL_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Dairatz.MOD_ID, "frostball")
    );

    public static final EntityType<DairatzEntity> DAIRATZ_ENTITY = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            DAIRATZ_KEY,
            EntityType.Builder.of(DairatzEntity::new, MobCategory.CREATURE)
                    .sized(FAIRY_HITBOX_WIDTH, FAIRY_HITBOX_HEIGHT)
                    .clientTrackingRange(FAIRY_TRACKING_RANGE)
                    .build(DAIRATZ_KEY)
    );

    public static final EntityType<FurballEntity> FURBALL = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            FURBALL_KEY,
            EntityType.Builder.<FurballEntity>of(FurballEntity::new, MobCategory.MISC)
                    .sized(PROJECTILE_HITBOX_WIDTH, PROJECTILE_HITBOX_HEIGHT)
                    .clientTrackingRange(PROJECTILE_TRACKING_RANGE)
                    .updateInterval(PROJECTILE_UPDATE_INTERVAL)
                    .build(FURBALL_KEY)
    );

    public static final EntityType<WinterFairyEntity> WINTER_FAIRY = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            WINTER_FAIRY_KEY,
            EntityType.Builder.of(WinterFairyEntity::new, MobCategory.CREATURE)
                    .sized(FAIRY_HITBOX_WIDTH, FAIRY_HITBOX_HEIGHT)
                    .clientTrackingRange(FAIRY_TRACKING_RANGE)
                    .build(WINTER_FAIRY_KEY)
    );

    public static final EntityType<FrostballEntity> FROSTBALL = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            FROSTBALL_KEY,
            EntityType.Builder.<FrostballEntity>of(FrostballEntity::new, MobCategory.MISC)
                    .sized(PROJECTILE_HITBOX_WIDTH, PROJECTILE_HITBOX_HEIGHT)
                    .clientTrackingRange(PROJECTILE_TRACKING_RANGE)
                    .updateInterval(PROJECTILE_UPDATE_INTERVAL)
                    .build(FROSTBALL_KEY)
    );

    public static void register() {
    }
}
