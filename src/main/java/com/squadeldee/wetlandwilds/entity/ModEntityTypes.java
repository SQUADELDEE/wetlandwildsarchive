package com.squadeldee.wetlandwilds.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import com.squadeldee.wetlandwilds.wetlandwilds;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, wetlandwilds.MODID);

    // EntityType.Builder.build(...) MUST run only inside this registration supplier, never
    // as an eager static field -- Forge patches EntityType's own constructor to write directly
    // into the registry as it's built ("intrusive holder"), which only works during that
    // registry's own brief writable window (RegisterEvent dispatch for ENTITY_TYPE). Building
    // it eagerly crashed with "Registry is already frozen", since mod construction (where a
    // static field would run) happens before ANY registry's window opens. Confirmed against
    // Forge's own docs: "Registered objects should not be stored in fields when they are
    // created and registered... always newly created... whenever RegisterEvent is fired."
    //
    // Every reference to the built EntityType elsewhere in this mod must go through this
    // RegistryObject's own .get() -- but see item/BlubberFishSpawnEggItem and
    // item/BlubberFishBucketItem for why the spawn egg and bucket items can't just call
    // .get() directly at their own construction time.
    public static final RegistryObject<EntityType<BlubberFishEntity>> BLUBBER_FISH = ENTITY_TYPES.register(
            "blubber_fish",
            () -> EntityType.Builder.of(BlubberFishEntity::new, MobCategory.WATER_AMBIENT)
                    .sized(0.7F, 0.45F)
                    .eyeHeight(0.25F)
                    .clientTrackingRange(4)
                    .build("blubber_fish"));
}
