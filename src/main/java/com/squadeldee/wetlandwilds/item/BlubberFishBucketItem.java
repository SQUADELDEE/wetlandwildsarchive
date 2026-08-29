package com.squadeldee.wetlandwilds.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;

import com.squadeldee.wetlandwilds.entity.ModEntityTypes;

/**
 * Same problem and same fix as BlubberFishSpawnEggItem: vanilla's MobBucketItem
 * constructor requires a real EntityType immediately, but ours can't exist yet this
 * early. EntityType.PLAYER is used as a throwaway placeholder for the super() call.
 * Vanilla's own spawn logic (MobBucketItem#checkExtraContent -> private #spawn) is
 * reimplemented here in full rather than overridden, since the private #spawn method
 * that actually uses the stored EntityType can't be intercepted from a subclass --
 * only checkExtraContent (which calls it) is public/overridable.
 *
 * EntityType.GIANT is the placeholder (same choice as BlubberFishSpawnEggItem, for
 * consistency -- MobBucketItem's constructor itself only requires EntityType<?>, no
 * Mob bound, so EntityType.PLAYER would also have compiled here).
 */
public class BlubberFishBucketItem extends MobBucketItem {
    public BlubberFishBucketItem(Fluid fluid, SoundEvent emptySound, Item.Properties properties) {
        super(EntityType.GIANT, fluid, emptySound, properties);
    }

    @Override
    public void checkExtraContent(@Nullable Player player, Level level, ItemStack stack, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            if (ModEntityTypes.BLUBBER_FISH.get().spawn(serverLevel, stack, null, pos, MobSpawnType.BUCKET, true, false) instanceof Bucketable bucketable) {
                CustomData customData = stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY);
                bucketable.loadFromBucketTag(customData.copyTag());
                bucketable.setFromBucket(true);
            }

            level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
        }
    }
}
