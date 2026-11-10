package ru.nyansus.mc.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;
import ru.nyansus.mc.entity.FairyKind;

public class FairyRenderState extends LivingEntityRenderState {
    public FairyKind kind = FairyKind.STANDARD;
    public int appearance;
    public boolean resting;

    public final AnimationState idleAnimationState = new AnimationState();
}
