package com.squadeldee.wetlandwilds.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import com.squadeldee.wetlandwilds.wetlandwilds;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, wetlandwilds.MODID);

    public static final RegistryObject<BlockEntityType<GeyserBlockEntity>> GEYSER = BLOCK_ENTITY_TYPES.register(
            "geyser",
            () -> BlockEntityType.Builder.of(GeyserBlockEntity::new, wetlandwilds.GEYSER.get()).build(null));
}
