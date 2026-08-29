package com.squadeldee.wetlandwilds.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import com.squadeldee.wetlandwilds.wetlandwilds;

/**
 * Places both halves of a cattail stalk from a single click, the same way vanilla
 * handles its own double-tall plants. Default right-click raytracing skips fluids
 * entirely, so a plain BlockItem could never target water's surface at all -- useOn
 * is disabled and use() does its own fluid-aware raycast instead, the same trick
 * vanilla's PlaceOnWaterBlockItem uses for lily pad.
 *
 * Unlike lily pad/duckweed (which float ON TOP of water, placed one block above the
 * clicked source), the cattail bottom half OCCUPIES the water source itself, the same
 * way vanilla's seagrass does -- so placement targets the clicked position directly,
 * not one above it. After the bottom half places successfully, the top half is
 * placed directly above it.
 */
public class CattailsBlockItem extends BlockItem {
    public CattailsBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        InteractionResult placeResult = super.useOn(new UseOnContext(player, hand, hitResult));

        if (placeResult.consumesAction() && !level.isClientSide) {
            BlockPos bottomPos = hitResult.getBlockPos();
            BlockPos topPos = bottomPos.above();

            if (level.getBlockState(bottomPos).is(wetlandwilds.CATTAILS.get()) && level.getBlockState(topPos).isAir()) {
                level.setBlockAndUpdate(topPos, wetlandwilds.CATTAILS_TOP.get().defaultBlockState());
            }
        }

        return new InteractionResultHolder<>(placeResult, player.getItemInHand(hand));
    }
}
