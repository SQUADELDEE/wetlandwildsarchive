package com.squadeldee.wetlandwilds.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.squadeldee.wetlandwilds.wetlandwilds;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, wetlandwilds.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeyserBlockEntity>> GEYSER = BLOCK_ENTITY_TYPES.register(
            "geyser",
            () -> BlockEntityType.Builder.of(GeyserBlockEntity::new, wetlandwilds.GEYSER.get()).build(null));
}
