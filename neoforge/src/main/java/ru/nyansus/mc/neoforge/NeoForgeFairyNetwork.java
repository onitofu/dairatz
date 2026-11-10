package ru.nyansus.mc.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import ru.nyansus.mc.network.FairyNetwork;
import ru.nyansus.mc.network.PaleDetectionPayload;

final class NeoForgeFairyNetwork {
    private NeoForgeFairyNetwork() {
    }

    static void register(IEventBus bus) {
        bus.addListener(NeoForgeFairyNetwork::registerPayloads);
        FairyNetwork.installSender(PacketDistributor::sendToPlayer);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(PaleDetectionPayload.TYPE, PaleDetectionPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> FairyNetwork.receive(payload)));
    }
}
