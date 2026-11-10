package ru.nyansus.mc.entity.ai;

import java.util.Comparator;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import ru.nyansus.mc.entity.AbstractFairyEntity;
import ru.nyansus.mc.entity.FairyKind;
import ru.nyansus.mc.entity.FairyPlacement;
import ru.nyansus.mc.entity.HousedFairyEntity;
import ru.nyansus.mc.hollow.FairyActivitySchedule;

final class FairyInterests {
    private static final int SEARCH_RADIUS = 6;

    private FairyInterests() {
    }

    static boolean canAct(AbstractFairyEntity fairy) {
        if (fairy.kind() == FairyKind.STANDARD || fairy.isOnHead() || !fairy.isAlive()
                || fairy.isPassenger() || fairy.getTarget() != null) {
            return false;
        }
        if (fairy instanceof HousedFairyEntity housed && housed.hasHome()) {
            return housed.home().dimension().equals(fairy.level().dimension())
                    && !housed.wantsShelter() && housed.isWithinGarden(fairy.blockPosition());
        }
        if (fairy.isTame()) {
            LivingEntity owner = fairy.getOwner();
            return owner != null && owner.isAlive() && owner.level() == fairy.level()
                    && fairy.distanceToSqr(owner) <= 64;
        }
        return true;
    }

    static Vec3 center(AbstractFairyEntity fairy) {
        if (fairy instanceof HousedFairyEntity housed && housed.hasHome()) {
            return Vec3.atCenterOf(housed.home().pos());
        }
        return fairy.isTame() && fairy.getOwner() != null ? fairy.getOwner().position() : fairy.position();
    }

    static boolean inBounds(AbstractFairyEntity fairy, Vec3 point) {
        if (fairy instanceof HousedFairyEntity housed && housed.hasHome()) {
            return housed.isWithinGarden(BlockPos.containing(point));
        }
        return !fairy.isTame() || center(fairy).distanceToSqr(point) <= 64;
    }

