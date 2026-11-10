package ru.nyansus.mc.entity.ai;

import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import ru.nyansus.mc.entity.AbstractFairyEntity;
import ru.nyansus.mc.entity.FairyAbilities;
import ru.nyansus.mc.entity.FairyKind;
import ru.nyansus.mc.entity.FairyPlacement;
import ru.nyansus.mc.entity.HousedFairyEntity;
import ru.nyansus.mc.loot.FairyLootAccess;
import ru.nyansus.mc.hollow.FairyHollowBlockEntity;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.ModHollows;

public final class FairyHabitsGameTests {
    private static final BlockPos CENTER = new BlockPos(10, 3, 10);

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination",
            skyAccess = true, maxTicks = 250)
    public void winterActuallyRestsOnIce(GameTestHelper test) {
        floor(test);
        test.setBlock(CENTER.south(2).below(), Blocks.ICE);
        AbstractFairyEntity fairy = fairy(test, FairyKind.WINTER);
        fairy.setNoAi(false);
        test.succeedWhen(() -> test.assertTrue(fairy.isResting()
                        && test.getLevel().getBlockState(fairy.blockPosition().below()).is(Blocks.ICE),
                "Winter fairy must approach ice and rest; position=" + fairy.position()
                        + "; navigation=" + fairy.getNavigation().getTargetPos()));
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void winterMovesAwayFromHeat(GameTestHelper test) {
        floor(test);
        BlockPos heat = CENTER.east().below();
        test.setBlock(heat, Blocks.CAMPFIRE);
        AbstractFairyEntity fairy = fairy(test, FairyKind.WINTER);
        FairyHabitsGoal goal = new FairyHabitsGoal(fairy);
        test.assertTrue(goal.canUse(), "Nearby lit campfire must trigger a cautious reaction");
        goal.start();
        goal.tick();
        BlockPos destination = fairy.getNavigation().getTargetPos();
        test.assertTrue(destination != null && Vec3.atCenterOf(destination).distanceToSqr(test.absoluteVec(
                Vec3.atCenterOf(heat))) > fairy.position().distanceToSqr(test.absoluteVec(Vec3.atCenterOf(heat))),
                "Winter fairy must choose a point farther from the heat source");
        goal.stop();
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination",
            skyAccess = true, maxTicks = 250)
    public void jungleInspectsMaturePlantsWithoutChangingThem(GameTestHelper test) {
        floor(test);
        BlockPos cocoa = CENTER.east(3);
        test.setBlock(cocoa.north(), Blocks.JUNGLE_LOG);
        test.setBlock(cocoa, Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.AGE, 2));
        AbstractFairyEntity fairy = fairy(test, FairyKind.JUNGLE);
        fairy.setNoAi(false);
        test.succeedWhen(() -> {
            test.assertTrue(fairy.distanceToSqr(test.absoluteVec(Vec3.atCenterOf(cocoa))) < 4,
                    "Jungle fairy must approach the mature cocoa to inspect it");
            test.assertTrue(test.getBlockState(cocoa).is(Blocks.COCOA)
                    && test.getBlockState(cocoa).getValue(CocoaBlock.AGE) == 2,
                    "Inspection must not harvest or change the plant");
        });
    }

    @GameTest(environment = "dairatz-test:full_day", structure = "dairatz-test:pollination",
            skyAccess = true, maxTicks = 350)
    public void cherryApproachesPatientBeforeHealing(GameTestHelper test) {
        floor(test);
        BlockPos home = CENTER.below();
        test.setBlock(home, ModHollows.hollow(FairyKind.CHERRY));
        ServerPlayer owner = test.makeMockServerPlayerInLevel();
        owner.setPos(test.absoluteVec(new Vec3(10.5, 3, 11.5)));
        HousedFairyEntity incoming = (HousedFairyEntity) fairy(test, FairyKind.CHERRY);
        incoming.tame(owner);
        FairyHollowBlockEntity hollow = test.getBlockEntity(home, FairyHollowBlockEntity.class);
        incoming.setPos(hollow.entrance());
        test.assertTrue(hollow.enter(incoming), "Fixture must settle the Cherry fairy");
        HousedFairyEntity fairy = hollow.letResidentOut().fairy();
        test.assertTrue(fairy != null, "Fixture must release the registered resident");
        var pet = test.spawn(EntityType.WOLF, CENTER.east(5).below());
        pet.setNoAi(true);
        pet.tame(owner);
        pet.setHealth(4);
        fairy.tickCount = 20;
        FairyAbilities.tick(fairy);
        test.assertTrue(pet.getHealth() == 4, "A distant patient must not be healed before the visit");
        fairy.setNoAi(false);
        test.succeedWhen(() -> {
            test.assertTrue(pet.getHealth() > 4, "Cherry fairy must visit its patient; position=" + fairy.position()
                            + "; home=" + fairy.hasHome() + "; time=" + test.getLevel().getDayTime()
                            + "; rain=" + test.getLevel().isRaining());
            test.assertTrue(fairy.distanceToSqr(pet) <= 6.25,
                    "Healing must happen while the fairy is near its patient");
        });
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void paleFindsShadeAndClearsRestingWhenInterrupted(GameTestHelper test) {
        floor(test);
        test.setBlock(CENTER.above(2), Blocks.PALE_OAK_LEAVES);
        AbstractFairyEntity fairy = fairy(test, FairyKind.PALE);
        long time = test.getLevel().getDayTime();
        try {
            test.getLevel().setDayTime(1000);
            FairyHabitsGoal goal = new FairyHabitsGoal(fairy);
            test.assertTrue(goal.canUse(), "Wild pale fairy must find daytime shelter under leaves");
            goal.start();
            goal.tick();
            BlockPos destination = fairy.getNavigation().getTargetPos();
            test.assertTrue(destination != null && destination.getY() < test.absolutePos(CENTER.above(2)).getY(),
                    "Shelter destination must be below the canopy");
            fairy.setPos(fairy.getMoveControl().getWantedX(),
                    fairy.getMoveControl().getWantedY(), fairy.getMoveControl().getWantedZ());
            goal.tick();
            test.assertTrue(fairy.isResting(), "Arriving under the leaves must enter the resting pose");
            fairy.setOnHead(true);
            test.assertTrue(!goal.canContinueToUse(), "Mounting must interrupt the habit");
            goal.stop();
            test.assertTrue(!fairy.isResting() && fairy.getNavigation().isDone(),
                    "Stopping the habit must clear its pose and navigation");
            test.succeed();
        } finally {
            test.getLevel().setDayTime(time);
        }
    }

    @GameTest(environment = "dairatz-test:night", structure = "dairatz-test:pollination", skyAccess = true)
    public void palePatrolHasThreeStableHomePoints(GameTestHelper test) {
        floor(test);
        test.setBlock(CENTER.below(), ModHollows.hollow(FairyKind.PALE));
        HousedFairyEntity fairy = (HousedFairyEntity) fairy(test, FairyKind.PALE);
        fairy.assignHome(test.absolutePos(CENTER.below()));
        long time = test.getLevel().getDayTime();
        float rain = test.getLevel().getRainLevel(1);
        try {
            test.getLevel().setDayTime(13000);
            test.getLevel().setRainLevel(0);
            Set<Vec3> points = new HashSet<>();
            for (int i = 0; i < 3; i++) {
                FairyInterest interest = FairyInterests.choose(fairy, i);
                test.assertTrue(interest != null && FairyInterests.safe(fairy, interest.focus()),
                        "Each patrol point must be safe and inside the home area");
                points.add(interest.focus());
                test.assertTrue(interest.focus().equals(FairyInterests.choose(fairy, i).focus()),
                        "The same fairy must reuse the same home observation points");
            }
            test.assertTrue(points.size() == 3, "Night patrol must visit distinct observation points");
            test.succeed();
        } finally {
            test.getLevel().setDayTime(time);
            test.getLevel().setRainLevel(rain);
        }
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void enderInspectsAndBlinksWithoutTakingItems(GameTestHelper test) {
        floor(test);
        AbstractFairyEntity fairy = fairy(test, FairyKind.ENDER);
        Vec3 point = test.absoluteVec(new Vec3(12.5, 3, 10.5));
        ItemEntity item = new ItemEntity(test.getLevel(), point.x, point.y, point.z, new ItemStack(Items.DIAMOND, 3));
        item.setNoGravity(true);
        item.setDeltaMovement(Vec3.ZERO);
        test.getLevel().addFreshEntity(item);
        FairyHabitsGoal goal = new FairyHabitsGoal(fairy);
        test.assertTrue(goal.canUse(), "An unowned dropped item must interest an Ender fairy");
        goal.start();
        goal.tick();
        BlockPos destination = fairy.getNavigation().getTargetPos();
        test.assertTrue(destination != null, "Inspection must navigate toward the item");
        fairy.setPos(Vec3.atCenterOf(destination));
        Vec3 arrived = fairy.position();
        for (int i = 0; i < 100 && goal.canContinueToUse() && fairy.position().equals(arrived); i++) {
            goal.tick();
        }
        test.assertTrue(!fairy.position().equals(arrived) && fairy.position().distanceToSqr(arrived) <= 9,
                "After observing the item, Ender fairy must make a short blink");
        test.assertTrue(FairyPlacement.isSafe(test.getLevel(), fairy, fairy.position()), "Blink must land safely");
        test.assertTrue(item.isAlive() && item.getItem().getCount() == 3
                && ((FairyLootAccess) item).fairyLootOwner() == null,
                "Curiosity must not collect or claim unowned items");
        goal.stop();
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void combatAndDamageInterruptHabit(GameTestHelper test) {
        floor(test);
        test.setBlock(CENTER.south(2).below(), Blocks.ICE);
        AbstractFairyEntity fairy = fairy(test, FairyKind.WINTER);
        FairyHabitsGoal goal = new FairyHabitsGoal(fairy);
        test.assertTrue(goal.canUse(), "Fixture must provide an ice resting spot");
        goal.start();
        fairy.setTarget(test.spawn(EntityType.ZOMBIE, CENTER.east(3)));
        test.assertTrue(!goal.canContinueToUse(), "Combat must preempt the habit");
        fairy.setTarget(null);
        fairy.hurtServer(test.getLevel(), test.getLevel().damageSources().magic(), 1);
        test.assertTrue(!goal.canContinueToUse(), "Received damage must also interrupt the habit");
        goal.stop();
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void interestSearchDoesNotLoadNeighborChunks(GameTestHelper test) {
        ServerLevel level = test.getLevel();
        int chunkX = (test.absolutePos(CENTER).getX() >> 4) + 64;
        int chunkZ = test.absolutePos(CENTER).getZ() >> 4;
        level.getChunk(chunkX, chunkZ);
        BlockPos neighbor = new BlockPos((chunkX + 1) * 16, 130, chunkZ * 16 + 8);
        test.assertTrue(!level.hasChunkAt(neighbor), "Neighbor must start unloaded");
        AbstractFairyEntity fairy = ModEntities.winterFairy().get().create(level, EntitySpawnReason.COMMAND);
        fairy.setPos(chunkX * 16 + 15.5, 130, chunkZ * 16 + 8.5);
        new FairyHabitsGoal(fairy).canUse();
        test.assertTrue(!level.hasChunkAt(neighbor), "Looking for interests must not load neighboring chunks");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void restingPoseIsDisposableOnReload(GameTestHelper test) {
        AbstractFairyEntity fairy = fairy(test, FairyKind.WINTER);
        fairy.setResting(true);
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,
                test.getLevel().registryAccess());
        fairy.save(output);
        AbstractFairyEntity restored = (AbstractFairyEntity) EntityType.loadEntityRecursive(output.buildResult(),
                test.getLevel(), EntitySpawnReason.LOAD, entity -> entity);
        test.assertTrue(restored != null && !restored.isResting() && restored.getUUID().equals(fairy.getUUID()),
                "Reloading must keep identity without a stale resting pose or idle episode");
        test.succeed();
    }

    private static void floor(GameTestHelper test) {
        for (int x = 3; x <= 18; x++) {
            for (int z = 3; z <= 18; z++) {
                test.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
    }

    private static AbstractFairyEntity fairy(GameTestHelper test, FairyKind kind) {
        AbstractFairyEntity fairy = switch (kind) {
            case WINTER -> test.spawn(ModEntities.winterFairy().get(), CENTER);
            default -> test.spawn(ModEntities.fairy(kind), CENTER);
        };
        fairy.setNoAi(true);
        fairy.setNoGravity(true);
        return fairy;
    }
}
