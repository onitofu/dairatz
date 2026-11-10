package ru.nyansus.mc.loot;

import java.util.UUID;

public interface FairyLootAccess {
    UUID fairyLootOwner();

    UUID fairyPickupOwner();

    void setFairyLootOwner(UUID owner);
}
