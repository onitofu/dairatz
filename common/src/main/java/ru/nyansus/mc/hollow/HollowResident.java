package ru.nyansus.mc.hollow;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

public record HollowResident(HollowStatus status, UUID id, CompoundTag snapshot) {
    public HollowResident {
        if ((status == HollowStatus.INSIDE && (id == null || snapshot == null))
                || (status == HollowStatus.OUTSIDE && (id == null || snapshot != null))
                || ((status == HollowStatus.EMPTY || status == HollowStatus.NATURAL_PENDING)
                    && (id != null || snapshot != null))) {
            throw new IllegalArgumentException("Inconsistent hollow resident state");
        }
        snapshot = snapshot == null ? null : snapshot.copy();
    }

    @Override
    public CompoundTag snapshot() {
        return snapshot == null ? null : snapshot.copy();
    }

    public static HollowResident empty() {
        return new HollowResident(HollowStatus.EMPTY, null, null);
    }

    public static HollowResident naturalPending() {
        return new HollowResident(HollowStatus.NATURAL_PENDING, null, null);
    }

    public static HollowResident inside(UUID id, CompoundTag snapshot) {
        return new HollowResident(HollowStatus.INSIDE, id, snapshot);
    }

    public static HollowResident outside(UUID id) {
        return new HollowResident(HollowStatus.OUTSIDE, id, null);
    }

    public static HollowResident restore(UUID id, CompoundTag snapshot, boolean pending) {
        if (snapshot != null) {
            UUID encoded = snapshot.get("UUID") == null ? null
                    : UUIDUtil.CODEC.parse(NbtOps.INSTANCE, snapshot.get("UUID")).result().orElse(null);
            UUID identity = encoded == null ? id : encoded;
            if (identity == null) {
                throw new IllegalArgumentException("Stored resident has no UUID");
            }
            CompoundTag normalized = snapshot.copy();
            normalized.store("UUID", UUIDUtil.CODEC, identity);
            return inside(identity, normalized);
        }
        if (id != null) {
            return outside(id);
        }
        return pending ? naturalPending() : empty();
    }
}
