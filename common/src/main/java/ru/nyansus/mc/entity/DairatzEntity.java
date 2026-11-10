package ru.nyansus.mc.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import ru.nyansus.mc.config.DairatzConfig;

public class DairatzEntity extends HousedFairyEntity {
    public DairatzEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected Item getTameItem() {
        return Items.POPPY;
    }

    @Override
    protected int getFireRate() {
        return DairatzConfig.current().fireRate();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createFairyAttributes(DairatzConfig.current().fairyHealth(), DairatzConfig.current().flySpeed());
    }

    @Override
    protected ThrowableItemProjectile createProjectile() {
        return new FurballEntity(level(), this);
    }

}
