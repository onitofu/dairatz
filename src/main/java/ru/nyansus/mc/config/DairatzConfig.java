package ru.nyansus.mc.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DairatzConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("dairatz.json");

    public static double fairyHealth = 16.0;
    public static double furballDamage = 2.0;
    public static int fireRate = 40;
    public static double flySpeed = 0.4;

    private static class Data {
        double fairyHealth = 16.0;
        double furballDamage = 2.0;
        int fireRate = 40;
        double flySpeed = 0.4;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                Data data = GSON.fromJson(json, Data.class);
                if (data != null) {
                    fairyHealth = data.fairyHealth;
                    furballDamage = data.furballDamage;
                    fireRate = data.fireRate;
                    flySpeed = data.flySpeed;
                }
            } catch (Exception e) {
                e.printStackTrace();
                save();
            }
        } else {
            save();
        }
    }

    public static void save() {
        try {
            Data data = new Data();
            data.fairyHealth = fairyHealth;
            data.furballDamage = furballDamage;
            data.fireRate = fireRate;
            data.flySpeed = flySpeed;
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
