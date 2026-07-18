package ru.nyansus.mc.entity;

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

public class FurballEntity extends AbstractFairyProjectile {
    public FurballEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public FurballEntity(Level level, LivingEntity shooter) {
        super(
                ModEntities.furball().get(),
                shooter,
                level,
                new ItemStack(ModItems.furball().get())
        );
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.furball().get();
    }

    @Override
    protected void applyHitEffect(Entity target) {
        target.hurt(damageSources().thrown(this, getOwner()),
                (float) DairatzConfig.furballDamage);
    }
}
