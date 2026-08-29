package com.squadeldee.wetlandwilds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import com.squadeldee.wetlandwilds.entity.ModEntityTypes;
import com.squadeldee.wetlandwilds.entity.client.BlubberFishRenderer;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = wetlandwilds.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = wetlandwilds.MODID, value = Dist.CLIENT)
public class wetlandwildsClient {
    public wetlandwildsClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

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
