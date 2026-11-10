package ru.nyansus.mc.registry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.EntityType;
import ru.nyansus.mc.entity.FairyKind;
import ru.nyansus.mc.entity.NewFairyEntity;
import ru.nyansus.mc.entity.VariantFurballEntity;
import net.minecraft.world.entity.MobCategory;
import ru.nyansus.mc.entity.DairatzEntity;
import ru.nyansus.mc.entity.FurballEntity;
import ru.nyansus.mc.entity.IceFurballEntity;
import ru.nyansus.mc.entity.WinterFairyEntity;

public final class ModEntities {
    public static final String DAIRATZ_NAME = "dairatz_entity";
    public static final String FURBALL_NAME = "furball";
    public static final String WINTER_FAIRY_NAME = "winter_fairy";
    // Preserve the 1.1.0 registry ID while using the clearer Ice Furball name in code.
    public static final String ICE_FURBALL_NAME = "frostball";

    private static final float FAIRY_HITBOX_WIDTH = 0.5f;
    private static final float FAIRY_HITBOX_HEIGHT = 0.5f;
    private static final int FAIRY_TRACKING_RANGE = 8;

    private static final float PROJECTILE_HITBOX_WIDTH = 0.25f;
    private static final float PROJECTILE_HITBOX_HEIGHT = 0.25f;
    private static final int PROJECTILE_TRACKING_RANGE = 4;
    private static final int PROJECTILE_UPDATE_INTERVAL = 10;

    private static final Map<FairyKind, RegistryEntry<EntityType<NewFairyEntity>>> FAIRIES =
            new EnumMap<>(FairyKind.class);
    private static final Map<FairyKind, RegistryEntry<EntityType<VariantFurballEntity>>> PROJECTILES =
            new EnumMap<>(FairyKind.class);

    public static List<FairyKind> newKinds() {
        return List.of(FairyKind.JUNGLE, FairyKind.CHERRY, FairyKind.PALE, FairyKind.ENDER);
    }

    public static EntityType<NewFairyEntity> fairy(FairyKind kind) {
        return FAIRIES.get(kind).get();
    }

    public static EntityType<VariantFurballEntity> projectile(FairyKind kind) {
        return PROJECTILES.get(kind).get();
    }

    public static FairyKind kindOf(EntityType<?> type) {
        for (FairyKind kind : newKinds()) {
            if (fairy(kind) == type || projectile(kind) == type) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Unregistered fairy type");
    }

    private static RegistryEntry<EntityType<DairatzEntity>> dairatzEntity;
    private static RegistryEntry<EntityType<FurballEntity>> furball;
    private static RegistryEntry<EntityType<WinterFairyEntity>> winterFairy;
    private static RegistryEntry<EntityType<IceFurballEntity>> iceFurball;

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

        iceFurball = registrar.registerEntityType(
                ICE_FURBALL_NAME,
                key -> EntityType.Builder.<IceFurballEntity>of(
                                IceFurballEntity::new, MobCategory.MISC)
                        .sized(PROJECTILE_HITBOX_WIDTH, PROJECTILE_HITBOX_HEIGHT)
                        .clientTrackingRange(PROJECTILE_TRACKING_RANGE)
                        .updateInterval(PROJECTILE_UPDATE_INTERVAL)
                        .build(key)
        );

        for (FairyKind kind : newKinds()) {
            FAIRIES.put(kind, registrar.registerEntityType(kind.id() + "_fairy",
                    key -> EntityType.Builder.of(NewFairyEntity::new, MobCategory.CREATURE)
                            .sized(FAIRY_HITBOX_WIDTH, FAIRY_HITBOX_HEIGHT)
                            .clientTrackingRange(FAIRY_TRACKING_RANGE).build(key)));
            PROJECTILES.put(kind, registrar.registerEntityType(kind.id() + "_furball",
                    key -> EntityType.Builder.<VariantFurballEntity>of(VariantFurballEntity::new, MobCategory.MISC)
                            .sized(PROJECTILE_HITBOX_WIDTH, PROJECTILE_HITBOX_HEIGHT)
                            .clientTrackingRange(PROJECTILE_TRACKING_RANGE)
                            .updateInterval(PROJECTILE_UPDATE_INTERVAL).build(key)));
        }
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

    public static RegistryEntry<EntityType<IceFurballEntity>> iceFurball() {
        return requireRegistered(iceFurball, ICE_FURBALL_NAME);
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
