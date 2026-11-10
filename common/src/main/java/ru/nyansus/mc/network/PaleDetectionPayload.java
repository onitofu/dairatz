package ru.nyansus.mc.network;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PaleDetectionPayload(Identifier dimension, List<PaleTarget> targets) implements CustomPacketPayload {
    public static final Type<PaleDetectionPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("dairatz", "pale_detection"));
    public static final StreamCodec<FriendlyByteBuf, PaleDetectionPayload> CODEC = StreamCodec.of(
            PaleDetectionPayload::encode, PaleDetectionPayload::decode);

    public PaleDetectionPayload {
        targets = List.copyOf(targets);
        if (dimension == null || targets.size() > 16) {
            throw new IllegalArgumentException("Invalid detection snapshot");
        }
    }

    private static PaleDetectionPayload decode(FriendlyByteBuf buffer) {
        Identifier dimension = buffer.readIdentifier();
        int count = buffer.readVarInt();
        if (count < 0 || count > 16) {
            throw new IllegalArgumentException("Detection snapshot exceeds limit");
        }
        List<PaleTarget> targets = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            targets.add(new PaleTarget(buffer.readVarInt(), buffer.readUUID()));
        }
        return new PaleDetectionPayload(dimension, targets);
    }

    private static void encode(FriendlyByteBuf buffer, PaleDetectionPayload payload) {
        buffer.writeIdentifier(payload.dimension());
        buffer.writeVarInt(payload.targets().size());
        for (PaleTarget target : payload.targets()) {
            buffer.writeVarInt(target.entityId());
            buffer.writeUUID(target.uuid());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
