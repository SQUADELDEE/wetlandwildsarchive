package com.squadeldee.wetlandwilds.entity.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import software.bernie.geckolib.renderer.GeoEntityRenderer;

import com.squadeldee.wetlandwilds.entity.BlubberFishEntity;
import com.squadeldee.wetlandwilds.wetlandwilds;

/**
 * Uses BlubberFishModel (rather than GeoEntityRenderer's defaulted-asset
 * constructor) so the model can apply the velocity-driven pitch in
 * BlubberFishModel#setCustomAnimations. The asset subpath ("blubber_fish")
 * still matches the entity's registered id -- same geo/animation/texture files
 * under geo/entity, animations/entity, and textures/entity as before.
 */
public class BlubberFishRenderer extends GeoEntityRenderer<BlubberFishEntity> {
    public BlubberFishRenderer(EntityRendererProvider.Context context) {
        super(context, new BlubberFishModel(ResourceLocation.fromNamespaceAndPath(wetlandwilds.MODID, "blubber_fish")));
    }
}
