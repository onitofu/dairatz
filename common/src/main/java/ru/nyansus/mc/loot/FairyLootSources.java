package ru.nyansus.mc.loot;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import ru.nyansus.mc.entity.AbstractFairyEntity;

public final class FairyLootSources {
    private static final Map<ItemStack, UUID> PENDING = new WeakHashMap<>();

    private FairyLootSources() {
    }

    public static synchronized void markBlocks(Entity actor, List<ItemStack> stacks) {
        if (actor instanceof Player player) {
            if (PENDING.size() + stacks.size() > 1024) {
                PENDING.clear();
            }
            stacks.stream().limit(1024).forEach(stack -> PENDING.put(stack, player.getUUID()));
        }
    }

    public static synchronized UUID take(ItemStack stack) {
        return PENDING.remove(stack);
    }

    public static synchronized void clear() {
        PENDING.clear();
    }

    public static void markDeath(Entity source, ItemEntity item) {
        if (item == null || !(source instanceof LivingEntity living) || source instanceof Player
                || living.getHealth() > 0) {
            return;
        }
        Entity attacker = living.getLastDamageSource() == null ? null : living.getLastDamageSource().getEntity();
        if (!(attacker instanceof Player) && !(attacker instanceof AbstractFairyEntity)) {
            attacker = living.getKillCredit();
        }
        UUID owner = attacker instanceof Player player ? player.getUUID()
                : attacker instanceof AbstractFairyEntity fairy && fairy.isTame()
                    && fairy.getOwnerReference() != null ? fairy.getOwnerReference().getUUID() : null;
        ((FairyLootAccess) item).setFairyLootOwner(owner);
    }
}
