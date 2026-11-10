package ru.nyansus.mc.entity;

import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import ru.nyansus.mc.loot.FairyLootAccess;
import ru.nyansus.mc.registry.ModEntities;
import ru.nyansus.mc.registry.ModHollows;

public final class FairyAbilityGameTests {
    private static final BlockPos CENTER = new BlockPos(10, 3, 10);

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void jungleHeadShortensNewPoison(GameTestHelper test) {
        ServerPlayer owner = owner(test);
        head(test, FairyKind.JUNGLE, owner);
        owner.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
        test.assertTrue(owner.getEffect(MobEffects.POISON).getDuration() == 100,
                "A mounted jungle fairy must halve newly applied poison duration");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void cherryHealingDoesNotStack(GameTestHelper test) {
        ServerPlayer owner = owner(test);
        NewFairyEntity first = head(test, FairyKind.CHERRY, owner);
        NewFairyEntity second = head(test, FairyKind.CHERRY, owner);
        owner.setHealth(10);
        first.tickCount = 20;
        second.tickCount = 20;
        FairyAbilities.tick(first);
        FairyAbilities.tick(second);
        test.assertTrue(owner.getHealth() == 11, "Two healers must share the target cooldown");
        FairyAbilities.tick(first);
        test.assertTrue(owner.getHealth() == 11, "Healing must not repeat within ten seconds");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void recentDamageBlocksHealing(GameTestHelper test) {
        ServerPlayer owner = owner(test);
        NewFairyEntity fairy = head(test, FairyKind.CHERRY, owner);
        owner.setHealth(10);
        owner.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
        owner.getAbilities().invulnerable = false;
        test.assertTrue(owner.hurtServer(test.getLevel(), test.getLevel().damageSources().genericKill(), 1),
                "Fixture must apply environmental damage");
        float health = owner.getHealth();
        fairy.tickCount = 20;
        FairyAbilities.tick(fairy);
        test.assertTrue(owner.getHealth() == health, "Recent damage must block fairy healing");
        test.assertTrue(FairyAbilities.state(owner).lastDamage == FairyAbilities.time(test.getLevel()),
                "Damage hook must record environmental damage too");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void projectileEffectsHaveSharedCooldowns(GameTestHelper test) {
        Wolf target = test.spawn(EntityType.WOLF, CENTER);
        target.setNoAi(true);
        FairyAbilities.applyProjectileEffect(FairyKind.JUNGLE, target);
        test.assertTrue(target.hasEffect(MobEffects.POISON), "Jungle projectile must apply poison");
        target.removeEffect(MobEffects.POISON);
        FairyAbilities.applyProjectileEffect(FairyKind.JUNGLE, target);
        test.assertTrue(!target.hasEffect(MobEffects.POISON), "Another fairy must respect poison cooldown");
        FairyAbilities.applyProjectileEffect(FairyKind.PALE, target);
        test.assertTrue(target.hasEffect(MobEffects.WEAKNESS), "Pale projectile must apply weakness");
        target.removeEffect(MobEffects.WEAKNESS);
        FairyAbilities.applyProjectileEffect(FairyKind.PALE, target);
        test.assertTrue(!target.hasEffect(MobEffects.WEAKNESS), "Weakness cooldown must also be shared");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void magnetIgnoresUnownedAndForeignLoot(GameTestHelper test) {
        ServerPlayer owner = owner(test);
        NewFairyEntity fairy = head(test, FairyKind.ENDER, owner);
        ItemEntity own = item(test, CENTER.east(3));
        ItemEntity unowned = item(test, CENTER.west(3));
        ItemEntity foreign = item(test, CENTER.south(3));
        ((FairyLootAccess) own).setFairyLootOwner(owner.getUUID());
        ((FairyLootAccess) foreign).setFairyLootOwner(UUID.randomUUID());
        fairy.tickCount = 5;
        FairyAbilities.tick(fairy);
        test.assertTrue(own.getDeltaMovement().lengthSqr() > 0, "Magnet must attract marked loot belonging to owner");
        test.assertTrue(unowned.getDeltaMovement().equals(Vec3.ZERO), "Hand-dropped or unknown loot must remain");
        test.assertTrue(foreign.getDeltaMovement().equals(Vec3.ZERO), "Other players' loot must remain");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void fullInventoryStopsMagnet(GameTestHelper test) {
        ServerPlayer owner = owner(test);
        NewFairyEntity fairy = head(test, FairyKind.ENDER, owner);
        for (int i = 0; i < 36; i++) {
            owner.getInventory().setItem(i, new ItemStack(Items.STONE, 64));
        }
        ItemEntity item = item(test, CENTER.east(3));
        ((FairyLootAccess) item).setFairyLootOwner(owner.getUUID());
        fairy.tickCount = 5;
        FairyAbilities.tick(fairy);
        test.assertTrue(item.getDeltaMovement().equals(Vec3.ZERO), "Full inventory must prevent attraction");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void realBlockDropsCarryProvenance(GameTestHelper test) {
        ServerPlayer owner = owner(test);
        Block.dropResources(Blocks.DIAMOND_ORE.defaultBlockState(), test.getLevel(), test.absolutePos(CENTER),
                null, owner, new ItemStack(Items.DIAMOND_PICKAXE));
        test.assertTrue(test.getLevel().getEntitiesOfClass(ItemEntity.class,
                owner.getBoundingBox().inflate(3)).stream().anyMatch(item ->
                    owner.getUUID().equals(((FairyLootAccess) item).fairyLootOwner())),
                "Actual player block drops must receive provenance");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void realMobDropsCarryProvenance(GameTestHelper test) {
        ServerPlayer owner = owner(test);
        var sheep = test.spawn(EntityType.SHEEP, CENTER);
        sheep.setNoAi(true);
        sheep.hurtServer(test.getLevel(), test.getLevel().damageSources().playerAttack(owner), 1000);
        test.assertTrue(test.getLevel().getEntitiesOfClass(ItemEntity.class,
                owner.getBoundingBox().inflate(3)).stream().anyMatch(item ->
                    owner.getUUID().equals(((FairyLootAccess) item).fairyLootOwner())),
                "Actual mob loot must receive the killer provenance");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void mixedStacksLoseProvenance(GameTestHelper test) {
        ItemEntity marked = item(test, CENTER);
        ItemEntity unmarked = item(test, CENTER);
        ((FairyLootAccess) marked).setFairyLootOwner(UUID.randomUUID());
        for (int i = 0; i < 50 && marked.isAlive() && unmarked.isAlive(); i++) {
            marked.tick();
            unmarked.tick();
        }
        ItemEntity remaining = marked.isAlive() ? marked : unmarked;
        test.assertTrue(marked.isRemoved() || unmarked.isRemoved(), "Fixture must really merge item entities");
        test.assertTrue(remaining.getItem().getCount() == 2, "Merging must preserve item count");
        test.assertTrue(((FairyLootAccess) remaining).fairyLootOwner() == null,
                "Mixed provenance must never become owned loot");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void cherryHomeHealsOnlyOwnedPets(GameTestHelper test) {
        ServerPlayer owner = owner(test);
        ServerPlayer stranger = test.makeMockServerPlayerInLevel();
        NewFairyEntity fairy = test.spawn(ModEntities.fairy(FairyKind.CHERRY), CENTER);
        fairy.setNoAi(true);
        fairy.tame(owner);
        test.setBlock(CENTER, ModHollows.hollow(FairyKind.CHERRY));
        fairy.assignHome(test.absolutePos(CENTER));
        fairy.setPos(Vec3.atCenterOf(test.absolutePos(CENTER.east().above())));
        Wolf pet = test.spawn(EntityType.WOLF, CENTER.east());
        pet.tame(owner);
        pet.setNoAi(true);
        pet.setHealth(4);
        Wolf foreign = test.spawn(EntityType.WOLF, CENTER.west());
        foreign.tame(stranger);
        foreign.setNoAi(true);
        foreign.setHealth(1);
        fairy.tickCount = 20;
        long previousTime = test.getLevel().getDayTime();
        float previousRain = test.getLevel().getRainLevel(1);
        try {
            test.getLevel().setDayTime(1000);
            test.getLevel().setRainLevel(0);
            FairyAbilities.tick(fairy);
            test.assertTrue(pet.getHealth() == 5 && foreign.getHealth() == 1,
                    "Cherry home must heal its owner's pet and leave another owner's pet unchanged");
            owner.setHealth(10);
            FairyAbilities.tick(fairy);
            test.assertTrue(owner.getHealth() == 10, "A home must heal only one target per cycle");
        } finally {
            test.getLevel().setDayTime(previousTime);
            test.getLevel().setRainLevel(previousRain);
        }
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void projectilesSkipOwnerAndOtherFairies(GameTestHelper test) {
        ServerPlayer owner = owner(test);
        NewFairyEntity fairy = head(test, FairyKind.JUNGLE, owner);
        VariantFurballEntity shot = new VariantFurballEntity(test.getLevel(), fairy);
        test.assertTrue(shot.shouldSkipTarget(owner), "Projectile must skip its owner");
        test.assertTrue(shot.shouldSkipTarget(test.spawn(ModEntities.fairy(FairyKind.PALE), CENTER.east())),
                "Projectile must skip another fairy species");
        test.succeed();
    }

    @GameTest(environment = "dairatz-test:garden", structure = "dairatz-test:pollination", skyAccess = true)
    public void enderEvasionKeepsDamageAndSafePlacement(GameTestHelper test) {
        NewFairyEntity fairy = test.spawn(ModEntities.fairy(FairyKind.ENDER), CENTER);
        fairy.setNoAi(true);
        var attacker = test.spawn(EntityType.ZOMBIE, CENTER.east());
        attacker.setNoAi(true);
        fairy.getRandom().setSeed(0);
        Vec3 original = fairy.position();
        float health = fairy.getHealth();
        test.assertTrue(fairy.hurtServer(test.getLevel(), test.getLevel().damageSources().mobAttack(attacker), 1),
                "Fixture must apply a real melee hit");
        test.assertTrue(fairy.getHealth() < health, "Evasion must preserve received damage");
        test.assertTrue(!fairy.position().equals(original), "A safe evasion point must be used");
        test.assertTrue(FairyPlacement.isSafe(test.getLevel(), fairy, fairy.position()),
                "Evasion must not place fairy inside blocks or fluids");
        test.assertTrue(FairyAbilities.state(fairy).nextEvasion > FairyAbilities.time(test.getLevel()),
                "Successful evasion must start a cooldown");
        test.succeed();
    }

    private static ServerPlayer owner(GameTestHelper test) {
        ServerPlayer owner = test.makeMockServerPlayerInLevel();
        owner.setPos(Vec3.atCenterOf(test.absolutePos(CENTER)));
        return owner;
    }

    private static NewFairyEntity head(GameTestHelper test, FairyKind kind, ServerPlayer owner) {
        NewFairyEntity fairy = test.spawn(ModEntities.fairy(kind), CENTER);
        fairy.setNoAi(true);
        fairy.tame(owner);
        fairy.setOnHead(true);
        return fairy;
    }

    private static ItemEntity item(GameTestHelper test, BlockPos pos) {
        Vec3 point = Vec3.atCenterOf(test.absolutePos(pos));
        ItemEntity item = new ItemEntity(test.getLevel(), point.x, point.y, point.z, new ItemStack(Items.DIAMOND));
        item.setDeltaMovement(Vec3.ZERO);
        item.setNoGravity(true);
        test.getLevel().addFreshEntity(item);
        return item;
    }
}
