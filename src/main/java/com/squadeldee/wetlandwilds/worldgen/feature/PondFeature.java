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
 * A small pond dug into dry land, the water counterpart to vanilla's lava pool
 * features (the same LAKES generation step it's registered in). Unlike vanilla's
 * own (deprecated) LakeFeature -- which always digs 4+ blocks into solid rock and
 * needs a strict solid shell to carve into, a poor fit for a shallow marsh -- this
 * carves a shallow basin whose depth eases from a few blocks at the center down to
 * nothing at the rim, traced from one reference surface height so the floor reads
 * as a coherent dip rather than following every bump in the local terrain.
 */
public class PondFeature extends Feature<NoneFeatureConfiguration> {
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();

    public PondFeature(Codec<NoneFeatureConfiguration> codec) {
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

        double radius = 3.5 + random.nextDouble() * 2.5; // 3.5-6 block pond radius
        int maxDepth = 2 + random.nextInt(2); // 2-3 blocks deep at the center
        int originY = originGround.getY();
        int r = (int) Math.ceil(radius);

        // Require the whole footprint to be level with the origin before carving anything.
        // Digging into uneven terrain would leave the water table following the bumps
        // instead of sitting flat, so it'd flow/spill to equalize rather than staying put.
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (Math.sqrt(dx * dx + dz * dz) > radius) {
                    continue;
                }
                BlockPos columnGround = groundPos(level, origin.offset(dx, 0, dz));
                if (columnGround.getY() != originY || !isValidGround(level, columnGround)) {
                    return false;
                }
            }
        }

        boolean placed = false;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > radius) {
                    continue;
                }

                int depth = (int) Math.round(maxDepth * (1.0 - distance / radius));
                if (depth <= 0) {
                    continue; // rim tapers to untouched ground for a natural edge
                }

                BlockPos column = origin.offset(dx, 0, dz);
                int floorY = originY - depth;
                for (int y = originY; y >= floorY; y--) {
                    BlockPos pos = new BlockPos(column.getX(), y, column.getZ());
                    if (!isValidGround(level, pos) && !level.getBlockState(pos).isAir()) {
                        break; // hit something that isn't diggable ground (e.g. stone)
                    }
                    level.setBlock(pos, WATER, 3);
                    placed = true;
                }
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
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MUD) || state.is(Blocks.DIRT)
                || state.is(Blocks.SAND) || state.is(Blocks.CLAY);
    }
}
