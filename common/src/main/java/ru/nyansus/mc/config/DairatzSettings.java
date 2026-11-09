package ru.nyansus.mc.config;

public record DairatzSettings(
    double fairyHealth,
    double furballDamage,
    int fireRate,
    double flySpeed,
    int dairatzSpawnWeight,
    int dairatzMinGroup,
    int dairatzMaxGroup,
    double winterFairyHealth,
    double winterSnowballDamage,
    int winterFireRate,
    double winterFlySpeed,
    int winterSlownessDuration,
    int winterSlownessLevel,
    int winterFreezeRadius,
    int winterSpawnWeight,
    int winterMinGroup,
    int winterMaxGroup,
    double healAmount,
    int tameChance
) {
    public static final DairatzSettings DEFAULTS = new DairatzSettings(
        16.0, 2.0, 60, 0.4, 10, 2, 4, 16.0, 1.0, 60, 0.4, 60, 2, 2, 8, 2, 3, 4.0, 3
    );

    public DairatzSettings {
        range("fairyHealth", fairyHealth, 1, 1024);
        range("furballDamage", furballDamage, 0, Float.MAX_VALUE);
        range("fireRate", fireRate, 1, Integer.MAX_VALUE);
        range("flySpeed", flySpeed, 0, 1024);
        range("dairatzSpawnWeight", dairatzSpawnWeight, 0, 1000000);
        range("dairatzMinGroup", dairatzMinGroup, 1, 64);
        range("dairatzMaxGroup", dairatzMaxGroup, 1, 64);
        range("winterFairyHealth", winterFairyHealth, 1, 1024);
        range("winterSnowballDamage", winterSnowballDamage, 0, Float.MAX_VALUE / 3.0);
        range("winterFireRate", winterFireRate, 1, Integer.MAX_VALUE);
        range("winterFlySpeed", winterFlySpeed, 0, 1024);
        range("winterSlownessDuration", winterSlownessDuration, 0, Integer.MAX_VALUE);
        range("winterSlownessLevel", winterSlownessLevel, 1, 256);
        range("winterFreezeRadius", winterFreezeRadius, 0, 8);
        range("winterSpawnWeight", winterSpawnWeight, 0, 1000000);
        range("winterMinGroup", winterMinGroup, 1, 64);
        range("winterMaxGroup", winterMaxGroup, 1, 64);
        range("healAmount", healAmount, 0, Float.MAX_VALUE);
        range("tameChance", tameChance, 1, Integer.MAX_VALUE);
        if (!Float.isFinite((float) winterSnowballDamage * 3.0f)) {
            throw new InvalidSettingsException("Winter damage multiplier exceeds the supported range");
        }
        if (dairatzMinGroup > dairatzMaxGroup || winterMinGroup > winterMaxGroup) {
            throw new InvalidSettingsException("Spawn group minimum must not exceed maximum");
        }
    }

    private static void range(String field, double value, double minimum, double maximum) {
        if (!Double.isFinite(value) || value < minimum || value > maximum) {
            throw new InvalidSettingsException(field + " is outside its supported range");
        }
    }
}
