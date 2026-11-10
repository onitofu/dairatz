package ru.nyansus.mc.entity;

import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import ru.nyansus.mc.registry.ModEntities;

public final class NewFairyEntity extends HousedFairyEntity {
    private Set<UUID> previousThreats = Set.of();
    private long nextAlarm;

    public boolean noticeThreats(Set<UUID> threats, long now) {
        boolean fresh = threats.stream().anyMatch(id -> !previousThreats.contains(id));
        previousThreats = Set.copyOf(threats);
        if (fresh && now >= nextAlarm) {
            nextAlarm = now + 100;
            return true;
        }
        return false;
    }

    public NewFairyEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes(FairyKind kind) {
        return createFairyAttributes(kind.settings().health(), kind.settings().flySpeed());
    }

    @Override
    public FairyKind kind() {
        return ModEntities.kindOf(getType());
    }

    @Override
    protected Item getTameItem() {
        return kind().food();
    }

    @Override
    protected int getFireRate() {
        return kind().settings().fireRate();
    }

    @Override
    protected ThrowableItemProjectile createProjectile() {
        return new VariantFurballEntity(level(), this);
    }
}
