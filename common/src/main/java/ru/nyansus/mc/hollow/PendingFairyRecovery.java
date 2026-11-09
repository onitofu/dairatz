package ru.nyansus.mc.hollow;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;

public record PendingFairyRecovery(UUID resident, GlobalPos origin, CompoundTag snapshot) {
    public static final Codec<PendingFairyRecovery> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("Resident").forGetter(PendingFairyRecovery::resident),
        GlobalPos.CODEC.fieldOf("Origin").forGetter(PendingFairyRecovery::origin),
        CompoundTag.CODEC.fieldOf("Snapshot").forGetter(PendingFairyRecovery::snapshot)
    ).apply(instance, PendingFairyRecovery::new));

    public PendingFairyRecovery {
        snapshot = snapshot.copy();
    }

    @Override
    public CompoundTag snapshot() {
        return snapshot.copy();
    }
}
