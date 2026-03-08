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

    public static double fairyHealth = 20.0;
    public static double furballDamage = 4.0;
    public static int fireRate = 20;

    private static class Data {
        double fairyHealth = 20.0;
        double furballDamage = 4.0;
        int fireRate = 20;
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
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
