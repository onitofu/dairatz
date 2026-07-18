package ru.nyansus.mc.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public abstract class AbstractFairyProjectile extends ThrowableItemProjectile {
    protected AbstractFairyProjectile(
            EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    protected AbstractFairyProjectile(
            EntityType<? extends ThrowableItemProjectile> type,
            LivingEntity shooter, Level level, ItemStack stack) {
        super(type, shooter, level, stack);
    }

    protected boolean shouldSkipTarget(Entity target) {
        if (target instanceof AbstractFairyEntity) {
            return true;
        }
        Entity shooter = getOwner();
        return target instanceof Player player
                && shooter instanceof AbstractFairyEntity fairy
                && fairy.isOwnedBy(player);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        if (!shouldSkipTarget(target)) {
            applyHitEffect(target);
        }
    }

    protected abstract void applyHitEffect(Entity target);

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            discard();
        }
    }
}
