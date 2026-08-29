package com.squadeldee.wetlandwilds.item;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import com.squadeldee.wetlandwilds.entity.ModEntityTypes;

/**
 * Vanilla's SpawnEggItem constructor requires a real EntityType immediately, but ours
 * can't exist yet at this point -- see ModEntityTypes' comment for why EntityType.Builder
 * .build(...) can only run inside ENTITY_TYPE's own RegisterEvent window, which on this
 * Forge build fires AFTER the ITEM registry's window (where this item's own construction
 * happens). EntityType.PLAYER is used as a throwaway placeholder for the super() call --
 * vanilla has no real spawn egg for players, so SpawnEggItem's internal BY_ID bookkeeping
 * for that key being "stolen" is inert. Every method that actually matters is overridden
 * below to resolve the real entity type lazily instead, which is always safe by the time
 * these are called (item use happens long after all registries are populated).
 *
 * EntityType.GIANT (not EntityType.PLAYER -- Player isn't a Mob, so it fails
 * SpawnEggItem's <? extends Mob> bound) is the placeholder: confirmed against vanilla's
 * own Items.java that it has no real spawn egg of its own to conflict with.
 */
public class BlubberFishSpawnEggItem extends SpawnEggItem {
    public BlubberFishSpawnEggItem(int backgroundColor, int highlightColor, Item.Properties properties) {
        super(EntityType.GIANT, backgroundColor, highlightColor, properties);
    }

    @Override
    public EntityType<?> getType(ItemStack stack) {
        return ModEntityTypes.BLUBBER_FISH.get();
    }

    @Override
    public boolean spawnsEntity(ItemStack stack, EntityType<?> type) {
        return ModEntityTypes.BLUBBER_FISH.get() == type;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return ModEntityTypes.BLUBBER_FISH.get().requiredFeatures();
    }
}
