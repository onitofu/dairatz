package ru.nyansus.mc.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import ru.nyansus.mc.entity.ai.FairyGardenWanderGoal;
import ru.nyansus.mc.entity.ai.FairyPollinateGoal;
import ru.nyansus.mc.entity.ai.FairyReturnHomeGoal;
import ru.nyansus.mc.hollow.FairyActivitySchedule;
import ru.nyansus.mc.hollow.FairyHollowBlockEntity;
import ru.nyansus.mc.hollow.FairyRecoveryJournal;

public abstract class HousedFairyEntity extends AbstractFairyEntity {
    public static final int GARDEN_RADIUS = 8;
    public static final int GARDEN_HEIGHT = 3;

    private GlobalPos home;
    private long outsideUntil;
    private long nextPollinationTick;

    protected HousedFairyEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(2, new FairyReturnHomeGoal(this));
        goalSelector.addGoal(4, new FairyPollinateGoal(this));
        goalSelector.addGoal(6, new FairyGardenWanderGoal(this));
    }

    @Override
    public boolean hasHome() {
        return home != null;
    }

    public GlobalPos home() {
        return home;
    }

    public void assignHome(BlockPos pos) {
        if (kind() == FairyKind.ENDER) {
            return;
        }
        home = GlobalPos.of(level().dimension(), pos.immutable());
        setOnHead(false);
        setTarget(null);
        getNavigation().stop();
    }

    @Override
    public void releaseHome() {
        GlobalPos previous = home;
        home = null;
        if (previous != null && level() instanceof ServerLevel server) {
            FairyRecoveryJournal.get(server.getServer()).releaseHome(server.getServer(), previous, getUUID());
        }
    }

    public void stayOutside() {
        outsideUntil = level().getGameTime() + 200;
    }

    public boolean wantsShelter() {
        if (!hasHome() || level().getGameTime() < outsideUntil) {
            return false;
        }
        return FairyActivitySchedule.isRestTime(level(), kind());
    }

    public boolean isWithinGarden(BlockPos pos) {
        if (home == null) {
            return true;
        }
        long dx = (long) pos.getX() - home.pos().getX();
        long dz = (long) pos.getZ() - home.pos().getZ();
        return dx * dx + dz * dz <= GARDEN_RADIUS * GARDEN_RADIUS
                && Math.abs(pos.getY() - home.pos().getY()) <= GARDEN_HEIGHT;
    }

    public long nextPollinationTick() {
        return nextPollinationTick;
    }

    public void delayPollination(int ticks) {
        nextPollinationTick = level().getGameTime() + ticks;
    }

    public boolean canPollinate() {
        return (kind() == FairyKind.STANDARD || kind() == FairyKind.JUNGLE) && !isOnHead()
                && ((!isTame() && !hasHome()) || (hasHome() && !wantsShelter()));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && home != null && tickCount % 20 == 0) {
            if (!home.dimension().equals(level().dimension())) {
                releaseHome();
            } else if (level().hasChunkAt(home.pos())) {
                if (!(level().getBlockEntity(home.pos()) instanceof FairyHollowBlockEntity hollow)
                        || !hollow.isResident(getUUID())) {
                    releaseHome();
                }
            }
        }
    }

    @Override
    public void die(DamageSource source) {
        releaseHome();
        super.die(source);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (home != null) {
            output.store("FairyHome", GlobalPos.CODEC, home);
        }
        output.putLong("OutsideUntil", outsideUntil);
        output.putLong("NextPollinationTick", nextPollinationTick);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        home = input.read("FairyHome", GlobalPos.CODEC).orElse(null);
        outsideUntil = input.getLongOr("OutsideUntil", 0);
        nextPollinationTick = input.getLongOr("NextPollinationTick", 0);
    }
}
