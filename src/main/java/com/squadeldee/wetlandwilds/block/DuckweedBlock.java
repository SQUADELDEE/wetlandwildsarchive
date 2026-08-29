package com.squadeldee.wetlandwilds.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A thin plant that floats flush on the water's surface, modeled on vanilla's
 * own lily pad (WaterlilyBlock) for placement (requires water or ice below,
 * empty space above) -- but registered with noCollission() so boats, players,
 * and animals all pass straight through it instead of being blocked or, in
 * a boat's case, destroying it.
 */
public class DuckweedBlock extends BushBlock {
    public static final MapCodec<DuckweedBlock> CODEC = simpleCodec(DuckweedBlock::new);
    protected static final VoxelShape AABB = Block.box(1.0, 0.0, 1.0, 15.0, 1.5, 15.0);

    public DuckweedBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<DuckweedBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AABB;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        FluidState fluidStateAbove = level.getFluidState(pos.above());
        return (fluidState.getType() == Fluids.WATER || state.getBlock() instanceof IceBlock)
                && fluidStateAbove.getType() == Fluids.EMPTY;
    }
}
