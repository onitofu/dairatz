package ru.nyansus.mc.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

public final class FairyAmbientParticles {
    private static final int EMISSION_INTERVAL_TICKS = 16;
    private static final ParticleOptions PINK_SPARK = new DustParticleOptions(0xFF85CA, 0.65f);

    private FairyAmbientParticles() {
    }

    public static void tick(AbstractFairyEntity fairy) {
        if (!fairy.level().isClientSide() || !fairy.isAlive() || fairy.isOnHead()
                || fairy.isInvisible() || fairy.isResting() || fairy.onGround()
                || Math.floorMod(fairy.tickCount + fairy.getId(), EMISSION_INTERVAL_TICKS) != 0) {
            return;
        }
        ParticleOptions particle = switch (fairy.kind()) {
            case STANDARD -> PINK_SPARK;
            case WINTER -> ParticleTypes.SNOWFLAKE;
            case JUNGLE -> ParticleTypes.SPORE_BLOSSOM_AIR;
            case CHERRY -> ParticleTypes.CHERRY_LEAVES;
            case PALE -> ParticleTypes.PALE_OAK_LEAVES;
            case ENDER -> ParticleTypes.REVERSE_PORTAL;
        };
        Vec3 movement = fairy.getDeltaMovement();
        double angle = fairy.getRandom().nextDouble() * Math.PI * 2;
        double radius = fairy.getBbWidth() * (0.65 + fairy.getRandom().nextDouble() * 0.2);
        double offsetX = Math.cos(angle);
        double offsetZ = Math.sin(angle);
        fairy.level().addParticle(particle,
                fairy.getX() - movement.x * 0.5 + offsetX * radius,
                fairy.getY() + fairy.getBbHeight() * 0.7,
                fairy.getZ() - movement.z * 0.5 + offsetZ * radius,
                offsetX * 0.01, 0.01, offsetZ * 0.01);
    }
}
