package com.squadeldee.wetlandwilds.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import com.squadeldee.wetlandwilds.wetlandwilds;

/**
 * Places a single geyser on dry land, replacing the topmost ground block the same
 * way ShallowPoolFeature does -- a landmark feature meant to be a rare find scattered
 * across the wetlands, not a common one, so it's kept to one per placement attempt.
 */
public class GeyserFeature extends Feature<NoneFeatureConfiguration> {
    public GeyserFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();

        BlockPos ground = groundPos(level, origin);
        if (!isValidGround(level, ground)) {
            return false;
        }

        level.setBlock(ground, wetlandwilds.GEYSER.get().defaultBlockState(), 3);
        return true;
    }

    private BlockPos groundPos(WorldGenLevel level, BlockPos columnPos) {
        int airY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, columnPos.getX(), columnPos.getZ());
        return new BlockPos(columnPos.getX(), airY - 1, columnPos.getZ());
    }

    private boolean isValidGround(WorldGenLevel level, BlockPos ground) {
        BlockState state = level.getBlockState(ground);
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MUD) || state.is(Blocks.DIRT);
    }
}
