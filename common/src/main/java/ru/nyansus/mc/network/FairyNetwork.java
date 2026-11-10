package ru.nyansus.mc.network;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;

public final class FairyNetwork {
    private static BiConsumer<ServerPlayer, PaleDetectionPayload> sender;
    private static Consumer<PaleDetectionPayload> receiver;

    private FairyNetwork() {
    }

    public static void installSender(BiConsumer<ServerPlayer, PaleDetectionPayload> transport) {
        sender = transport;
    }

    public static void installReceiver(Consumer<PaleDetectionPayload> handler) {
        receiver = handler;
    }

    public static void send(ServerPlayer player, PaleDetectionPayload payload) {
        if (sender == null) {
            throw new IllegalStateException("Fairy networking was not initialized");
        }
        sender.accept(player, payload);
    }

    public static void receive(PaleDetectionPayload payload) {
        if (receiver != null) {
            receiver.accept(payload);
        }
    }
}
