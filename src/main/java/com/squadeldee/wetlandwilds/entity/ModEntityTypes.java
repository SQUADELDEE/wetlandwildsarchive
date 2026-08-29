package com.squadeldee.wetlandwilds.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.squadeldee.wetlandwilds.wetlandwilds;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, wetlandwilds.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<BlubberFishEntity>> BLUBBER_FISH = ENTITY_TYPES.register(
            "blubber_fish",
            () -> EntityType.Builder.of(BlubberFishEntity::new, MobCategory.WATER_AMBIENT)
                    .sized(0.7F, 0.45F)
                    .eyeHeight(0.25F)
                    .clientTrackingRange(4)
                    .build("blubber_fish"));
}
