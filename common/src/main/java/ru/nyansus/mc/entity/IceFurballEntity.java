package ru.nyansus.mc.entity;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ru.nyansus.mc.config.DairatzConfig;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.ModItems;

public class IceFurballEntity extends AbstractFairyProjectile {
    private static final float FIRE_MOB_DAMAGE_MULTIPLIER = 3.0f;

    public IceFurballEntity(
            EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public IceFurballEntity(Level level, LivingEntity shooter) {
        super(
                ModEntities.iceFurball().get(),
                shooter,
                level,
                new ItemStack(ModItems.iceFurball().get())
        );
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.iceFurball().get();
    }

    @Override
    protected void applyHitEffect(Entity target) {
        target.hurt(damageSources().thrown(this, getOwner()),
                damageAgainst(target));
        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(
                    MobEffects.SLOWNESS,
                    DairatzConfig.winterSlownessDuration,
                    DairatzConfig.winterSlownessLevel - 1
            ));
        }
    }

    private float damageAgainst(Entity target) {
        float damage = (float) DairatzConfig.winterSnowballDamage;
        if (target.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
            return damage * FIRE_MOB_DAMAGE_MULTIPLIER;
        }
        return damage;
    }
}
