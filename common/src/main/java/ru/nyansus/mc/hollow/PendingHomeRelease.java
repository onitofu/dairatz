package ru.nyansus.mc.hollow;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;

public record PendingHomeRelease(GlobalPos home, UUID resident) {
    public static final Codec<PendingHomeRelease> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        GlobalPos.CODEC.fieldOf("Home").forGetter(PendingHomeRelease::home),
        UUIDUtil.CODEC.fieldOf("Resident").forGetter(PendingHomeRelease::resident)
    ).apply(instance, PendingHomeRelease::new));
}
