package ru.nyansus.mc.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import ru.nyansus.mc.config.DairatzConfig;

public class WinterFairyEntity extends AbstractFairyEntity {
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
            } else if (tickCount % 20 == 0) {
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
            for (int dy = -2; dy <= 2; dy++) {
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
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        FrostballEntity frostball = new FrostballEntity(level(), this);
        double dx = target.getX() - getX();
        double dy = target.getEyeY() - getEyeY();
        double dz = target.getZ() - getZ();
        frostball.shoot(dx, dy, dz, 1.5f, 1.0f);
        serverLevel.addFreshEntity(frostball);
        playSound(net.minecraft.sounds.SoundEvents.SNOWBALL_THROW, 1.0f,
                1.0f / (getRandom().nextFloat() * 0.4f + 0.8f));
    }
}
