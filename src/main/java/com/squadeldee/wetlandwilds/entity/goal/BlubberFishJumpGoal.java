package com.squadeldee.wetlandwilds.entity.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import com.squadeldee.wetlandwilds.entity.BlubberFishEntity;

/**
 * A rare, purely cosmetic flourish: launches the fish up and forward out of the
 * water in a single impulse, letting gravity carry it through a natural arc
 * before it splashes back down -- the same physics AbstractFish's own
 * out-of-water flop already relies on, just triggered deliberately instead of
 * as a reaction to being stranded. One-shot: start() applies the impulse and
 * canContinueToUse() immediately returns false, so the goal doesn't fight the
 * fish's normal swim/school goals for more than an instant.
 */
public class BlubberFishJumpGoal extends Goal {
    private final BlubberFishEntity fish;

    public BlubberFishJumpGoal(BlubberFishEntity fish) {
        this.fish = fish;
    }

    @Override
    public boolean canUse() {
        if (!this.fish.isInWater() || this.fish.getRandom().nextInt(300) != 0) {
            return false;
        }

        BlockPos pos = this.fish.blockPosition();
        return this.fish.level().getBlockState(pos.above()).isAir()
                && this.fish.level().getFluidState(pos).is(FluidTags.WATER);
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        Vec3 look = this.fish.getLookAngle();
        double horizontal = 0.4 + this.fish.getRandom().nextDouble() * 0.2;
        double vertical = 0.55 + this.fish.getRandom().nextDouble() * 0.15;

        this.fish.setDeltaMovement(look.x * horizontal, vertical, look.z * horizontal);
        this.fish.hasImpulse = true;
    }
}
