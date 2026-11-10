package ru.nyansus.mc.network;

import java.util.UUID;

public record PaleTarget(int entityId, UUID uuid) {
    public PaleTarget {
        if (entityId < 0 || uuid == null) {
            throw new IllegalArgumentException("Invalid detection target");
        }
    }
}
