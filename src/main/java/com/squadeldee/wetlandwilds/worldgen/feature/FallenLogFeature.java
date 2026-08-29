package com.squadeldee.wetlandwilds.worldgen.feature;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * A short line of fallen spruce logs lying on the ground, with a couple of
 * small mushrooms growing on top -- debris from the tree groves.
 */
public class FallenLogFeature extends Feature<NoneFeatureConfiguration> {
    public FallenLogFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        if (!isValidGround(level, origin)) {
            return false;
        }

        boolean alongX = random.nextBoolean();
        int length = 2 + random.nextInt(3); // 2-4 logs
        BlockState logState = Blocks.SPRUCE_LOG.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, alongX ? Direction.Axis.X : Direction.Axis.Z);

        List<BlockPos> placedLogs = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            BlockPos pos = alongX ? origin.offset(i, 0, 0) : origin.offset(0, 0, i);
            if (!isValidGround(level, pos)) {
                break; // stop the log where solid ground runs out
            }
            level.setBlock(pos, logState, 3);
            placedLogs.add(pos);
        }

        if (placedLogs.isEmpty()) {
            return false;
        }

        int mushroomAttempts = 1 + random.nextInt(2); // 1-2
        for (int i = 0; i < mushroomAttempts; i++) {
            BlockPos above = placedLogs.get(random.nextInt(placedLogs.size())).above();
            if (level.getBlockState(above).isAir()) {
                BlockState mushroom = random.nextBoolean()
                        ? Blocks.BROWN_MUSHROOM.defaultBlockState()
                        : Blocks.RED_MUSHROOM.defaultBlockState();
                level.setBlock(above, mushroom, 3);
            }
        }

        return true;
    }

    private boolean isValidGround(WorldGenLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir()) {
            return false;
        }
        BlockState below = level.getBlockState(pos.below());
        return below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.MUD) || below.is(Blocks.DIRT);
    }
}
