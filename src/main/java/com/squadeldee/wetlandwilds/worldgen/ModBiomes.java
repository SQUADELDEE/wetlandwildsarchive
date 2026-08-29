package com.squadeldee.wetlandwilds.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import com.squadeldee.wetlandwilds.wetlandwilds;

public class ModBiomes {
    public static final ResourceKey<Biome> WETLANDS = ResourceKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(wetlandwilds.MODID, "wetlands"));
}
