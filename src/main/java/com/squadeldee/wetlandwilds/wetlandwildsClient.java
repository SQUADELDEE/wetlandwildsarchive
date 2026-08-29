package com.squadeldee.wetlandwilds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import com.squadeldee.wetlandwilds.entity.ModEntityTypes;
import com.squadeldee.wetlandwilds.entity.client.BlubberFishRenderer;

// Forge doesn't support NeoForge's separate client-dist @Mod class with its own
// constructor/ModContainer injection -- client-only setup goes through a plain
// @Mod.EventBusSubscriber(bus = MOD, value = CLIENT) class instead, the pattern
// confirmed directly from Forge's own 1.21.1 MDK.
//
// One thing dropped in this port: NeoForge's ConfigurationScreen (an auto-generated
// config GUI from a ModConfigSpec) has no Forge equivalent -- Forge's
// ConfigScreenHandler expects you to hand-write your own Screen. Since Config.java's
// values are unused template placeholders, not real mod settings, skipping the GUI
// screen doesn't lose anything functional; the config file itself still works fine.
@Mod.EventBusSubscriber(modid = wetlandwilds.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class wetlandwildsClient {
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Duckweed's texture has transparent areas; without this it defaults to the
            // "solid" render layer, which ignores alpha and renders those areas as black.
            // Same render type vanilla itself uses for lily pad.
            ItemBlockRenderTypes.setRenderLayer(wetlandwilds.DUCKWEED.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(wetlandwilds.CATTAILS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(wetlandwilds.CATTAILS_TOP.get(), RenderType.cutout());
        });

        // Some client setup code
        wetlandwilds.LOGGER.info("HELLO FROM CLIENT SETUP");
        wetlandwilds.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.BLUBBER_FISH.get(), BlubberFishRenderer::new);
    }
}
