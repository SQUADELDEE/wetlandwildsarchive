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

/**
 * A small, raggedy clump of shallow puddles on dry land -- each one is just the
 * top ground block swapped for water (a single layer deep, flush with the
 * surrounding land, mud/dirt still underneath), scattered within a loose
 * circular clump rather than a hard-edged shape for a natural look.
 */
public class ShallowPoolFeature extends Feature<NoneFeatureConfiguration> {
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();

    public ShallowPoolFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        BlockPos originGround = groundPos(level, origin);
        if (!isValidGround(level, originGround)) {
            return false;
        }
        int originY = originGround.getY();

        double radius = 2.5 + random.nextDouble() * 1.5; // 2.5-4 block clump radius
        int tries = 8 + random.nextInt(6); // 8-13 puddle blocks attempted

        boolean placed = false;
        for (int i = 0; i < tries; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            // sqrt() biases distance sampling toward a uniform-area disc rather
            // than clumping everything near the center.
            double distance = radius * Math.sqrt(random.nextDouble());
            int dx = (int) Math.round(Math.cos(angle) * distance);
            int dz = (int) Math.round(Math.sin(angle) * distance);
            BlockPos ground = groundPos(level, origin.offset(dx, 0, dz));

            // Only place where the ground is level with the origin -- a puddle whose
            // blocks sit at different heights would just flow/spill instead of staying put.
            if (ground.getY() == originY && isValidGround(level, ground)) {
                level.setBlock(ground, WATER, 3);
                placed = true;
            }
        }

        return placed;
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
