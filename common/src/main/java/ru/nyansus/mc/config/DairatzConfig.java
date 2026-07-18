package ru.nyansus.mc.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DairatzConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger(DairatzConfig.class);
    private static Path configPath;

    public static double fairyHealth = 16.0;
    public static double furballDamage = 2.0;
    public static int fireRate = 60;
    public static double flySpeed = 0.4;
    public static int dairatzSpawnWeight = 10;
    public static int dairatzMinGroup = 2;
    public static int dairatzMaxGroup = 4;

    public static double winterFairyHealth = 16.0;
    public static double winterSnowballDamage = 1.0;
    public static int winterFireRate = 60;
    public static double winterFlySpeed = 0.4;
    public static int winterSlownessDuration = 60;
    public static int winterSlownessLevel = 2;
    public static int winterFreezeRadius = 2;
    public static int winterSpawnWeight = 8;
    public static int winterMinGroup = 2;
    public static int winterMaxGroup = 3;

    public static double healAmount = 4.0;
    public static int tameChance = 3;

    @SuppressWarnings("checkstyle:MemberName")
    private static class Data {
        double fairyHealth = 16.0;
        double furballDamage = 2.0;
        int fireRate = 60;
        double flySpeed = 0.4;
        int dairatzSpawnWeight = 10;
        int dairatzMinGroup = 2;
        int dairatzMaxGroup = 4;

        double winterFairyHealth = 16.0;
        double winterSnowballDamage = 1.0;
        int winterFireRate = 60;
        double winterFlySpeed = 0.4;
        int winterSlownessDuration = 60;
        int winterSlownessLevel = 2;
        int winterFreezeRadius = 2;
        int winterSpawnWeight = 8;
        int winterMinGroup = 2;
        int winterMaxGroup = 3;

        double healAmount = 4.0;
        int tameChance = 3;
    }

    public static void load(Path path) {
        configPath = path;
        if (Files.exists(configPath)) {
            try {
                String json = Files.readString(configPath);
                Data data = GSON.fromJson(json, Data.class);
                if (data != null) {
                    applyData(data);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load Dairatz config from {}", configPath, e);
                save();
            }
        } else {
            save();
        }
    }

    private static void applyData(Data data) {
        fairyHealth = data.fairyHealth;
        furballDamage = data.furballDamage;
        fireRate = data.fireRate;
        flySpeed = data.flySpeed;
        dairatzSpawnWeight = data.dairatzSpawnWeight;
        dairatzMinGroup = data.dairatzMinGroup;
        dairatzMaxGroup = data.dairatzMaxGroup;

        winterFairyHealth = data.winterFairyHealth;
        winterSnowballDamage = data.winterSnowballDamage;
        winterFireRate = data.winterFireRate;
        winterFlySpeed = data.winterFlySpeed;
        winterSlownessDuration = data.winterSlownessDuration;
        winterSlownessLevel = data.winterSlownessLevel;
        winterFreezeRadius = data.winterFreezeRadius;
        winterSpawnWeight = data.winterSpawnWeight;
        winterMinGroup = data.winterMinGroup;
        winterMaxGroup = data.winterMaxGroup;

        healAmount = data.healAmount;
        tameChance = data.tameChance;
    }

    public static void save() {
        if (configPath == null) {
            throw new IllegalStateException("Dairatz config path has not been initialized");
        }
        try {
            Data data = new Data();
            data.fairyHealth = fairyHealth;
            data.furballDamage = furballDamage;
            data.fireRate = fireRate;
            data.flySpeed = flySpeed;
            data.dairatzSpawnWeight = dairatzSpawnWeight;
            data.dairatzMinGroup = dairatzMinGroup;
            data.dairatzMaxGroup = dairatzMaxGroup;

            data.winterFairyHealth = winterFairyHealth;
            data.winterSnowballDamage = winterSnowballDamage;
            data.winterFireRate = winterFireRate;
            data.winterFlySpeed = winterFlySpeed;
            data.winterSlownessDuration = winterSlownessDuration;
            data.winterSlownessLevel = winterSlownessLevel;
            data.winterFreezeRadius = winterFreezeRadius;
            data.winterSpawnWeight = winterSpawnWeight;
            data.winterMinGroup = winterMinGroup;
            data.winterMaxGroup = winterMaxGroup;

            data.healAmount = healAmount;
            data.tameChance = tameChance;

            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(data));
        } catch (IOException e) {
            LOGGER.error("Failed to save Dairatz config to {}", configPath, e);
        }
    }
}
