package ru.nyansus.mc.hollow;

import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import ru.nyansus.mc.entity.DairatzEntity;
import ru.nyansus.mc.entity.WinterFairyEntity;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.ModHollows;

public final class FairyHollowGameTests {
    private static final BlockPos HOME = new BlockPos(3, 3, 3);

    @GameTest(environment = "dairatz-test:night", maxTicks = 60)
    public void settleAndRecallPreservesFairy(GameTestHelper test) {
        FairyHollowBlockEntity hollow = hollow(test);
        ServerPlayer owner = owner(test);
        DairatzEntity fairy = fairy(test, hollow);
        fairy.tame(owner);
        fairy.setCustomName(Component.literal("Garden resident"));
        fairy.setHealth(7);
        fairy.setOnHead(true);
        fairy.setPos(owner.getX(), owner.getEyeY(), owner.getZ());
        UUID id = fairy.getUUID();
        hollow.interact(owner);
        test.assertTrue(hollow.isInside() && hollow.isResident(id), "The owner's head fairy must settle");
        FairyHollowBlockEntity restored = reload(test, hollow);
        test.runAfterDelay(2, () -> {
            owner.setShiftKeyDown(true);
            restored.interact(owner);
            test.assertTrue(test.getLevel().getEntity(id) instanceof DairatzEntity,
                    "Recall must restore the same UUID");
            DairatzEntity returned = (DairatzEntity) test.getLevel().getEntity(id);
            test.assertTrue(returned.isOwnedBy(owner) && returned.isOnHead() && !returned.hasHome(),
                    "Recall must preserve ownership and resume companion mode");
            test.assertTrue(returned.getHealth() == 7 && returned.getName().getString().equals("Garden resident"),
                    "Storage must preserve health and name");
            test.assertTrue(!restored.hasResident(), "Recall must release the home reservation");
            test.succeed();
        });
    }

    @GameTest(environment = "dairatz-test:night", maxTicks = 60)
    public void occupiedHomeRejectsSecondFairy(GameTestHelper test) {
        FairyHollowBlockEntity hollow = hollow(test);
        DairatzEntity first = fairy(test, hollow);
        test.assertTrue(hollow.enter(first), "First resident must enter");
        DairatzEntity second = fairy(test, hollow);
        test.assertTrue(!hollow.enter(second) && second.isAlive() && !second.hasHome(),
                "A second fairy must not replace the first");
        UUID id = first.getUUID();
        test.runAfterDelay(2, () -> {
            DairatzEntity outside = (DairatzEntity) hollow.letResidentOut().fairy();
            test.assertTrue(outside != null && hollow.isResident(id) && !hollow.isInside(),
                    "Leaving the home must preserve the reservation");
            test.assertTrue(!hollow.enter(second), "An outside resident still owns the home");
            FairyHollowBlockEntity restored = reload(test, hollow);
            test.assertTrue(restored.isResident(id), "An outside reservation must survive save and load");
            test.succeed();
        });
    }

    @GameTest(environment = "dairatz-test:night", maxTicks = 60)
    public void strangerCannotRecallResident(GameTestHelper test) {
        FairyHollowBlockEntity hollow = hollow(test);
        ServerPlayer owner = owner(test);
        DairatzEntity fairy = fairy(test, hollow);
        fairy.tame(owner);
        test.assertTrue(hollow.enter(fairy), "Resident must enter");
        ServerPlayer stranger = owner(test);
        stranger.setShiftKeyDown(true);
        test.runAfterDelay(2, () -> {
            hollow.interact(stranger);
            test.assertTrue(hollow.isInside() && hollow.isResident(fairy.getUUID()),
                    "A stranger must not recall another player's resident");
            test.succeed();
        });
    }

