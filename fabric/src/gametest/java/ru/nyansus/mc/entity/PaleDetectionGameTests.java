package ru.nyansus.mc.entity;

import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import ru.nyansus.mc.network.PaleDetectionPayload;
import ru.nyansus.mc.registry.ModEntities;

public final class PaleDetectionGameTests {
    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void detectionBelongsOnlyToOwner(GameTestHelper test) {
        var owner = test.makeMockServerPlayerInLevel();
        var observer = test.makeMockServerPlayerInLevel();
        Vec3 center = Vec3.atCenterOf(test.absolutePos(new BlockPos(10, 3, 10)));
        owner.setPos(center);
        observer.setPos(center.add(1, 0, 0));
        NewFairyEntity fairy = test.spawn(ModEntities.fairy(FairyKind.PALE), new BlockPos(10, 3, 10));
        fairy.setNoAi(true);
        fairy.tame(owner);
        fairy.setOnHead(true);
        var zombie = test.spawn(EntityType.ZOMBIE, new BlockPos(10, 3, 13));
        zombie.setNoAi(true);
        test.assertTrue(PaleDetection.snapshotFor(owner).targets().stream()
                .anyMatch(target -> target.uuid().equals(zombie.getUUID())), "Owner must detect nearby threat");
        test.assertTrue(PaleDetection.snapshotFor(observer).targets().isEmpty(),
                "A different player must not receive the owner's threat detection");
        test.assertTrue(!zombie.isCurrentlyGlowing(), "Detection must not set the server glowing flag");
        test.succeed();
    }

    @GameTest
    public void codecReadsIndependentFixture(GameTestHelper test) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            byte[] dimension = "minecraft:overworld".getBytes(StandardCharsets.UTF_8);
            buffer.writeByte(dimension.length);
            buffer.writeBytes(dimension);
            buffer.writeByte(1);
            buffer.writeByte(7);
            buffer.writeLong(0);
            buffer.writeLong(1);
            PaleDetectionPayload decoded = PaleDetectionPayload.CODEC.decode(buffer);
            test.assertTrue(decoded.targets().size() == 1 && decoded.targets().getFirst().entityId() == 7
                    && decoded.targets().getFirst().uuid().equals(new UUID(0, 1)),
                    "Codec must read the independent wire fixture");
        } finally {
            buffer.release();
        }
        test.succeed();
    }

    @GameTest
    public void codecRejectsOversizedSnapshot(GameTestHelper test) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        boolean rejected = false;
        try {
            byte[] dimension = "minecraft:overworld".getBytes(StandardCharsets.UTF_8);
            buffer.writeByte(dimension.length);
            buffer.writeBytes(dimension);
            buffer.writeByte(17);
            try {
                PaleDetectionPayload.CODEC.decode(buffer);
            } catch (IllegalArgumentException expected) {
                rejected = true;
            }
        } finally {
            buffer.release();
        }
        test.assertTrue(rejected, "Oversized snapshots must be rejected before reading targets");
        test.succeed();
    }
}
