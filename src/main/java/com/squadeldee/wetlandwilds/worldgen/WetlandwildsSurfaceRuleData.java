package com.squadeldee.wetlandwilds.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/**
 * Registered with TerraBlender's SurfaceRuleManager instead of hand-overriding the whole
 * (2500+ line) vanilla noise_settings/overworld.json: TerraBlender applies this rule source
 * only for columns whose biome is in the "wetlandwilds" namespace, falling through to its
 * vanilla-equivalent base rule (bedrock, deepslate, etc.) for anything we don't match here.
 */
public class WetlandwildsSurfaceRuleData {
    // Reuses vanilla's already-registered "surface"/"surface_swamp" noises rather than defining new ones.
    private static final ResourceKey<NormalNoise.NoiseParameters> SURFACE =
            ResourceKey.create(Registries.NOISE, ResourceLocation.withDefaultNamespace("surface"));
    private static final ResourceKey<NormalNoise.NoiseParameters> SURFACE_SWAMP =
            ResourceKey.create(Registries.NOISE, ResourceLocation.withDefaultNamespace("surface_swamp"));

    private static final SurfaceRules.RuleSource MUD = state(Blocks.MUD);
    private static final SurfaceRules.RuleSource GRASS_BLOCK = state(Blocks.GRASS_BLOCK);
    private static final SurfaceRules.RuleSource DIRT = state(Blocks.DIRT);
    private static final SurfaceRules.RuleSource SAND = state(Blocks.SAND);
    private static final SurfaceRules.RuleSource GRAVEL = state(Blocks.GRAVEL);

    public static SurfaceRules.RuleSource makeRules() {
        // Marsh fringe at the water's edge (Y62-63): mostly mud, matching vanilla swamp's
        // puddle band but resolving to mud instead of open water, per the "lean on mud" brief.
        SurfaceRules.ConditionSource marshFringe = SurfaceRules.not(
                SurfaceRules.yBlockCheck(VerticalAnchor.absolute(63), 0));
        SurfaceRules.RuleSource marshFringeRule = SurfaceRules.ifTrue(
                SurfaceRules.yBlockCheck(VerticalAnchor.absolute(62), 0),
                SurfaceRules.ifTrue(marshFringe,
                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(SURFACE_SWAMP, -0.2), MUD)));

        // Dry land: mud-dominant, with grass and dirt patches for variety and flower/grass placement.
        SurfaceRules.RuleSource dryLandTop = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(SURFACE, 0.15), MUD),
                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(SURFACE, -0.5), GRASS_BLOCK),
                DIRT);

        // Submerged lake/river bed: sand-dominant, with occasional gravel patches for texture.
        SurfaceRules.RuleSource lakebed = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(SURFACE, 0.6), GRAVEL),
                SAND);

        SurfaceRules.RuleSource islandTop = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.waterBlockCheck(-1, 0), dryLandTop),
                lakebed);

        SurfaceRules.RuleSource wetlandsSurface = SurfaceRules.sequence(
                marshFringeRule,
                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, islandTop));

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.WETLANDS), wetlandsSurface));
    }

    private static SurfaceRules.RuleSource state(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }
}
