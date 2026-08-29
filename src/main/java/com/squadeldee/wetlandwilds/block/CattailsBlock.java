package com.squadeldee.wetlandwilds.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * The submerged base of a two-tall cattail stalk. Modeled on vanilla's SeagrassBlock:
 * it occupies the water block itself (reporting a water FluidState) rather than sitting
 * on top of it, so breaking it leaves water behind. Requires solid ground directly below
 * and open air directly above -- the latter is what confines cattails to exactly one
 * block of water depth, matching real cattails rooting at the shallow edges of ponds
 * and rivers rather than out in open, deeper water.
 */
public class CattailsBlock extends BushBlock implements LiquidBlockContainer {
    public static final MapCodec<CattailsBlock> CODEC = simpleCodec(CattailsBlock::new);
    protected static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);

    public CattailsBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<CattailsBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        // "pos" here is the ground position (one below the cattail itself), per BushBlock's
        // canSurvive -- pos.above() is where the cattail actually sits, which must be a real
        // water source. Without this check, a player could place cattails on dry land; since
        // the block unconditionally reports itself as a water source (see getFluidState
        // below), the scheduled water tick would then "correct" that lie by overwriting the
        // block with real water the next time it ran.
        boolean validGround = state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MUD) || state.is(Blocks.DIRT)
                || state.is(Blocks.SAND) || state.is(Blocks.CLAY);
        FluidState fluidAbove = level.getFluidState(pos.above());
        return validGround && fluidAbove.getType() == Fluids.WATER && fluidAbove.isSource();
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return Fluids.WATER.getSource(false);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState result = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        if (!result.isAir()) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return result;
    }

    @Override
    public boolean canPlaceLiquid(@Nullable Player player, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }
}
