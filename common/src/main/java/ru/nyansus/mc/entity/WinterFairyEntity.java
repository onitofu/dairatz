package ru.nyansus.mc.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import ru.nyansus.mc.config.DairatzConfig;

public class WinterFairyEntity extends AbstractFairyEntity {
    private static final int FREEZE_AROUND_INTERVAL = 20;
    private static final int FREEZE_AROUND_Y_RANGE = 2;

    public WinterFairyEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected Item getTameItem() {
        return Items.SWEET_BERRIES;
    }

    @Override
    protected int getFireRate() {
        return DairatzConfig.winterFireRate;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createFairyAttributes(
                DairatzConfig.winterFairyHealth, DairatzConfig.winterFlySpeed);
    }

    @Override
    protected void onHeadTick(LivingEntity owner) {
        if (owner.isOnFire() && !level().isClientSide()) {
            owner.setRemainingFireTicks(0);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            if (isOnHead()) {
                freezeWaterFrostWalker();
            } else if (tickCount % FREEZE_AROUND_INTERVAL == 0) {
                freezeWaterAround();
            }
        }
    }

    private void freezeWaterFrostWalker() {
        LivingEntity owner = getOwner();
        if (owner == null) {
            return;
        }
        int radius = DairatzConfig.winterFreezeRadius;
        BlockPos feetPos = owner.blockPosition();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) {
                    continue;
                }
                freezeWaterAt(feetPos.offset(dx, -1, dz));
            }
        }
    }

    private void freezeWaterAround() {
        int radius = DairatzConfig.winterFreezeRadius;
        BlockPos center = blockPosition();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -FREEZE_AROUND_Y_RANGE; dy <= FREEZE_AROUND_Y_RANGE; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz > radius * radius) {
                        continue;
                    }
                    freezeWaterAt(center.offset(dx, dy, dz));
                }
            }
        }
    }

    private void freezeWaterAt(BlockPos pos) {
        BlockState state = level().getBlockState(pos);
        if (state.is(Blocks.WATER)
                && state.getFluidState().isSource()
                && level().getBlockState(pos.above()).isAir()) {
            level().setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
        }
    }

    @Override
    protected ThrowableItemProjectile createProjectile() {
        return new IceFurballEntity(level(), this);
    }
}
