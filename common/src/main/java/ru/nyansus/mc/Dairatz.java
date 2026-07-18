package ru.nyansus.mc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nyansus.mc.config.DairatzConfig;
import ru.nyansus.mc.registry.ContentRegistrar;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.ModItems;

import java.nio.file.Path;

public final class Dairatz {
    public static final String MOD_ID = "dairatz";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private Dairatz() {
    }

    public static void initialize(ContentRegistrar registrar, Path configDirectory) {
        DairatzConfig.load(configDirectory.resolve("dairatz.json"));
        ModEntities.register(registrar);
        ModItems.register(registrar);
        LOGGER.info("Dairatz common content initialized");
    }
}
