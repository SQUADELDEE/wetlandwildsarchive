package com.squadeldee.wetlandwilds.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import com.squadeldee.wetlandwilds.wetlandwilds;

/**
 * A small clump of cattail stalks rooted right at the shoreline: each stalk needs
 * a column with exactly one block of water (solid ground below it, open air above
 * it), which is what naturally confines them to the shallow edges of ponds and
 * rivers instead of open water or dry land.
 */
public class CattailsFeature extends Feature<NoneFeatureConfiguration> {
    public CattailsFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        int attempts = 3 + random.nextInt(4); // 3-6 stalks in a loose clump
        boolean placed = false;
        for (int i = 0; i < attempts; i++) {
            int dx = random.nextInt(5) - 2; // -2..2
            int dz = random.nextInt(5) - 2;
            BlockPos bottom = shorelinePos(level, origin.offset(dx, 0, dz));

            if (isValidSpot(level, bottom)) {
                level.setBlock(bottom, wetlandwilds.CATTAILS.get().defaultBlockState(), 3);
                level.setBlock(bottom.above(), wetlandwilds.CATTAILS_TOP.get().defaultBlockState(), 3);
                placed = true;
            }
        }

        return placed;
    }

    private BlockPos shorelinePos(WorldGenLevel level, BlockPos columnPos) {
        int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, columnPos.getX(), columnPos.getZ());
        return new BlockPos(columnPos.getX(), y, columnPos.getZ());
    }

    private boolean isValidSpot(WorldGenLevel level, BlockPos pos) {
        FluidState fluid = level.getFluidState(pos);
        if (fluid.getType() != Fluids.WATER || !fluid.isSource()) {
            return false;
        }
        if (!level.getBlockState(pos.above()).isAir()) {
            return false;
        }

        BlockState ground = level.getBlockState(pos.below());
        return ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.MUD) || ground.is(Blocks.DIRT)
                || ground.is(Blocks.SAND) || ground.is(Blocks.CLAY);
    }
}
