package com.squadeldee.wetlandwilds.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import com.squadeldee.wetlandwilds.wetlandwilds;
import com.squadeldee.wetlandwilds.worldgen.feature.CattailsFeature;
import com.squadeldee.wetlandwilds.worldgen.feature.FallenLogFeature;
import com.squadeldee.wetlandwilds.worldgen.feature.GeyserFeature;
import com.squadeldee.wetlandwilds.worldgen.feature.MudRuinsFeature;
import com.squadeldee.wetlandwilds.worldgen.feature.PondFeature;
import com.squadeldee.wetlandwilds.worldgen.feature.RadialTreeGroveFeature;
import com.squadeldee.wetlandwilds.worldgen.feature.ShallowPoolFeature;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, wetlandwilds.MODID);

    public static final RegistryObject<RadialTreeGroveFeature> RADIAL_TREE_GROVE = FEATURES.register(
            "radial_tree_grove", () -> new RadialTreeGroveFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<FallenLogFeature> FALLEN_LOG = FEATURES.register(
            "fallen_log", () -> new FallenLogFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<ShallowPoolFeature> SHALLOW_POOL = FEATURES.register(
            "shallow_pool", () -> new ShallowPoolFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<CattailsFeature> CATTAILS = FEATURES.register(
            "cattails", () -> new CattailsFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<PondFeature> POND = FEATURES.register(
            "pond", () -> new PondFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<GeyserFeature> GEYSER = FEATURES.register(
            "geyser", () -> new GeyserFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<MudRuinsFeature> MUD_RUINS = FEATURES.register(
            "mud_ruins", () -> new MudRuinsFeature(NoneFeatureConfiguration.CODEC));
}
