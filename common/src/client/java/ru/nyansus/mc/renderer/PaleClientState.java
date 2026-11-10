package ru.nyansus.mc.renderer;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import ru.nyansus.mc.network.PaleDetectionPayload;
import ru.nyansus.mc.network.PaleTarget;

public final class PaleClientState {
    private static ClientLevel world;
    private static List<PaleTarget> targets = List.of();
    private static long expires;

    private PaleClientState() {
    }

    public static void receive(PaleDetectionPayload payload) {
        ClientLevel current = Minecraft.getInstance().level;
        if (current == null || !current.dimension().identifier().equals(payload.dimension())) {
            clear();
            return;
        }
        world = current;
        targets = payload.targets();
        expires = current.getGameTime() + 40;
    }

    public static boolean detected(Entity entity) {
        ClientLevel current = Minecraft.getInstance().level;
        if (current == null || current != world || current.getGameTime() >= expires) {
            clear();
            return false;
        }
        return entity.level() == current && targets.stream()
                .anyMatch(target -> target.entityId() == entity.getId() && target.uuid().equals(entity.getUUID()));
    }

    public static void clear() {
        world = null;
        targets = List.of();
        expires = 0;
    }
}
