package ru.nyansus.mc.entity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import ru.nyansus.mc.config.DairatzConfig;
import ru.nyansus.mc.registry.ModEntities;

public class FrostballEntity extends AbstractFairyProjectile {
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
    protected void applyHitEffect(Entity target) {
        target.hurt(damageSources().thrown(this, getOwner()),
                (float) DairatzConfig.winterSnowballDamage);
        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(
                    MobEffects.SLOWNESS,
                    DairatzConfig.winterSlownessDuration,
                    DairatzConfig.winterSlownessLevel - 1
            ));
        }
    }
}
