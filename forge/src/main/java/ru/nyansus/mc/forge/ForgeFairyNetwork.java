package ru.nyansus.mc.forge;

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import ru.nyansus.mc.network.FairyNetwork;
import ru.nyansus.mc.network.PaleDetectionPayload;

final class ForgeFairyNetwork {
    private ForgeFairyNetwork() {
    }

    static void register() {
        SimpleChannel channel = ChannelBuilder.named(Identifier.fromNamespaceAndPath("dairatz", "abilities"))
                .networkProtocolVersion(1).simpleChannel();
        channel.messageBuilder(PaleDetectionPayload.class, 0).direction(PacketFlow.CLIENTBOUND)
                .codec(PaleDetectionPayload.CODEC)
                .consumerMainThread((payload, context) -> FairyNetwork.receive(payload)).add();
        FairyNetwork.installSender((player, payload) -> channel.send(payload, PacketDistributor.PLAYER.with(player)));
    }
}
