package ru.nyansus.mc.entity.ai;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

record FairyInterest(FairyHabit habit, Vec3 focus, Entity subject) {
    Vec3 currentFocus() {
        return subject == null ? focus : subject.position().add(0, subject.getBbHeight() * 0.5, 0);
    }
}
