package ru.nyansus.mc.animations;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationChannel.Interpolations;
import net.minecraft.client.animation.AnimationChannel.Targets;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class IdleAnimation {

    private static Keyframe deg(float time, float x, float y, float z) {
        return new Keyframe(time, KeyframeAnimations.degreeVec(x, y, z), Interpolations.LINEAR);
    }

    private static Keyframe pos(float time, float x, float y, float z) {
        return new Keyframe(time, KeyframeAnimations.posVec(x, y, z), Interpolations.LINEAR);
    }

    public static final AnimationDefinition IDLE = AnimationDefinition.Builder.withLength(4.0F).looping()
            .addAnimation("body", new AnimationChannel(Targets.ROTATION,
                    deg(0.0F, 0.0F, 0.0F, 0.0F),
                    deg(2.0F, 2.5F, 0.0F, 0.0F),
                    deg(4.0F, 0.0F, 0.0F, 0.0F)
            ))
            .addAnimation("body", new AnimationChannel(Targets.POSITION,
                    pos(0.0F, 0.0F, 0.0F, 0.0F),
                    pos(2.0F, 0.0F, 0.5F, 0.0F),
                    pos(4.0F, 0.0F, 0.0F, 0.0F)
            ))
            .addAnimation("rightWing", new AnimationChannel(Targets.ROTATION,
                    deg(0.0F, 0.0F, -30.0F, 0.0F),
                    deg(0.125F, 0.0F, 30.0F, 0.0F),
                    deg(0.25F, 0.0F, -30.0F, 0.0F),
                    deg(0.375F, 0.0F, 30.0F, 0.0F),
                    deg(0.5F, 0.0F, -30.0F, 0.0F),
                    deg(0.625F, 0.0F, 30.0F, 0.0F),
                    deg(0.75F, 0.0F, -30.0F, 0.0F),
                    deg(0.875F, 0.0F, 30.0F, 0.0F),
                    deg(1.0F, 0.0F, -30.0F, 0.0F),
                    deg(1.125F, 0.0F, 30.0F, 0.0F),
                    deg(1.25F, 0.0F, -30.0F, 0.0F),
                    deg(1.375F, 0.0F, 30.0F, 0.0F),
                    deg(1.5F, 0.0F, -30.0F, 0.0F),
                    deg(1.625F, 0.0F, 30.0F, 0.0F),
                    deg(1.75F, 0.0F, -30.0F, 0.0F),
                    deg(1.875F, 0.0F, 30.0F, 0.0F),
                    deg(2.0F, 0.0F, -30.0F, 0.0F),
                    deg(2.125F, 0.0F, 30.0F, 0.0F),
                    deg(2.25F, 0.0F, -30.0F, 0.0F),
                    deg(2.375F, 0.0F, 30.0F, 0.0F),
                    deg(2.5F, 0.0F, -30.0F, 0.0F),
                    deg(2.625F, 0.0F, 30.0F, 0.0F),
                    deg(2.75F, 0.0F, -30.0F, 0.0F),
                    deg(2.875F, 0.0F, 30.0F, 0.0F),
                    deg(3.0F, 0.0F, -30.0F, 0.0F),
                    deg(3.125F, 0.0F, 30.0F, 0.0F),
                    deg(3.25F, 0.0F, -30.0F, 0.0F),
                    deg(3.375F, 0.0F, 30.0F, 0.0F),
                    deg(3.5F, 0.0F, -30.0F, 0.0F),
                    deg(3.625F, 0.0F, 30.0F, 0.0F),
                    deg(3.75F, 0.0F, -30.0F, 0.0F),
                    deg(3.875F, 0.0F, 30.0F, 0.0F),
                    deg(4.0F, 0.0F, -30.0F, 0.0F)
            ))
            .addAnimation("leftWing", new AnimationChannel(Targets.ROTATION,
                    deg(0.0F, 0.0F, 30.0F, 0.0F),
                    deg(0.125F, 0.0F, -30.0F, 0.0F),
                    deg(0.25F, 0.0F, 30.0F, 0.0F),
                    deg(0.375F, 0.0F, -30.0F, 0.0F),
                    deg(0.5F, 0.0F, 30.0F, 0.0F),
                    deg(0.625F, 0.0F, -30.0F, 0.0F),
                    deg(0.75F, 0.0F, 30.0F, 0.0F),
                    deg(0.875F, 0.0F, -30.0F, 0.0F),
                    deg(1.0F, 0.0F, 30.0F, 0.0F),
                    deg(1.125F, 0.0F, -30.0F, 0.0F),
                    deg(1.25F, 0.0F, 30.0F, 0.0F),
                    deg(1.375F, 0.0F, -30.0F, 0.0F),
                    deg(1.5F, 0.0F, 30.0F, 0.0F),
                    deg(1.625F, 0.0F, -30.0F, 0.0F),
                    deg(1.75F, 0.0F, 30.0F, 0.0F),
                    deg(1.875F, 0.0F, -30.0F, 0.0F),
                    deg(2.0F, 0.0F, 30.0F, 0.0F),
                    deg(2.125F, 0.0F, -30.0F, 0.0F),
                    deg(2.25F, 0.0F, 30.0F, 0.0F),
                    deg(2.375F, 0.0F, -30.0F, 0.0F),
                    deg(2.5F, 0.0F, 30.0F, 0.0F),
                    deg(2.625F, 0.0F, -30.0F, 0.0F),
                    deg(2.75F, 0.0F, 30.0F, 0.0F),
                    deg(2.875F, 0.0F, -30.0F, 0.0F),
                    deg(3.0F, 0.0F, 30.0F, 0.0F),
                    deg(3.125F, 0.0F, -30.0F, 0.0F),
                    deg(3.25F, 0.0F, 30.0F, 0.0F),
                    deg(3.375F, 0.0F, -30.0F, 0.0F),
                    deg(3.5F, 0.0F, 30.0F, 0.0F),
                    deg(3.625F, 0.0F, -30.0F, 0.0F),
                    deg(3.75F, 0.0F, 30.0F, 0.0F),
                    deg(3.875F, 0.0F, -30.0F, 0.0F),
                    deg(4.0F, 0.0F, 30.0F, 0.0F)
            ))
            .addAnimation("paws", new AnimationChannel(Targets.POSITION,
                    pos(0.0F, 0.0F, 0.0F, 0.0F),
                    pos(2.0F, 0.0F, 0.55F, 0.0F),
                    pos(4.0F, 0.0F, 0.0F, 0.0F)
            ))
            .build();
}
