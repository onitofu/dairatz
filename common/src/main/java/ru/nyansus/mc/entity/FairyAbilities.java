package ru.nyansus.mc.entity;

import java.util.Comparator;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import ru.nyansus.mc.loot.FairyLootAccess;

public final class FairyAbilities {
    private FairyAbilities() {
    }

    public static long time(ServerLevel level) {
        return level.getServer().overworld().getGameTime();
    }

    public static FairyEffectState state(LivingEntity target) {
        return ((FairyLivingAccess) target).fairyEffects();
    }

    public static AbstractFairyEntity headFairy(LivingEntity owner) {
        return owner.level().getEntitiesOfClass(AbstractFairyEntity.class, owner.getBoundingBox().inflate(2),
                fairy -> fairy.isOnHead() && fairy.isOwnedBy(owner)).stream().findFirst().orElse(null);
    }

    public static MobEffectInstance protectFromPoison(LivingEntity target, MobEffectInstance effect) {
        if (!target.level().isClientSide() && effect.is(MobEffects.POISON) && effect.getDuration() > 1) {
            AbstractFairyEntity fairy = headFairy(target);
            if (fairy != null && fairy.kind() == FairyKind.JUNGLE) {
                return new MobEffectInstance(effect.getEffect(), Math.max(1, effect.getDuration() / 2),
                        effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
            }
        }
        return effect;
    }

    public static void applyProjectileEffect(FairyKind kind, Entity target) {
        if (!(target instanceof LivingEntity living) || !(living.level() instanceof ServerLevel level)) {
            return;
        }
        FairyEffectState state = state(living);
        long now = time(level);
        if (kind == FairyKind.JUNGLE && now >= state.nextPoison) {
            if (living.addEffect(new MobEffectInstance(MobEffects.POISON, 60))) {
                state.nextPoison = now + 100;
            }
        } else if (kind == FairyKind.PALE && now >= state.nextWeakness) {
            if (living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40))) {
                state.nextWeakness = now + 160;
            }
        }
    }

    public static void tick(AbstractFairyEntity fairy) {
        if (!(fairy.level() instanceof ServerLevel level) || !fairy.isTame()) {
            return;
        }
        if (fairy.kind() == FairyKind.CHERRY && fairy.tickCount % 20 == 0) {
            if (fairy.isOnHead() && fairy.getOwner() != null) {
                heal(fairy.getOwner(), level);
            } else if (fairy instanceof HousedFairyEntity housed && housed.hasHome() && !housed.wantsShelter()) {
                healGarden(housed, level);
            }
        } else if (fairy.kind() == FairyKind.ENDER && fairy.isOnHead() && fairy.tickCount % 5 == 0
                && fairy.getOwner() instanceof Player owner && !owner.isSpectator()) {
            attractLoot(owner, level);
        }
    }

    private static void healGarden(HousedFairyEntity fairy, ServerLevel level) {
        LivingEntity patient = gardenPatient(fairy);
        if (patient != null && fairy.distanceToSqr(patient) <= 6.25 && fairy.hasLineOfSight(patient)) {
            heal(patient, level);
            state(fairy).nextGardenHealing = time(level) + 200;
        }
    }

    public static LivingEntity gardenPatient(HousedFairyEntity fairy) {
        if (!fairy.hasHome() || !fairy.isTame() || !(fairy.level() instanceof ServerLevel level)
                || !fairy.home().dimension().equals(level.dimension())
                || fairy.wantsShelter() || time(level) < state(fairy).nextGardenHealing) {
            return null;
        }
        BlockPos home = fairy.home().pos();
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(home).inflate(8),
                target -> isGardenPatient(fairy, target))
                .stream().min(Comparator.comparingDouble(target -> target.getHealth() / target.getMaxHealth()))
                .orElse(null);
    }

    public static boolean isGardenPatient(HousedFairyEntity fairy, LivingEntity target) {
        if (!fairy.hasHome() || !(fairy.level() instanceof ServerLevel level) || target.level() != level
                || !fairy.home().dimension().equals(level.dimension())
                || !level.hasChunkAt(target.blockPosition()) || fairy.wantsShelter()
                || time(level) < state(fairy).nextGardenHealing || !canHeal(target, level)) {
            return false;
        }
        UUID owner = fairy.getOwnerReference() == null ? null : fairy.getOwnerReference().getUUID();
        return owner != null && target.distanceToSqr(Vec3.atCenterOf(fairy.home().pos())) <= 64
                && (target.getUUID().equals(owner) || target instanceof OwnableEntity pet
                    && pet.getOwnerReference() != null && owner.equals(pet.getOwnerReference().getUUID()));
    }

    private static boolean canHeal(LivingEntity target, ServerLevel level) {
        FairyEffectState state = state(target);
        return target.isAlive() && target.getHealth() < target.getMaxHealth()
                && time(level) >= state.nextHealing && time(level) - state.lastDamage >= 100
                && (!(target instanceof Player player) || !player.isSpectator());
    }

    private static void heal(LivingEntity target, ServerLevel level) {
        if (!canHeal(target, level)) {
            return;
        }
        target.heal(1);
        state(target).nextHealing = time(level) + 200;
        level.sendParticles(ParticleTypes.HEART,
                target.getX(), target.getY() + target.getBbHeight(), target.getZ(), 2, 0.2, 0.2, 0.2, 0);
    }

    private static void attractLoot(Player owner, ServerLevel level) {
        level.getEntitiesOfClass(ItemEntity.class, owner.getBoundingBox().inflate(6), item ->
                item.isAlive() && !item.hasPickUpDelay() && item.distanceToSqr(owner) <= 36
                        && owner.getUUID().equals(((FairyLootAccess) item).fairyLootOwner())
                        && (((FairyLootAccess) item).fairyPickupOwner() == null
                            || owner.getUUID().equals(((FairyLootAccess) item).fairyPickupOwner()))
                        && (owner.getInventory().getFreeSlot() >= 0
                            || owner.getInventory().getSlotWithRemainingSpace(item.getItem()) >= 0))
                .stream().sorted(Comparator.comparingDouble(owner::distanceToSqr)).limit(16).forEach(item -> {
                    Vec3 direction = owner.position().add(0, 0.5, 0).subtract(item.position());
                    if (direction.lengthSqr() > 0.01) {
                        item.setDeltaMovement(direction.normalize().scale(0.3));
                    }
                });
    }

    public static void afterDamage(LivingEntity target, ServerLevel level, DamageSource source) {
        state(target).lastDamage = time(level);
        if (!(target instanceof AbstractFairyEntity fairy) || fairy.kind() != FairyKind.ENDER
                || fairy.isOnHead() || !fairy.isAlive() || time(level) < state(fairy).nextEvasion
                || !(source.getDirectEntity() instanceof LivingEntity attacker)
                || attacker.distanceToSqr(fairy) > 9) {
            return;
        }
        for (int i = 0; i < 16; i++) {
            Vec3 point = fairy.position().add(fairy.getRandom().nextInt(9) - 4,
                    fairy.getRandom().nextInt(4) - 1, fairy.getRandom().nextInt(9) - 4);
            if (point.distanceToSqr(fairy.position()) > 16 || point.distanceToSqr(attacker.position())
                    <= fairy.distanceToSqr(attacker) || !FairyPlacement.isSafe(level, fairy, point)) {
                continue;
            }
            level.sendParticles(ParticleTypes.PORTAL,
                    fairy.getX(), fairy.getY(), fairy.getZ(), 12, 0.3, 0.3, 0.3, 0.1);
            fairy.teleportTo(point.x, point.y, point.z);
            fairy.getNavigation().stop();
            state(fairy).nextEvasion = time(level) + 100;
            return;
        }
    }
}
