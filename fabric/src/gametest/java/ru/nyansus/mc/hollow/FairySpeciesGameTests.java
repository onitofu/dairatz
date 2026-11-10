package ru.nyansus.mc.hollow;

import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueOutput;
import ru.nyansus.mc.entity.FairyKind;
import ru.nyansus.mc.entity.FairyAbilities;
import ru.nyansus.mc.entity.HousedFairyEntity;
import ru.nyansus.mc.entity.NewFairyEntity;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.ModHollows;

public final class FairySpeciesGameTests {
    private static final BlockPos HOME = new BlockPos(10, 3, 10);

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination")
    public void everyHomePreservesSpeciesAndAppearance(GameTestHelper test) {
        ServerLevel level = test.getLevel();
        for (FairyKind kind : FairyKind.values()) {
            if (kind == FairyKind.ENDER) {
                continue;
            }
            test.setBlock(HOME, ModHollows.hollow(kind));
            FairyHollowBlockEntity hollow = test.getBlockEntity(HOME, FairyHollowBlockEntity.class);
            HousedFairyEntity fairy = switch (kind) {
                case STANDARD -> ModEntities.dairatzEntity().get().create(level, EntitySpawnReason.COMMAND);
                case WINTER -> ModEntities.winterFairy().get().create(level, EntitySpawnReason.COMMAND);
                default -> ModEntities.fairy(kind).create(level, EntitySpawnReason.COMMAND);
            };
            fairy.setAppearance(1);
            fairy.setNoAi(true);
            fairy.setPos(hollow.entrance());
            level.addFreshEntity(fairy);
            UUID id = fairy.getUUID();
            test.assertTrue(hollow.enter(fairy), "Matching species must enter " + kind);
            HousedFairyEntity returned = hollow.letResidentOut().fairy();
            test.assertTrue(returned != null && returned.kind() == kind && returned.getUUID().equals(id)
                    && returned.appearance() == (kind.hasVariants() ? 1 : 0),
                    "Home must preserve species, UUID and appearance for " + kind);
            returned.releaseHome();
            returned.discard();
        }
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination")
    public void wrongSpeciesCannotEnterHome(GameTestHelper test) {
        test.setBlock(HOME, ModHollows.hollow(FairyKind.CHERRY));
        FairyHollowBlockEntity hollow = test.getBlockEntity(HOME, FairyHollowBlockEntity.class);
        for (FairyKind kind : ModEntities.newKinds()) {
            if (kind == FairyKind.CHERRY) {
                continue;
            }
            NewFairyEntity fairy = test.spawn(ModEntities.fairy(kind), HOME.north());
            fairy.setNoAi(true);
            fairy.setPos(hollow.entrance());
            test.assertTrue(!hollow.enter(fairy) && fairy.isAlive() && !fairy.hasHome(),
                    "A different species must not occupy a cherry home");
        }
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination")
    public void variantsAndCooldownsSurviveEntityReload(GameTestHelper test) {
        NewFairyEntity original = test.spawn(ModEntities.fairy(FairyKind.PALE), HOME);
        original.setAppearance(1);
        original.setNoAi(true);
        FairyAbilities.state(original).nextWeakness = 12345;
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,
                test.getLevel().registryAccess());
        original.save(output);
        NewFairyEntity restored = (NewFairyEntity) EntityType.loadEntityRecursive(output.buildResult(),
                test.getLevel(), EntitySpawnReason.LOAD, value -> value);
        test.assertTrue(restored != null && restored.appearance() == 1,
                "Variant must survive serialization");
        test.assertTrue(FairyAbilities.state(restored).nextWeakness == 12345,
                "Ability cooldown must survive serialization");
        original.setAppearance(99);
        test.assertTrue(original.appearance() == 0, "Unknown appearances must resolve to the primary skin");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination")
    public void failedReleaseRecoversEveryNewResident(GameTestHelper test) {
        ServerLevel level = test.getLevel();
        FairyRecoveryJournal journal = FairyRecoveryJournal.get(level.getServer());
        for (FairyKind kind : ModEntities.newKinds()) {
            if (kind == FairyKind.ENDER) {
                continue;
            }
            test.setBlock(HOME, ModHollows.hollow(kind));
            FairyHollowBlockEntity original = test.getBlockEntity(HOME, FairyHollowBlockEntity.class);
            FairyHollowBlockEntity hollow = new FairyHollowBlockEntity(original.getBlockPos(),
                    original.getBlockState(), (world, entity) -> false);
            level.setBlockEntity(hollow);
            NewFairyEntity fairy = test.spawn(ModEntities.fairy(kind), HOME.north());
            fairy.setNoAi(true);
            fairy.setAppearance(1);
            fairy.setPos(hollow.entrance());
            UUID id = fairy.getUUID();
            test.assertTrue(hollow.enter(fairy), "Fixture must settle resident " + kind);
            hollow.releaseOnDestruction();
            test.assertTrue(journal.hasRecovery(id), "Refused spawn must preserve the resident snapshot");
            test.setBlock(HOME, Blocks.AIR);
            HousedFairyEntity returned = journal.recoverNow(level.getServer(), id, ServerLevel::addFreshEntity).fairy();
            test.assertTrue(returned != null && returned.getUUID().equals(id) && returned.kind() == kind
                    && !returned.hasHome() && returned.appearance() == (kind.hasVariants() ? 1 : 0),
                    "Recovery must preserve species and variant without stale home");
            test.assertTrue(!journal.hasRecovery(id), "Successful recovery must clear its journal entry");
            returned.discard();
        }
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:night", structure = "dairatz-test:pollination", maxTicks = 80)
    public void paleHomeReleasesResidentAtNight(GameTestHelper test) {
        test.setBlock(HOME, ModHollows.hollow(FairyKind.PALE));
        FairyHollowBlockEntity hollow = test.getBlockEntity(HOME, FairyHollowBlockEntity.class);
        hollow.prepareNaturalResident();
        long previousTime = test.getLevel().getDayTime();
        float previousRain = test.getLevel().getRainLevel(1);
        try {
            test.getLevel().setDayTime(13000);
            test.getLevel().setRainLevel(0);
            hollow.tickResident(test.getLevel());
            test.assertTrue(hollow.residentId() != null && !hollow.isInside(),
                    "Pale resident must leave home on a clear night");
            test.succeed();
        } finally {
            test.getLevel().setDayTime(previousTime);
            test.getLevel().setRainLevel(previousRain);
        }
    }
}
