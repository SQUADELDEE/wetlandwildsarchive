package com.squadeldee.wetlandwilds.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * A conifer grove radiating out from a center point on dry, high ground: a single
 * giant mega spruce in the middle, a ring of tiered pines around it, and ordinary
 * spruce thinning out toward the edge -- with loose clumps of bamboo scattered
 * through the grove, similar to how Guatape's islands cluster tall growth inland
 * and shorter growth toward the shore. Vines are draped along the lower trunks
 * for a damp, overgrown look.
 */
public class RadialTreeGroveFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceKey<ConfiguredFeature<?, ?>> BAMBOO_NO_PODZOL = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, ResourceLocation.withDefaultNamespace("bamboo_no_podzol"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> VINES = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, ResourceLocation.withDefaultNamespace("vines"));

    public RadialTreeGroveFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ChunkGenerator generator = context.chunkGenerator();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        if (!isValidGround(level, origin)) {
            return false;
        }

        ConfiguredFeature<?, ?> mega = configuredFeature(level, TreeFeatures.MEGA_SPRUCE);
        ConfiguredFeature<?, ?> pine = configuredFeature(level, TreeFeatures.PINE);
        ConfiguredFeature<?, ?> spruce = configuredFeature(level, TreeFeatures.SPRUCE);
        ConfiguredFeature<?, ?> bamboo = configuredFeature(level, BAMBOO_NO_PODZOL);
        ConfiguredFeature<?, ?> vines = configuredFeature(level, VINES);

        double radius = 5 + random.nextInt(3); // 5-7 block grove radius
        boolean placedAnything = mega.place(level, generator, random, origin);
        if (placedAnything) {
            tryAttachVines(level, generator, random, vines, origin, 6);
        }

        // Middle ring: tiered pines.
        placedAnything |= ringOfTrees(level, generator, random, origin, pine, vines,
                radius * 0.35, radius * 0.65, 3 + random.nextInt(3));

        // Outer ring: ordinary spruce, thinning toward the edge.
        placedAnything |= ringOfTrees(level, generator, random, origin, spruce, vines,
                radius * 0.65, radius, 4 + random.nextInt(4));

        // Loose bamboo clumps scattered through the grove.
        int clumpCount = 2 + random.nextInt(3);
        for (int i = 0; i < clumpCount; i++) {
            BlockPos clumpCenter = offset(origin, random.nextDouble() * Math.PI * 2, radius * (0.3 + random.nextDouble() * 0.7));
            int stalks = 3 + random.nextInt(4);
            for (int s = 0; s < stalks; s++) {
                BlockPos stalkColumn = offset(clumpCenter, random.nextDouble() * Math.PI * 2, random.nextDouble() * 1.5);
                BlockPos stalkPos = surfacePos(level, stalkColumn);
                if (isValidGround(level, stalkPos)) {
                    placedAnything |= bamboo.place(level, generator, random, stalkPos);
                }
            }
        }

        return placedAnything;
    }

    private ConfiguredFeature<?, ?> configuredFeature(WorldGenLevel level, ResourceKey<ConfiguredFeature<?, ?>> key) {
        return level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getOrThrow(key);
    }

    private boolean ringOfTrees(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin,
            ConfiguredFeature<?, ?> tree, ConfiguredFeature<?, ?> vines, double minRadius, double maxRadius, int count) {
        boolean placed = false;
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = minRadius + random.nextDouble() * (maxRadius - minRadius);
            BlockPos pos = surfacePos(level, offset(origin, angle, distance));
            if (isValidGround(level, pos)) {
                if (tree.place(level, generator, random, pos)) {
                    placed = true;
                    tryAttachVines(level, generator, random, vines, pos, 3);
                }
            }
        }
        return placed;
    }

    // VinesFeature only checks whether ITS OWN target position is air with a valid
    // (e.g. log) neighbour to attach to -- it doesn't search for one itself. So we
    // spam several candidate air spots up and around a tree's base and let each
    // attempt self-filter; most will fail harmlessly (return false) unless they
    // happen to land beside an actual trunk block, same strategy vanilla's own
    // default "vines" placement uses (127 unfocused attempts per chunk).
    private void tryAttachVines(WorldGenLevel level, ChunkGenerator generator, RandomSource random,
            ConfiguredFeature<?, ?> vines, BlockPos treeBase, int attempts) {
        for (int i = 0; i < attempts; i++) {
            int dy = random.nextInt(10);
            Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos candidate = treeBase.above(dy).relative(side);
            vines.place(level, generator, random, candidate);
        }
    }

    private BlockPos offset(BlockPos origin, double angle, double distance) {
        int dx = (int) Math.round(Math.cos(angle) * distance);
        int dz = (int) Math.round(Math.sin(angle) * distance);
        return origin.offset(dx, 0, dz);
    }

    private BlockPos surfacePos(WorldGenLevel level, BlockPos columnPos) {
        int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, columnPos.getX(), columnPos.getZ());
        return new BlockPos(columnPos.getX(), y, columnPos.getZ());
    }

    // Rejects underwater/submerged spots (the position itself must be air, not water)
    // and requires the ground below to be one of our biome's actual surface materials.
    private boolean isValidGround(WorldGenLevel level, BlockPos surfacePos) {
        if (!level.getBlockState(surfacePos).isAir()) {
            return false;
        }
        BlockState below = level.getBlockState(surfacePos.below());
        return below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.MUD) || below.is(Blocks.DIRT);
    }
}
