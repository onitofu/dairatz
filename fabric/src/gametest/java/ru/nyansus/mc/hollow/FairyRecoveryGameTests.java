package ru.nyansus.mc.hollow;

import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import ru.nyansus.mc.entity.DairatzEntity;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.ModHollows;

public final class FairyRecoveryGameTests {
    private static final BlockPos HOME = new BlockPos(3, 3, 3);

    @GameTest(environment = "dairatz-test:night", maxTicks = 80)
    public void failedDestructionPreservesSnapshotAndRetriesOnce(GameTestHelper test) {
        ServerLevel level = test.getLevel();
        test.setBlock(HOME, ModHollows.oakHollow());
        BlockPos pos = test.absolutePos(HOME);
        FairyHollowBlockEntity hollow = new FairyHollowBlockEntity(
            pos, level.getBlockState(pos), (world, fairy) -> false
        );
        level.setBlockEntity(hollow);
        DairatzEntity fairy = test.spawn(ModEntities.dairatzEntity().get(), HOME.north());
        fairy.setPos(hollow.entrance());
        fairy.setHealth(7);
        UUID id = fairy.getUUID();
        test.assertTrue(hollow.enter(fairy), "Fixture must store the resident");
        test.runAfterDelay(2, () -> {
            level.destroyBlock(pos, false);
            FairyRecoveryJournal journal = FairyRecoveryJournal.get(level.getServer());
            test.assertTrue(journal.hasRecovery(id), "Failed destruction must retain the full snapshot");
            var saved = FairyRecoveryJournal.CODEC.encodeStart(NbtOps.INSTANCE, journal).getOrThrow();
            FairyRecoveryJournal restored = FairyRecoveryJournal.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
            test.assertTrue(restored.hasRecovery(id), "Serialization must retain pending recovery");
            test.assertTrue(restored.recoverNow(level.getServer(), id, (world, entity) -> false).status()
                    == FairyReleaseStatus.FAILED, "A second spawn refusal must preserve recovery");
            test.assertTrue(restored.hasRecovery(id), "Repeated refusal must not erase the snapshot");
            FairyReleaseResult recovered = restored.recoverNow(level.getServer(), id, ServerLevel::addFreshEntity);
            test.assertTrue(recovered.succeeded() && recovered.fairy().getHealth() == 7
                    && recovered.fairy().getUUID().equals(id) && !recovered.fairy().hasHome(),
                    "A later successful recovery must preserve identity and health");
            test.assertTrue(!restored.hasRecovery(id), "Successful recovery must acknowledge delivery");
            journal.recoverNow(level.getServer(), id, ServerLevel::addFreshEntity);
            test.assertTrue(!journal.hasRecovery(id), "An already present UUID must complete without duplication");
            test.assertTrue(level.getEntitiesOfClass(DairatzEntity.class, test.getBounds(), entity -> entity.getUUID()
                    .equals(id)).size() == 1, "Recovery must not create duplicate UUIDs");
            test.succeed();
        });
    }

