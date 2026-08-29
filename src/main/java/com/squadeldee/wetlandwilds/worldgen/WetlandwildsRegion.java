package com.squadeldee.wetlandwilds.worldgen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.ParameterUtils;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

/**
 * Registers our own climate point instead of cloning all of vanilla swamp's (the
 * previous approach, via addBiomeSimilar): swamp's own point spans continentalness
 * all the way from near-coast out to far-inland, which is why it -- and by extension
 * our old wetlands clone of it -- could generate as an elevated, plateau-like variant
 * far from any water. Using vanilla's flattest erosion band keeps it from being bumpy.
 * This is TerraBlender's normal per-biome registration API -- it doesn't touch any
 * shared density-function/noise-settings file, so it can't conflict with Terralith
 * or vanilla's own worldgen the way a density-function override would.
 *
 * Continentalness deliberately starts at NEAR_INLAND, not COAST -- vanilla's own swamp
 * registration never includes COAST for the same reason described below.
 *
 * Second registered point (2026-08-28): a deliberately catch-all DEFERRED_PLACEHOLDER
 * point. Narrowing the climate box (dropping COAST) alone did NOT fully fix "wetlands
 * generating as a huge expanse of open water/no land" -- confirmed by disassembling
 * TerraBlender's actual mixin classes (MixinParameterList#findValuePositional): which
 * REGION owns a given map tile is decided by a low-resolution, climate-blind layered
 * noise system (LayeredNoiseUtil / getUniqueness), entirely separate from the real
 * per-column climate sampling that decides terrain height. Once a tile is territorially
 * "ours", the biome search only ever considers points THIS region registered -- there is
 * no fallback to vanilla's real ocean/mountain points -- so a tile we win that's
 * climatically real ocean still gets forced into wetlands regardless of how narrow our
 * own climate box is. TerraBlender's sanctioned fix is DEFERRED_PLACEHOLDER: a second
 * point registered here with the widest possible climate span defers back to vanilla's
 * own point-set (region index 0) whenever it's the closer match. Per Climate.java's
 * actual fitness formula (sum of squared per-axis distances + offset^2), a FULL_RANGE
 * point has zero distance on every axis for any column, so a small nonzero offset here
 * (vs. our wetlands point's 0) is enough to let wetlands win cleanly inside its own box
 * while the deferred point wins everywhere else.
 */
public class WetlandwildsRegion extends Region {
    public WetlandwildsRegion(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        this.addBiome(
                mapper,
                ParameterUtils.Temperature.span(ParameterUtils.Temperature.COOL, ParameterUtils.Temperature.NEUTRAL),
                ParameterUtils.Humidity.FULL_RANGE.parameter(),
                ParameterUtils.Continentalness.span(ParameterUtils.Continentalness.NEAR_INLAND, ParameterUtils.Continentalness.MID_INLAND),
                ParameterUtils.Erosion.EROSION_6.parameter(),
                ParameterUtils.Weirdness.FULL_RANGE.parameter(),
                ParameterUtils.Depth.SURFACE.parameter(),
                0.0F,
                ModBiomes.WETLANDS
        );

        // Catch-all: defers to vanilla's own biome (including real ocean) for any tile
        // territorially ours whose actual climate falls outside the box above. See the
        // class doc for why this is necessary, not just a narrower box on its own.
        this.addBiome(
                mapper,
                ParameterUtils.Temperature.FULL_RANGE.parameter(),
                ParameterUtils.Humidity.FULL_RANGE.parameter(),
                ParameterUtils.Continentalness.FULL_RANGE.parameter(),
                ParameterUtils.Erosion.FULL_RANGE.parameter(),
                ParameterUtils.Weirdness.FULL_RANGE.parameter(),
                ParameterUtils.Depth.FULL_RANGE.parameter(),
                0.1F,
                Region.DEFERRED_PLACEHOLDER
        );
    }
}
