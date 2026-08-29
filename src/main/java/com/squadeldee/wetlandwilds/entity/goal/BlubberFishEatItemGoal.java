package com.squadeldee.wetlandwilds.entity.goal;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import com.squadeldee.wetlandwilds.entity.BlubberFishEntity;
import com.squadeldee.wetlandwilds.wetlandwilds;

/**
 * Seeks out a dropped organic item (see BlubberFishEntity#isOrganicFood -- food,
 * plants, dirt; deliberately NOT arbitrary items, so a player can't lose a tool or
 * valuable block just by dropping it near a fish) floating in water near the fish,
 * swims to it, and eats it -- consuming one from the stack and leaving a
 * blubberfish excrement item behind, the same outcome as feeding the fish directly
 * (see BlubberFishEntity#mobInteract).
 */
public class BlubberFishEatItemGoal extends Goal {
    private static final double SEARCH_RADIUS = 8.0;
    private static final double EAT_DISTANCE_SQR = 1.2;

    private final BlubberFishEntity fish;
    private ItemEntity target;

    public BlubberFishEatItemGoal(BlubberFishEntity fish) {
        this.fish = fish;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.fish.isInWater()) {
            return false;
        }

        List<ItemEntity> nearby = this.fish.level().getEntitiesOfClass(
                ItemEntity.class,
                this.fish.getBoundingBox().inflate(SEARCH_RADIUS),
                item -> item.isAlive() && item.isInWater() && BlubberFishEntity.isOrganicFood(item.getItem()));

        if (nearby.isEmpty()) {
            return false;
        }

        this.target = nearby.get(this.fish.getRandom().nextInt(nearby.size()));
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null && this.target.isAlive() && this.fish.isInWater();
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public void tick() {
        this.fish.getNavigation().moveTo(this.target, 1.0);

        if (this.fish.distanceToSqr(this.target) <= EAT_DISTANCE_SQR) {
            eat();
        }
    }

    private void eat() {
        ItemStack stack = this.target.getItem();
        stack.shrink(1);
        if (stack.isEmpty()) {
            this.target.discard();
        }

        if (this.fish.level() instanceof ServerLevel serverLevel) {
            ItemEntity excrement = new ItemEntity(
                    serverLevel, this.fish.getX(), this.fish.getY() + 0.2, this.fish.getZ(),
                    new ItemStack(wetlandwilds.BLUBBERFISH_EXCREMENT.get()));
            serverLevel.addFreshEntity(excrement);
        }

        this.target = null;
    }
}
