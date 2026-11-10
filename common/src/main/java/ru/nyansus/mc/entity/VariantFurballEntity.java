package ru.nyansus.mc.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.ModItems;

public final class VariantFurballEntity extends AbstractFairyProjectile {
    public VariantFurballEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public VariantFurballEntity(Level level, AbstractFairyEntity shooter) {
        super(ModEntities.projectile(shooter.kind()), shooter, level,
                new ItemStack(ModItems.projectile(shooter.kind())));
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.projectile(ModEntities.kindOf(getType()));
    }

    @Override
    protected void applyHitEffect(Entity target) {
        FairyKind kind = ModEntities.kindOf(getType());
        target.hurt(damageSources().thrown(this, getOwner()), (float) kind.settings().damage());
        FairyAbilities.applyProjectileEffect(kind, target);
    }
}
