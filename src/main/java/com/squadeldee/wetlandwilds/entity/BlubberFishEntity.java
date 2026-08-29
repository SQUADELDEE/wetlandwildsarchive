package com.squadeldee.wetlandwilds.entity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Set;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;

import com.squadeldee.wetlandwilds.entity.goal.BlubberFishEatItemGoal;
import com.squadeldee.wetlandwilds.entity.goal.BlubberFishJumpGoal;
import com.squadeldee.wetlandwilds.wetlandwilds;

/**
 * A GeckoLib-animated schooling fish for the wetlands. Schooling behavior comes
 * free from AbstractSchoolingFish (the same base vanilla Salmon uses); behavior
 * added on top of that: the occasional out-of-water jump (BlubberFishJumpGoal),
 * seeking out and eating dropped items in water (BlubberFishEatItemGoal), and
 * being fed directly by right-clicking with any item. Both feeding paths consume
 * one item and leave a blubberfish excrement item behind.
 */
public class BlubberFishEntity extends AbstractSchoolingFish implements GeoEntity {
    private static final RawAnimation SWIM = RawAnimation.begin().thenLoop("swim");

    // Plant-ish items with no FoodProperties component and no matching vanilla tag, so
    // they need listing explicitly rather than falling out of a tag/component check.
    private static final Set<Item> EXTRA_ORGANIC_ITEMS = Set.of(
            Items.BROWN_MUSHROOM, Items.RED_MUSHROOM, Items.CRIMSON_FUNGUS, Items.WARPED_FUNGUS,
            Items.SUGAR_CANE, Items.KELP, Items.SEAGRASS, Items.LILY_PAD, Items.VINE,
            Items.MOSS_BLOCK, Items.MOSS_CARPET, Items.BAMBOO, Items.CACTUS, Items.WHEAT, Items.HAY_BLOCK
    );

    private final AnimatableInstanceCache animatableInstanceCache = GeckoLibUtil.createInstanceCache(this);

    public BlubberFishEntity(EntityType<? extends BlubberFishEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new BlubberFishEatItemGoal(this));
        this.goalSelector.addGoal(6, new BlubberFishJumpGoal(this));
    }

    @Override
    public int getMaxSchoolSize() {
        return 5;
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(wetlandwilds.BUCKET_OF_BLUBBERFISH.get());
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        Optional<InteractionResult> bucketResult = Bucketable.bucketMobPickup(player, hand, this);
        if (bucketResult.isPresent()) {
            return bucketResult.get();
        }

        ItemStack heldItem = player.getItemInHand(hand);
        if (isOrganicFood(heldItem)) {
            if (!this.level().isClientSide) {
                heldItem.shrink(1);
                if (this.level() instanceof ServerLevel serverLevel) {
                    ItemEntity excrement = new ItemEntity(
                            serverLevel, this.getX(), this.getY() + 0.2, this.getZ(),
                            new ItemStack(wetlandwilds.BLUBBERFISH_EXCREMENT.get()));
                    serverLevel.addFreshEntity(excrement);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    /**
     * Whether this item is safe for a blubber fish to eat -- restricted to organic
     * material (food, plants, dirt) rather than any arbitrary item, so a player can't
     * accidentally lose a tool or valuable block by dropping it near a fish or
     * misclicking while feeding one. Used by both feeding paths: direct interaction
     * (above) and BlubberFishEatItemGoal, seeking out items dropped in water.
     */
    public static boolean isOrganicFood(ItemStack stack) {
        if (stack.isEmpty() || stack.is(wetlandwilds.BLUBBERFISH_EXCREMENT.get())) {
            return false;
        }
        if (stack.has(DataComponents.FOOD) || EXTRA_ORGANIC_ITEMS.contains(stack.getItem())) {
            return true;
        }
        return stack.is(ItemTags.SAPLINGS)
                || stack.is(ItemTags.LEAVES)
                || stack.is(ItemTags.FLOWERS)
                || stack.is(ItemTags.DIRT)
                || stack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS)
                || stack.is(wetlandwilds.DUCKWEED_ITEM.get())
                || stack.is(wetlandwilds.CATTAILS_ITEM.get());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.COD_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.COD_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.COD_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.COD_FLOP;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "swim_controller", 5,
                state -> state.setAndContinue(SWIM)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animatableInstanceCache;
    }
}