    static boolean hot(BlockState state) {
        return state.is(BlockTags.FIRE) || state.getFluidState().is(FluidTags.LAVA) || state.is(Blocks.MAGMA_BLOCK)
                || (state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE))
                    && state.getValue(BlockStateProperties.LIT);
    }

    static boolean safe(AbstractFairyEntity fairy, Vec3 point) {
        if (!inBounds(fairy, point) || !FairyPlacement.isSafe(fairy.level(), fairy, point)) {
            return false;
        }
        BlockPos feet = BlockPos.containing(point);
        return !hot(fairy.level().getBlockState(feet)) && !hot(fairy.level().getBlockState(feet.below()));
    }

    static FairyInterest choose(AbstractFairyEntity fairy, int patrolIndex) {
        return switch (fairy.kind()) {
            case WINTER -> winter(fairy);
            case JUNGLE -> block(fairy, FairyHabit.JUNGLE_INSPECT,
                    state -> state.is(Blocks.COCOA) || state.is(Blocks.SWEET_BERRY_BUSH)
                            || state.is(Blocks.VINE) || state.is(BlockTags.LEAVES));
            case CHERRY -> cherry(fairy);
            case PALE -> pale(fairy, patrolIndex);
            case ENDER -> ender(fairy);
            default -> null;
        };
    }

    private static FairyInterest winter(AbstractFairyEntity fairy) {
        FairyInterest heat = block(fairy, FairyHabit.AVOID_HEAT, FairyInterests::hot);
        if (heat != null && fairy.position().distanceToSqr(heat.focus()) < 16) {
            return heat;
        }
        BlockPos pos = fairy.blockPosition();
        if (fairy.level().isRaining() && fairy.level().canSeeSky(pos)
                && fairy.level().getBiome(pos).value().getPrecipitationAt(pos, fairy.level().getSeaLevel())
                    == Biome.Precipitation.SNOW) {
            return new FairyInterest(FairyHabit.WINTER_SNOW, fairy.position().add(0, 1, 0), null);
        }
        FairyInterest resting = block(fairy, FairyHabit.WINTER_REST, state ->
                state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.ICE)
                        || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE) || state.is(Blocks.FROSTED_ICE));
        return resting != null ? resting : block(fairy, FairyHabit.WINTER_GLIDE, state -> state.is(Blocks.WATER));
    }

    private static FairyInterest cherry(AbstractFairyEntity fairy) {
        Entity company = fairy.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(BlockPos.containing(center(fairy))).inflate(SEARCH_RADIUS), target ->
                    target != fairy && target.isAlive() && !target.isInvisible()
                            && center(fairy).distanceToSqr(target.position()) <= 36 && friendly(fairy, target))
                .stream().min(Comparator.comparingDouble(fairy::distanceToSqr)).orElse(null);
        return company == null ? block(fairy, FairyHabit.CHERRY_FLOWERS, state -> state.is(BlockTags.FLOWERS))
                : new FairyInterest(FairyHabit.CHERRY_COMPANY, company.position(), company);
    }

    private static boolean friendly(AbstractFairyEntity fairy, LivingEntity target) {
        if (!fairy.isTame()) {
            return target instanceof Animal;
        }
        return fairy.isOwnedBy(target) || target instanceof OwnableEntity pet && pet.getOwnerReference() != null
                && fairy.getOwnerReference() != null
                && pet.getOwnerReference().getUUID().equals(fairy.getOwnerReference().getUUID());
    }

    private static FairyInterest pale(AbstractFairyEntity fairy, int patrolIndex) {
        if (FairyActivitySchedule.isRestTime(fairy.level(), FairyKind.PALE)) {
            return block(fairy, FairyHabit.PALE_SHELTER, state -> state.is(BlockTags.LEAVES));
        }
        double angle = Math.toRadians(Math.floorMod(fairy.getUUID().hashCode(), 360))
                + patrolIndex * Math.PI * 2 / 3;
        Vec3 point = center(fairy).add(Math.cos(angle) * 4, 0.25, Math.sin(angle) * 4);
        return safe(fairy, point) ? new FairyInterest(FairyHabit.PALE_WATCH, point, null) : null;
    }

    private static FairyInterest ender(AbstractFairyEntity fairy) {
        ItemEntity item = fairy.level().getEntitiesOfClass(ItemEntity.class,
                new AABB(BlockPos.containing(center(fairy))).inflate(SEARCH_RADIUS), target ->
                    target.isAlive() && center(fairy).distanceToSqr(target.position()) <= 36)
                .stream().min(Comparator.comparingDouble(fairy::distanceToSqr)).orElse(null);
        return item == null ? block(fairy, FairyHabit.ENDER_INSPECT,
                state -> state.is(Blocks.CHORUS_PLANT) || state.is(Blocks.CHORUS_FLOWER)
                        || state.is(Blocks.END_ROD) || state.is(Blocks.PURPUR_BLOCK))
                : new FairyInterest(FairyHabit.ENDER_INSPECT, item.position(), item);
    }

    static LivingEntity threat(AbstractFairyEntity fairy) {
        int radius = fairy.hasHome() ? 12 : SEARCH_RADIUS;
        return fairy.level().getEntitiesOfClass(Mob.class,
                new AABB(BlockPos.containing(center(fairy))).inflate(radius), target ->
                    target.isAlive() && target instanceof Enemy
                            && center(fairy).distanceToSqr(target.position()) <= radius * radius
                            && (!(target instanceof NeutralMob) || target.getTarget() == fairy
                                || target.getTarget() == fairy.getOwner() && fairy.getOwner() != null))
                .stream().min(Comparator.comparingDouble(fairy::distanceToSqr)).orElse(null);
    }

    private static FairyInterest block(
            AbstractFairyEntity fairy, FairyHabit habit, Predicate<BlockState> matches
    ) {
        BlockPos center = BlockPos.containing(center(fairy));
        BlockPos nearest = null;
        double distance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-SEARCH_RADIUS, -3, -SEARCH_RADIUS),
                center.offset(SEARCH_RADIUS, 3, SEARCH_RADIUS))) {
            if (!fairy.level().hasChunkAt(pos) || !matches.test(fairy.level().getBlockState(pos))) {
                continue;
            }
            double candidate = fairy.distanceToSqr(Vec3.atCenterOf(pos));
            if (candidate < distance) {
                nearest = pos.immutable();
                distance = candidate;
            }
        }
        return nearest == null ? null : new FairyInterest(habit, Vec3.atCenterOf(nearest), null);
    }

    static Vec3 approach(AbstractFairyEntity fairy, FairyInterest interest, int step) {
        Vec3 focus = interest.currentFocus();
        BlockPos block = BlockPos.containing(focus);
        if (!fairy.level().hasChunkAt(block) || interest.subject() != null
                && (interest.subject().level() != fairy.level() || !interest.subject().isAlive())) {
            return null;
        }
        if (interest.habit() == FairyHabit.PALE_WATCH) {
            return safe(fairy, focus) ? focus : null;
        }
        if (interest.habit() == FairyHabit.WINTER_REST) {
            BlockState state = fairy.level().getBlockState(block);
            if (!state.getCollisionShape(fairy.level(), block).isEmpty()) {
                Vec3 surface = new Vec3(block.getX() + 0.5,
                        block.getY() + state.getCollisionShape(fairy.level(), block).max(Direction.Axis.Y) + 0.03,
                        block.getZ() + 0.5);
                return safe(fairy, surface) ? surface : null;
            }
        }
        if (interest.habit() == FairyHabit.PALE_SHELTER
                || interest.habit() == FairyHabit.JUNGLE_INSPECT
                    && fairy.level().getBlockState(block).is(BlockTags.LEAVES)) {
            Vec3 under = focus.add(0, -1.15, 0);
            return safe(fairy, under) ? under : null;
        }
        if (interest.habit() == FairyHabit.AVOID_HEAT) {
            Vec3 away = fairy.position().subtract(focus).multiply(1, 0, 1).normalize();
            if (away.lengthSqr() < 0.01) {
                double angle = Math.toRadians(Math.floorMod(fairy.getUUID().hashCode(), 360));
                away = new Vec3(Math.cos(angle), 0, Math.sin(angle));
            }
            for (int offset = 3; offset <= 5; offset++) {
                Vec3 candidate = fairy.position().add(away.scale(offset)).add(0, 0.5, 0);
                if (candidate.distanceToSqr(focus) > 16 && safe(fairy, candidate)) {
                    return candidate;
                }
            }
            return null;
        }
        double angle = Math.toRadians(Math.floorMod(fairy.getUUID().hashCode(), 360)) + step * Math.PI * 2 / 3;
        for (int attempt = 0; attempt < 6; attempt++) {
            double phase = angle + attempt * Math.PI / 3;
            Vec3 candidate = focus.add(Math.cos(phase) * 1.1, 0.6, Math.sin(phase) * 1.1);
            if (safe(fairy, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    static void moveTo(AbstractFairyEntity fairy, Vec3 destination, double speed) {
        fairy.getNavigation().moveTo(destination.x, destination.y, destination.z, speed);
        Vec3 delta = destination.subtract(fairy.position());
        if (delta.lengthSqr() > 2.25) {
            return;
        }
        AABB swept = fairy.getBoundingBox().expandTowards(delta);
        BlockPos minimum = BlockPos.containing(swept.minX, swept.minY, swept.minZ);
        BlockPos maximum = BlockPos.containing(swept.maxX, swept.maxY, swept.maxZ);
        if (!fairy.level().hasChunksAt(minimum, maximum) || fairy.level().containsAnyLiquid(swept)) {
            return;
        }
        if (!fairy.level().noCollision(fairy, swept)) {
            Vec3 lifted = new Vec3(fairy.getX(), Math.max(fairy.getY() + 0.3, destination.y + 0.15), fairy.getZ());
            AABB ascent = fairy.getBoundingBox().expandTowards(lifted.subtract(fairy.position()));
            if (safe(fairy, lifted) && fairy.level().noCollision(fairy, ascent)
                    && !fairy.level().containsAnyLiquid(ascent)) {
                fairy.getNavigation().stop();
                fairy.getMoveControl().setWantedPosition(lifted.x, lifted.y, lifted.z, speed);
            }
            return;
        }
        for (BlockPos pos : BlockPos.betweenClosed(minimum, maximum)) {
            if (hot(fairy.level().getBlockState(pos))) {
                return;
            }
        }
        fairy.getNavigation().stop();
        fairy.getMoveControl().setWantedPosition(destination.x, destination.y, destination.z, speed);
    }

    static boolean valid(AbstractFairyEntity fairy, FairyInterest interest) {
        if (interest.subject() != null) {
            return interest.subject().isAlive() && interest.subject().level() == fairy.level()
                    && fairy.level().hasChunkAt(interest.subject().blockPosition())
                    && inBounds(fairy, interest.subject().position());
        }
        BlockPos pos = BlockPos.containing(interest.focus());
        if (!fairy.level().hasChunkAt(pos)) {
            return false;
        }
        BlockState state = fairy.level().getBlockState(pos);
        return switch (interest.habit()) {
            case WINTER_REST -> state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.ICE)
                    || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE) || state.is(Blocks.FROSTED_ICE);
            case PALE_SHELTER -> state.is(BlockTags.LEAVES);
            case JUNGLE_INSPECT -> state.is(Blocks.COCOA) || state.is(Blocks.SWEET_BERRY_BUSH)
                    || state.is(Blocks.VINE) || state.is(BlockTags.LEAVES);
            case CHERRY_FLOWERS -> state.is(BlockTags.FLOWERS);
            case ENDER_INSPECT -> state.is(Blocks.CHORUS_PLANT) || state.is(Blocks.CHORUS_FLOWER)
                    || state.is(Blocks.END_ROD) || state.is(Blocks.PURPUR_BLOCK);
            case AVOID_HEAT -> hot(state);
            default -> true;
        };
    }
}
