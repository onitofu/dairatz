package ru.nyansus.mc.config;

public record FairySpeciesSettings(
    double health, double damage, int fireRate, double flySpeed, int spawnWeight, int minGroup, int maxGroup
) {
    public FairySpeciesSettings {
        if (!Double.isFinite(health) || health < 1 || health > 1024
                || !Double.isFinite(damage) || damage < 0 || damage > Float.MAX_VALUE
                || fireRate < 1 || !Double.isFinite(flySpeed) || flySpeed < 0 || flySpeed > 1024
                || spawnWeight < 0 || spawnWeight > 1000000 || minGroup < 1 || maxGroup > 64 || minGroup > maxGroup) {
            throw new InvalidSettingsException("Invalid fairy species settings");
        }
    }
}
