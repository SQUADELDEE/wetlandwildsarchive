package com.squadeldee.wetlandwilds.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.squadeldee.wetlandwilds.wetlandwilds;

/**
 * The upper half of a cattail stalk -- a plain air-based plant block that only
 * requires CattailsBlock directly beneath it. When the lower half stops existing
 * (e.g. its supporting ground is removed), BushBlock's own updateShape check makes
 * this half pop itself the same way any unsupported bush does, with no extra code.
 */
public class CattailsTopBlock extends BushBlock {
    public static final MapCodec<CattailsTopBlock> CODEC = simpleCodec(CattailsTopBlock::new);
    protected static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);

    public CattailsTopBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<CattailsTopBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(wetlandwilds.CATTAILS.get());
    }
}
