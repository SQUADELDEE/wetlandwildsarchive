package com.squadeldee.wetlandwilds.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

import com.squadeldee.wetlandwilds.block.entity.GeyserBlockEntity;
import com.squadeldee.wetlandwilds.block.entity.ModBlockEntities;

/**
 * A geothermal vent that erupts every 2-5 seconds, sending up white smoke, rising
 * bubbles, and water splashes, and launching any entity standing above it. Backed
 * by GeyserBlockEntity for its ticking -- see that class for why (not randomTick,
 * not a scheduled tick from onPlace).
 */
public class GeyserBlock extends BaseEntityBlock {
    public static final MapCodec<GeyserBlock> CODEC = simpleCodec(GeyserBlock::new);

    public GeyserBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<GeyserBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // BaseEntityBlock defaults to INVISIBLE (for blocks rendered entirely by a
        // BlockEntityRenderer, e.g. chests); this block still uses a normal baked
        // model, so render it like any other block.
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GeyserBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.GEYSER.get(), GeyserBlockEntity::tick);
    }
}
