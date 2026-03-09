package ru.nyansus.mc.entity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import ru.nyansus.mc.config.DairatzConfig;
import ru.nyansus.mc.registry.ModEntities;

public class FrostballEntity extends ThrowableItemProjectile {
    public FrostballEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public FrostballEntity(Level level, LivingEntity shooter) {
        super(ModEntities.FROSTBALL, shooter, level, new ItemStack(Items.SNOWBALL));
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SNOWBALL;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        if (target instanceof AbstractFairyEntity) {
            return;
        }
        Entity shooter = getOwner();
        if (target instanceof Player player
                && shooter instanceof AbstractFairyEntity fairy
                && fairy.isOwnedBy(player)) {
            return;
        }
        target.hurt(
                damageSources().thrown(this, getOwner()),
                (float) DairatzConfig.winterSnowballDamage
        );
        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(
                    MobEffects.SLOWNESS,
                    DairatzConfig.winterSlownessDuration,
                    DairatzConfig.winterSlownessLevel - 1
            ));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            discard();
        }
    }
}