    @GameTest(environment = "dairatz-test:garden")
    public void winterFairyCannotSettle(GameTestHelper test) {
        FairyHollowBlockEntity hollow = hollow(test);
        ServerPlayer owner = owner(test);
        WinterFairyEntity fairy = test.spawn(ModEntities.winterFairy().get(), HOME.above());
        fairy.tame(owner);
        fairy.setOnHead(true);
        fairy.setPos(owner.getX(), owner.getEyeY(), owner.getZ());
        hollow.interact(owner);
        test.assertTrue(!hollow.hasResident() && fairy.isOnHead(), "Oak hollows must reject winter fairies");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:night", maxTicks = 60)
    public void blockedEntranceRetainsStoredFairy(GameTestHelper test) {
        FairyHollowBlockEntity hollow = hollow(test);
        DairatzEntity fairy = fairy(test, hollow);
        test.assertTrue(hollow.enter(fairy), "Resident must enter");
        test.setBlock(HOME.north(), Blocks.STONE);
        test.runAfterDelay(2, () -> {
            test.assertTrue(hollow.letResidentOut().fairy() == null && hollow.isInside(),
                    "A blocked entrance must retain the stored fairy");
            test.setBlock(HOME.north(), Blocks.AIR);
            test.assertTrue(hollow.letResidentOut().fairy() != null, "Clearing the entrance must allow release");
            test.succeed();
        });
    }

    @GameTest(environment = "dairatz-test:night", maxTicks = 60)
    public void breakingHomeReleasesResident(GameTestHelper test) {
        FairyHollowBlockEntity hollow = hollow(test);
        DairatzEntity fairy = fairy(test, hollow);
        UUID id = fairy.getUUID();
        test.assertTrue(hollow.enter(fairy), "Resident must enter");
        test.runAfterDelay(2, () -> {
            test.getLevel().destroyBlock(hollow.getBlockPos(), false);
            test.assertTrue(test.getLevel().getEntity(id) instanceof DairatzEntity, "Breaking must release the fairy");
            DairatzEntity released = (DairatzEntity) test.getLevel().getEntity(id);
            test.assertTrue(!released.hasHome() && released.isAlive(), "Released fairy must be alive and homeless");
            test.succeed();
        });
    }

    @GameTest(environment = "dairatz-test:garden")
    public void silkTouchControlsBlockDrop(GameTestHelper test) {
        FairyHollowBlockEntity hollow = hollow(test);
        ItemStack pick = new ItemStack(Items.DIAMOND_AXE);
        BlockState state = hollow.getBlockState();
        test.assertTrue(Block.getDrops(state, test.getLevel(), hollow.getBlockPos(), hollow, null, pick)
                .stream().anyMatch(stack -> stack.is(Items.OAK_LOG)), "An ordinary tool must drop an oak log");
        pick.enchant(test.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.SILK_TOUCH), 1);
        var drops = Block.getDrops(state, test.getLevel(), hollow.getBlockPos(), hollow, null, pick);
        test.assertTrue(drops.size() == 1 && drops.getFirst().is(ModHollows.oakHollowItem()),
                "Silk Touch must drop a hollow");
        test.assertTrue(!drops.getFirst().has(DataComponents.BLOCK_ENTITY_DATA),
                "The dropped hollow must not contain a fairy");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:night", maxTicks = 100)
    public void residentDoesNotTeleportToOwner(GameTestHelper test) {
        FairyHollowBlockEntity hollow = hollow(test);
        ServerPlayer owner = owner(test);
        DairatzEntity fairy = fairy(test, hollow);
        fairy.tame(owner);
        fairy.assignHome(hollow.getBlockPos());
        test.assertTrue(hollow.enter(fairy), "Resident must enter");
        test.runAfterDelay(2, () -> {
            DairatzEntity resident = (DairatzEntity) hollow.letResidentOut().fairy();
            test.assertTrue(resident != null, "Resident must leave during daytime");
            owner.setPos(owner.getX() + 80, owner.getY(), owner.getZ());
            test.runAfterDelay(60, () -> {
                test.assertTrue(resident.hasHome()
                        && resident.position().distanceToSqr(Vec3.atCenterOf(hollow.getBlockPos())) < 225,
                        "A resident must remain by the garden when its owner leaves");
                test.succeed();
            });
        });
    }

    @GameTest(environment = "dairatz-test:night", maxTicks = 100)
    public void deathFreesHome(GameTestHelper test) {
        FairyHollowBlockEntity hollow = hollow(test);
        DairatzEntity fairy = fairy(test, hollow);
        test.assertTrue(hollow.enter(fairy), "Resident must enter");
        test.runAfterDelay(2, () -> {
            DairatzEntity resident = (DairatzEntity) hollow.letResidentOut().fairy();
            test.assertTrue(resident != null, "Resident must leave");
            resident.kill(test.getLevel());
            test.assertTrue(!hollow.hasResident(), "Death must free the home");
            test.succeed();
        });
    }

    @GameTest(environment = "dairatz-test:night", maxTicks = 120)
    public void nightReturnsFairyAndKnockAllowsOutsideTime(GameTestHelper test) {
        FairyHollowBlockEntity hollow = hollow(test);
        DairatzEntity fairy = fairy(test, hollow);
        test.assertTrue(hollow.enter(fairy), "Resident must enter");
        test.runAfterDelay(2, () -> {
            DairatzEntity resident = (DairatzEntity) hollow.letResidentOut().fairy();
            test.assertTrue(resident != null && !resident.wantsShelter(), "Knock must grant ten seconds outside");
            resident.stayOutside();
            test.succeed();
        });
    }

    @GameTest(environment = "dairatz-test:night", maxTicks = 260)
    public void residentReturnsAfterKnockGrace(GameTestHelper test) {
        FairyHollowBlockEntity hollow = hollow(test);
        DairatzEntity fairy = fairy(test, hollow);
        test.assertTrue(hollow.enter(fairy), "Resident must enter");
        test.runAfterDelay(2, () -> {
            DairatzEntity resident = (DairatzEntity) hollow.letResidentOut().fairy();
            test.assertTrue(resident != null, "Resident must leave on a knock");
            resident.setNoAi(true);
            test.runAfterDelay(205, () -> {
                resident.setNoAi(false);
                resident.setPos(hollow.entrance());
                test.assertTrue(resident.wantsShelter(), "A night resident must seek shelter after ten seconds");
                test.succeedWhen(() -> test.assertTrue(hollow.isInside(), "The night resident must return inside"));
            });
        });
    }

    @GameTest(environment = "dairatz-test:rain", setupTicks = 30)
    public void rainMakesResidentSeekShelter(GameTestHelper test) {
        FairyHollowBlockEntity hollow = hollow(test);
        DairatzEntity fairy = fairy(test, hollow);
        test.assertTrue(hollow.enter(fairy), "Resident must enter");
        test.assertTrue(fairy.wantsShelter(), "Rain must make a resident seek shelter");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", maxTicks = 900, skyAccess = true)
    public void tamedResidentGrowsGarden(GameTestHelper test) {
        FairyHollowBlockEntity hollow = hollow(test);
        ServerPlayer owner = owner(test);
        DairatzEntity fairy = fairy(test, hollow);
        fairy.tame(owner);
        test.assertTrue(hollow.enter(fairy), "Resident must enter");
        BlockPos crop = prepareGarden(test);
        test.succeedWhen(() -> test.assertTrue(test.getBlockState(crop).getValue(CropBlock.AGE) > 0,
                "A tamed resident must grow crops without random ticks"));
    }

    @GameTest(environment = "dairatz-test:garden")
    public void blockedEntranceRejectsSettlement(GameTestHelper test) {
        FairyHollowBlockEntity hollow = hollow(test);
        ServerPlayer owner = owner(test);
        DairatzEntity fairy = fairy(test, hollow);
        fairy.tame(owner);
        fairy.setOnHead(true);
        fairy.setPos(owner.getX(), owner.getEyeY(), owner.getZ());
        test.setBlock(HOME.north(), Blocks.STONE);
        hollow.interact(owner);
        test.assertTrue(fairy.isOnHead() && !fairy.hasHome() && !hollow.hasResident(),
                "A blocked home must leave the fairy on its owner's head");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:night", maxTicks = 80)
    public void missingHomeDetachesReloadedOutsideFairy(GameTestHelper test) {
        FairyHollowBlockEntity hollow = hollow(test);
        DairatzEntity fairy = fairy(test, hollow);
        test.assertTrue(hollow.enter(fairy), "Resident must enter");
        test.runAfterDelay(2, () -> {
            DairatzEntity outside = (DairatzEntity) hollow.letResidentOut().fairy();
            test.assertTrue(outside != null, "Resident must leave");
            TagValueOutput output = TagValueOutput.createWithContext(
                    ProblemReporter.DISCARDING, test.getLevel().registryAccess());
            outside.save(output);
            outside.discard();
            test.getLevel().destroyBlock(hollow.getBlockPos(), false);
            test.runAfterDelay(2, () -> {
                DairatzEntity restored = ModEntities.dairatzEntity().get()
                        .create(test.getLevel(), EntitySpawnReason.LOAD);
                restored.load(TagValueInput.create(
                        ProblemReporter.DISCARDING, test.getLevel().registryAccess(), output.buildResult()));
                test.getLevel().addFreshEntity(restored);
                test.runAfterDelay(25, () -> {
                    test.assertTrue(!restored.hasHome(), "A reloaded fairy must detach from a destroyed home");
                    test.succeed();
                });
            });
        });
    }

    @GameTest(environment = "dairatz-test:garden", maxTicks = 80)
    public void naturalResidentIsCreatedOnlyOnce(GameTestHelper test) {
        FairyHollowBlockEntity hollow = hollow(test);
        hollow.prepareNaturalResident();
        ServerPlayer player = owner(test);
        hollow.interact(player);
        test.runAfterDelay(2, () -> {
            DairatzEntity resident = test.findOneEntity(ModEntities.dairatzEntity().get());
            test.assertTrue(hollow.isResident(resident.getUUID()), "Natural residents must own their hollow");
            resident.kill(test.getLevel());
            test.runAfterDelay(25, () -> {
                test.assertTrue(!hollow.hasResident(), "A natural resident must not regenerate after death");
                test.succeed();
            });
        });
    }

    @GameTest(environment = "dairatz-test:full_day", maxTicks = 24200, skyAccess = true)
    public void gardenSurvivesFullDayNightCycle(GameTestHelper test) {
        FairyHollowBlockEntity hollow = hollow(test);
        BlockPos crop = prepareGarden(test);
        ServerPlayer owner = owner(test);
        DairatzEntity fairy = fairy(test, hollow);
        fairy.tame(owner);
        fairy.setOnHead(true);
        fairy.setPos(owner.getX(), owner.getEyeY(), owner.getZ());
        UUID id = fairy.getUUID();
        hollow.interact(owner);
        test.assertTrue(hollow.isInside(), "Head interaction must settle the gardener");
        owner.setPos(owner.getX() + 80, owner.getY(), owner.getZ());
        test.runAtTickTime(6000, () -> {
            test.assertTrue(!hollow.isInside() && hollow.isResident(id), "Daytime resident must work outside");
            test.assertTrue(test.getBlockState(crop).getValue(CropBlock.AGE) > 0,
                    "The resident must tend the garden while its owner is away");
        });
        test.runAtTickTime(14000, () -> {
            test.assertTrue(hollow.isInside() && hollow.isResident(id),
                    "The same fairy must sleep inside at night; time=" + test.getLevel().getDayTime()
                            + "; resident=" + test.getLevel().getEntity(id));
            reload(test, hollow);
        });
        test.runAtTickTime(24060, () -> {
            FairyHollowBlockEntity restored = test.getBlockEntity(HOME, FairyHollowBlockEntity.class);
            test.assertTrue(!restored.isInside() && restored.isResident(id),
                    "The resident must leave the next morning");
            test.assertTrue(test.getLevel().getEntity(id) instanceof DairatzEntity,
                    "A full day and a stored reload must preserve the resident UUID");
            test.succeed();
        });
    }

    @GameTest(environment = "dairatz-test:garden", skyAccess = true)
    public void generationDecoratesOakOnlyOnce(GameTestHelper test) {
        test.setBlock(HOME.below(), Blocks.DIRT);
        for (int y = 0; y < 5; y++) {
            test.setBlock(HOME.above(y), Blocks.OAK_LOG);
        }
        test.setBlock(HOME.above(5), Blocks.OAK_LEAVES);
        ServerLevel level = test.getLevel();
        BlockPos base = test.absolutePos(HOME);
        FairyHollowFeature feature = new FairyHollowFeature();
        test.assertTrue(feature.place(NoneFeatureConfiguration.INSTANCE, level,
                level.getChunkSource().getGenerator(), RandomSource.create(0), base),
                "A fixed seed must decorate the eligible oak");
        int count = 0;
        for (int y = 1; y <= 3; y++) {
            if (test.getBlockState(HOME.above(y)).is(ModHollows.oakHollow())) {
                count++;
            }
        }
        test.assertTrue(count == 1, "Generation must replace exactly one trunk block");
        test.assertTrue(!feature.place(NoneFeatureConfiguration.INSTANCE, level,
                level.getChunkSource().getGenerator(), RandomSource.create(0), base),
                "An existing home must prevent another hollow in the same trunk");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", skyAccess = true)
    public void generationRejectsBirchAndBareLogs(GameTestHelper test) {
        test.setBlock(HOME.below(), Blocks.DIRT);
        for (int y = 0; y < 5; y++) {
            test.setBlock(HOME.above(y), Blocks.BIRCH_LOG);
        }
        test.setBlock(HOME.above(5), Blocks.BIRCH_LEAVES);
        ServerLevel level = test.getLevel();
        BlockPos base = test.absolutePos(HOME);
        FairyHollowFeature feature = new FairyHollowFeature();
        test.assertTrue(!feature.place(NoneFeatureConfiguration.INSTANCE, level,
                level.getChunkSource().getGenerator(), RandomSource.create(0), base), "Birch must remain unchanged");
        for (int y = 0; y < 5; y++) {
            test.setBlock(HOME.above(y), Blocks.OAK_LOG);
        }
        test.setBlock(HOME.above(5), Blocks.AIR);
        test.assertTrue(!feature.place(NoneFeatureConfiguration.INSTANCE, level,
                level.getChunkSource().getGenerator(), RandomSource.create(0), base),
                "An oak post without leaves must remain unchanged");
        test.succeed();
    }

    private static BlockPos prepareGarden(GameTestHelper test) {
        for (int x = 1; x <= 6; x++) {
            for (int z = 1; z <= 6; z++) {
                test.setBlock(new BlockPos(x, 1, z), Blocks.DIRT);
                test.setBlock(new BlockPos(x, 2, z), Blocks.AIR);
            }
        }
        test.setBlock(new BlockPos(6, 2, 6), Blocks.POPPY);
        BlockPos crop = new BlockPos(2, 2, 2);
        test.setBlock(crop.below(), Blocks.FARMLAND);
        test.setBlock(crop, Blocks.WHEAT.defaultBlockState());
        test.setBlock(new BlockPos(3, 6, 3), Blocks.GLOWSTONE);
        return crop;
    }

    private static FairyHollowBlockEntity hollow(GameTestHelper test) {
        test.setBlock(HOME, ModHollows.oakHollow().defaultBlockState()
                .setValue(FairyHollowBlock.FACING, Direction.NORTH));
        return test.getBlockEntity(HOME, FairyHollowBlockEntity.class);
    }

    private static ServerPlayer owner(GameTestHelper test) {
        ServerPlayer player = test.makeMockServerPlayerInLevel();
        Vec3 pos = test.absoluteVec(new Vec3(3.5, 3, 4.5));
        player.setPos(pos);
        return player;
    }

    private static DairatzEntity fairy(GameTestHelper test, FairyHollowBlockEntity hollow) {
        DairatzEntity fairy = test.spawn(ModEntities.dairatzEntity().get(), HOME.north());
        fairy.setPos(hollow.entrance());
        return fairy;
    }

    private static FairyHollowBlockEntity reload(GameTestHelper test, FairyHollowBlockEntity original) {
        ServerLevel level = test.getLevel();
        CompoundTag saved = original.saveWithoutMetadata(level.registryAccess());
        FairyHollowBlockEntity restored = new FairyHollowBlockEntity(original.getBlockPos(), original.getBlockState());
        restored.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
        level.setBlockEntity(restored);
        return restored;
    }
}
