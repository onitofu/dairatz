package ru.nyansus.mc.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import ru.nyansus.mc.entity.DairatzEntity;
import ru.nyansus.mc.entity.FrostballEntity;
import ru.nyansus.mc.entity.FurballEntity;
import ru.nyansus.mc.entity.WinterFairyEntity;

public final class ModEntities {
    public static final String DAIRATZ_NAME = "dairatz_entity";
    public static final String FURBALL_NAME = "furball";
    public static final String WINTER_FAIRY_NAME = "winter_fairy";
    public static final String FROSTBALL_NAME = "frostball";

    private static final float FAIRY_HITBOX_WIDTH = 0.5f;
    private static final float FAIRY_HITBOX_HEIGHT = 0.5f;
    private static final int FAIRY_TRACKING_RANGE = 8;

    private static final float PROJECTILE_HITBOX_WIDTH = 0.25f;
    private static final float PROJECTILE_HITBOX_HEIGHT = 0.25f;
    private static final int PROJECTILE_TRACKING_RANGE = 4;
    private static final int PROJECTILE_UPDATE_INTERVAL = 10;

    private static RegistryEntry<EntityType<DairatzEntity>> dairatzEntity;
    private static RegistryEntry<EntityType<FurballEntity>> furball;
    private static RegistryEntry<EntityType<WinterFairyEntity>> winterFairy;
    private static RegistryEntry<EntityType<FrostballEntity>> frostball;

    private ModEntities() {
    }

    public static void register(ContentRegistrar registrar) {
        ensureNotRegistered();

        dairatzEntity = registrar.registerEntityType(
                DAIRATZ_NAME,
                key -> EntityType.Builder.of(DairatzEntity::new, MobCategory.CREATURE)
                        .sized(FAIRY_HITBOX_WIDTH, FAIRY_HITBOX_HEIGHT)
                        .clientTrackingRange(FAIRY_TRACKING_RANGE)
                        .build(key)
        );

        furball = registrar.registerEntityType(
                FURBALL_NAME,
                key -> EntityType.Builder.<FurballEntity>of(
                                FurballEntity::new, MobCategory.MISC)
                        .sized(PROJECTILE_HITBOX_WIDTH, PROJECTILE_HITBOX_HEIGHT)
                        .clientTrackingRange(PROJECTILE_TRACKING_RANGE)
                        .updateInterval(PROJECTILE_UPDATE_INTERVAL)
                        .build(key)
        );

        winterFairy = registrar.registerEntityType(
                WINTER_FAIRY_NAME,
                key -> EntityType.Builder.of(WinterFairyEntity::new, MobCategory.CREATURE)
                        .sized(FAIRY_HITBOX_WIDTH, FAIRY_HITBOX_HEIGHT)
                        .clientTrackingRange(FAIRY_TRACKING_RANGE)
                        .build(key)
        );

        frostball = registrar.registerEntityType(
                FROSTBALL_NAME,
                key -> EntityType.Builder.<FrostballEntity>of(
                                FrostballEntity::new, MobCategory.MISC)
                        .sized(PROJECTILE_HITBOX_WIDTH, PROJECTILE_HITBOX_HEIGHT)
                        .clientTrackingRange(PROJECTILE_TRACKING_RANGE)
                        .updateInterval(PROJECTILE_UPDATE_INTERVAL)
                        .build(key)
        );
    }

    private static void ensureNotRegistered() {
        if (dairatzEntity != null) {
            throw new IllegalStateException("Dairatz entity types are already registered");
        }
    }

    public static RegistryEntry<EntityType<DairatzEntity>> dairatzEntity() {
        return requireRegistered(dairatzEntity, DAIRATZ_NAME);
    }

    public static RegistryEntry<EntityType<FurballEntity>> furball() {
        return requireRegistered(furball, FURBALL_NAME);
    }

    public static RegistryEntry<EntityType<WinterFairyEntity>> winterFairy() {
        return requireRegistered(winterFairy, WINTER_FAIRY_NAME);
    }

    public static RegistryEntry<EntityType<FrostballEntity>> frostball() {
        return requireRegistered(frostball, FROSTBALL_NAME);
    }

    private static <T> RegistryEntry<T> requireRegistered(
            RegistryEntry<T> entry,
            String name) {
        if (entry == null) {
            throw new IllegalStateException(name + " is not registered yet");
        }
        return entry;
    }
}
