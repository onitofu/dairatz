package ru.nyansus.mc.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import ru.nyansus.mc.config.DairatzConfig;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.ModItems;

public class FurballEntity extends ThrowableItemProjectile {
    public FurballEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public FurballEntity(Level level, LivingEntity shooter) {
        super(ModEntities.FURBALL, shooter, level, new ItemStack(ModItems.FURBALL));
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.FURBALL;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        if (target instanceof DairatzEntity) {
            return;
        }
        if (target instanceof Player && target.equals(getOwner())) {
            return;
        }
        target.hurt(damageSources().thrown(this, getOwner()), (float) DairatzConfig.furballDamage);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            discard();
        }
    }
}
