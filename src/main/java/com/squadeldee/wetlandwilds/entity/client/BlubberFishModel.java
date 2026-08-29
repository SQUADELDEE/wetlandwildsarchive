package com.squadeldee.wetlandwilds.entity.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

import com.squadeldee.wetlandwilds.entity.BlubberFishEntity;

/**
 * Pitches the root "bone" based on the fish's current vertical velocity, so it
 * visibly noses down while swimming down and noses up while swimming up, instead
 * of staying perfectly level regardless of direction. Read directly off the
 * entity's live delta movement each frame rather than smoothed/cached -- the
 * same approach vanilla itself uses for velocity-driven visual effects.
 */
public class BlubberFishModel extends DefaultedEntityGeoModel<BlubberFishEntity> {
    private static final String ROOT_BONE = "bone";
    private static final double VELOCITY_TO_DEGREES = 220.0;

    public BlubberFishModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    public void setCustomAnimations(BlubberFishEntity animatable, long instanceId, AnimationState<BlubberFishEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        getBone(ROOT_BONE).ifPresent(bone -> {
            double verticalVelocity = animatable.getDeltaMovement().y;
            float pitchDegrees = (float) (verticalVelocity * VELOCITY_TO_DEGREES);

            bone.setRotX(pitchDegrees * Mth.DEG_TO_RAD);
        });
    }
}
