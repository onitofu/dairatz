package ru.nyansus.mc.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DairatzConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger(DairatzConfig.class);
    private static volatile DairatzSettings settings = DairatzSettings.DEFAULTS;
    private static Path configPath;

    private DairatzConfig() {
    }

    public static DairatzSettings current() {
        return settings;
    }

    public static boolean load(Path path) {
        configPath = path.toAbsolutePath().normalize();
        if (!Files.exists(configPath)) {
            save();
            return true;
        }
        try {
            JsonObject supplied = JsonParser.parseString(Files.readString(configPath)).getAsJsonObject();
            JsonObject merged = GSON.toJsonTree(DairatzSettings.DEFAULTS).getAsJsonObject();
            for (String key : merged.keySet()) {
                if (supplied.has(key)) {
                    if (supplied.get(key).isJsonNull()) {
                        throw new InvalidSettingsException(key + " must not be null");
                    }
                    merged.add(key, supplied.get(key));
                }
            }
            DairatzSettings candidate = GSON.fromJson(merged, DairatzSettings.class);
            settings = candidate;
            return true;
        } catch (IOException | RuntimeException error) {
            String reason = "Invalid JSON, field types, or unreadable configuration";
            Throwable cause = error;
            for (int depth = 0; cause != null && depth < 16; depth++, cause = cause.getCause()) {
                if (cause instanceof InvalidSettingsException) {
                    reason = cause.getMessage();
                    break;
                }
            }
            LOGGER.warn("Rejected Dairatz configuration; keeping previous settings: {}", reason);
            return false;
        }
    }

    public static void save() {
        if (configPath == null) {
            throw new IllegalStateException("Dairatz configuration path has not been initialized");
        }
        Path temporary = null;
        try {
            Files.createDirectories(configPath.getParent());
            temporary = Files.createTempFile(configPath.getParent(), "dairatz-", ".tmp");
            Files.writeString(temporary, GSON.toJson(settings) + "\n");
            try {
                Files.move(temporary, configPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException error) {
                Files.move(temporary, configPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException error) {
            LOGGER.error("Failed to save Dairatz configuration", error);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException error) {
                    LOGGER.warn("Could not remove temporary Dairatz configuration");
                }
            }
        }
    }
}
