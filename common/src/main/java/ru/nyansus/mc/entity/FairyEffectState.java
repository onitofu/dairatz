package ru.nyansus.mc.entity;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class FairyEffectState {
    public long lastDamage = -200;
    public long nextHealing;
    public long nextGardenHealing;
    public long nextPoison;
    public long nextWeakness;
    public long nextEvasion;

    public void save(ValueOutput output) {
        output.putLong("DairatzLastDamage", lastDamage);
        output.putLong("DairatzNextHealing", nextHealing);
        output.putLong("DairatzNextGardenHealing", nextGardenHealing);
        output.putLong("DairatzNextPoison", nextPoison);
        output.putLong("DairatzNextWeakness", nextWeakness);
        output.putLong("DairatzNextEvasion", nextEvasion);
    }

    public void load(ValueInput input) {
        lastDamage = input.getLongOr("DairatzLastDamage", -200);
        nextHealing = input.getLongOr("DairatzNextHealing", 0);
        nextGardenHealing = input.getLongOr("DairatzNextGardenHealing", 0);
        nextPoison = input.getLongOr("DairatzNextPoison", 0);
        nextWeakness = input.getLongOr("DairatzNextWeakness", 0);
        nextEvasion = input.getLongOr("DairatzNextEvasion", 0);
    }
}