    @GameTest(environment = "dairatz-test:night")
    public void unloadedHomeIsReleasedWhenItsChunkBecomesAvailable(GameTestHelper test) {
        ServerLevel level = test.getLevel();
        BlockPos far = test.absolutePos(HOME).offset(1024, 0, 0);
        UUID id = UUID.randomUUID();
        GlobalPos home = GlobalPos.of(level.dimension(), far);
        test.assertTrue(!level.hasChunkAt(far), "Fixture home must initially be unloaded");
        FairyRecoveryJournal journal = new FairyRecoveryJournal();
        journal.releaseHome(level.getServer(), home, id);
        test.assertTrue(!level.hasChunkAt(far) && !journal.homeReleases().isEmpty(),
                "Deferred release must not force-load the home");
        var saved = FairyRecoveryJournal.CODEC.encodeStart(NbtOps.INSTANCE, journal).getOrThrow();
        FairyRecoveryJournal restored = FairyRecoveryJournal.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
        level.getChunk(far.getX() >> 4, far.getZ() >> 4);
        level.setBlock(far, ModHollows.oakHollow().defaultBlockState(), 3);
        FairyHollowBlockEntity hollow = (FairyHollowBlockEntity) level.getBlockEntity(far);
        TagValueOutput state = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        state.store("Resident", UUIDUtil.CODEC, id);
        hollow.loadCustomOnly(TagValueInput.create(
            ProblemReporter.DISCARDING, level.registryAccess(), state.buildResult()
        ));
        restored.applyHomeReleases(home, hollow);
        test.assertTrue(!hollow.hasResident() && restored.homeReleases().isEmpty(),
                "Loading the home must clear the matching old reservation");
        level.removeBlock(far, false);
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:night")
    public void recoveryWaitsForChunksAndHonorsPerTickBudget(GameTestHelper test) {
        ServerLevel level = test.getLevel();
        FairyRecoveryJournal journal = new FairyRecoveryJournal();
        int[] calls = {0};
        for (int i = 0; i < 12; i++) {
            DairatzEntity fairy = ModEntities.dairatzEntity().get()
                    .create(level, EntitySpawnReason.COMMAND);
            fairy.setPos(test.absoluteVec(new Vec3(3.5, 3, 3.5)));
            TagValueOutput output = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING, level.registryAccess()
            );
            fairy.save(output);
            journal.enqueue(
                fairy.getUUID(), GlobalPos.of(level.dimension(), test.absolutePos(HOME)), output.buildResult()
            );
        }
        journal.retry(level.getServer(), (world, entity) -> {
            calls[0]++;
            return false;
        });
        test.assertTrue(calls[0] == 4 && journal.recoverySnapshots().size() == 12,
                "Only four spawn attempts are allowed per tick and all failures must remain pending");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:night")
    public void pendingReleaseDoesNotEvictAnotherResident(GameTestHelper test) {
        ServerLevel level = test.getLevel();
        test.setBlock(HOME, ModHollows.oakHollow());
        FairyHollowBlockEntity hollow = test.getBlockEntity(HOME, FairyHollowBlockEntity.class);
        UUID current = UUID.randomUUID();
        TagValueOutput state = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        state.store("Resident", UUIDUtil.CODEC, current);
        hollow.loadCustomOnly(TagValueInput.create(
            ProblemReporter.DISCARDING, level.registryAccess(), state.buildResult()
        ));
        GlobalPos home = GlobalPos.of(level.dimension(), hollow.getBlockPos());
        PendingHomeRelease stale = new PendingHomeRelease(home, UUID.randomUUID());
        CompoundTag data = new CompoundTag();
        data.store("HomeReleases", PendingHomeRelease.CODEC.listOf(), List.of(stale));
        FairyRecoveryJournal journal = FairyRecoveryJournal.CODEC.parse(NbtOps.INSTANCE, data).getOrThrow();
        journal.applyHomeReleases(home, hollow);
        test.assertTrue(hollow.isResident(current) && journal.homeReleases().isEmpty(),
                "Deferred cleanup must not evict a different resident");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:night")
    public void unloadedRecoveryDoesNotAttemptSpawn(GameTestHelper test) {
        ServerLevel level = test.getLevel();
        DairatzEntity fairy = ModEntities.dairatzEntity().get().create(level, EntitySpawnReason.COMMAND);
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        fairy.save(output);
        BlockPos far = test.absolutePos(HOME).offset(4096, 0, 0);
        FairyRecoveryJournal journal = new FairyRecoveryJournal();
        journal.enqueue(fairy.getUUID(), GlobalPos.of(level.dimension(), far), output.buildResult());
        int[] attempts = {0};
        journal.retry(level.getServer(), (world, entity) -> {
            attempts[0]++;
            return false;
        });
        test.assertTrue(attempts[0] == 0 && !level.hasChunkAt(far) && journal.hasRecovery(fairy.getUUID()),
                "Recovery must wait without spawning, loading chunks, or deleting data");
        test.succeed();
    }

}
