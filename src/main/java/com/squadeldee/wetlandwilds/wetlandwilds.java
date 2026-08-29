package com.squadeldee.wetlandwilds;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

import com.squadeldee.wetlandwilds.block.CattailsBlock;
import com.squadeldee.wetlandwilds.block.CattailsTopBlock;
import com.squadeldee.wetlandwilds.block.DuckweedBlock;
import com.squadeldee.wetlandwilds.block.GeyserBlock;
import com.squadeldee.wetlandwilds.block.entity.ModBlockEntities;
import com.squadeldee.wetlandwilds.entity.ModEntityTypes;
import com.squadeldee.wetlandwilds.item.BlubberFishBucketItem;
import com.squadeldee.wetlandwilds.item.BlubberFishSpawnEggItem;
import com.squadeldee.wetlandwilds.item.CattailsBlockItem;
import com.squadeldee.wetlandwilds.worldgen.ModFeatures;
import com.squadeldee.wetlandwilds.worldgen.WetlandwildsRegion;
import com.squadeldee.wetlandwilds.worldgen.WetlandwildsSurfaceRuleData;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(wetlandwilds.MODID)
public class wetlandwilds {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "wetlandwilds";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "wetlandwilds" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "wetlandwilds" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "wetlandwilds" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a new Block with the id "wetlandwilds:example_block", combining the namespace and path
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
    // Creates a new BlockItem with the id "wetlandwilds:example_block", combining the namespace and path
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block",
            () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));

    // Duckweed: a thin plant that floats on the water's surface, modeled on vanilla's lily pad
    // but non-collidable -- boats, players, and animals pass straight through it.
    public static final RegistryObject<DuckweedBlock> DUCKWEED = BLOCKS.register("duckweed",
            () -> new DuckweedBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak()
                    .sound(SoundType.LILY_PAD).noOcclusion().noCollission().pushReaction(PushReaction.DESTROY)));
    // A plain BlockItem can't place duckweed at all: default right-click raytracing skips
    // fluids entirely, so it can never target water's surface to place against. Vanilla's
    // own lily pad has the exact same problem and solves it with PlaceOnWaterBlockItem,
    // which does its own fluid-aware raycast and simulates a click on the position above
    // the water it finds.
    public static final RegistryObject<Item> DUCKWEED_ITEM = ITEMS.register("duckweed",
            () -> new PlaceOnWaterBlockItem(DUCKWEED.get(), new Item.Properties()));

    // Cattails: a two-tall plant rooted in exactly one block of water at the edges of
    // ponds and rivers. CATTAILS is the submerged base (occupies the water block itself,
    // like vanilla's seagrass); CATTAILS_TOP is the plain air-based upper half.
    public static final RegistryObject<CattailsBlock> CATTAILS = BLOCKS.register("cattails",
            () -> new CattailsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak()
                    .sound(SoundType.WET_GRASS).noOcclusion().noCollission().pushReaction(PushReaction.DESTROY)));
    // Custom item: fluid-aware placement (see DUCKWEED_ITEM above) plus placing CATTAILS_TOP
    // above the bottom half automatically, since a plain BlockItem only ever places one block.
    public static final RegistryObject<Item> CATTAILS_ITEM = ITEMS.register("cattails",
            () -> new CattailsBlockItem(CATTAILS.get(), new Item.Properties()));

    public static final RegistryObject<CattailsTopBlock> CATTAILS_TOP = BLOCKS.register("cattails_top",
            () -> new CattailsTopBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak()
                    .sound(SoundType.WET_GRASS).noOcclusion().noCollission().pushReaction(PushReaction.DESTROY)));

    // Geyser: a geothermal vent (vanilla mud texture with a vented top) that occasionally
    // erupts smoke, bubbles, and splashes, launching entities standing above it.
    public static final RegistryObject<GeyserBlock> GEYSER = BLOCKS.register("geyser",
            () -> new GeyserBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BLACK)
                    .strength(0.5F).sound(SoundType.MUD)));
    public static final RegistryObject<Item> GEYSER_ITEM = ITEMS.register("geyser",
            () -> new BlockItem(GEYSER.get(), new Item.Properties()));

    // Blubber Fish: a GeckoLib-animated schooling fish for the wetlands -- see BlubberFishEntity.
    // Custom item subclasses (not plain SpawnEggItem/MobBucketItem) because the real entity
    // type can't exist yet at this point -- see BlubberFishSpawnEggItem's doc comment.
    public static final RegistryObject<SpawnEggItem> BLUBBER_FISH_SPAWN_EGG = ITEMS.register("blubber_fish_spawn_egg",
            () -> new BlubberFishSpawnEggItem(0x4a6b5c, 0xd8c9a3, new Item.Properties()));

    // Produced when a blubber fish eats something (fed directly, or a dropped item it swam
    // to and ate on its own). Crafts into slimeballs with mud -- see the recipe json.
    public static final RegistryObject<Item> BLUBBERFISH_EXCREMENT = ITEMS.register("blubberfish_excrement",
            () -> new Item(new Item.Properties()));

    // Captures a blubber fish, the same way vanilla's own fish buckets work -- caught fish
    // won't despawn, matching vanilla's fromBucket() persistence behavior.
    public static final RegistryObject<MobBucketItem> BUCKET_OF_BLUBBERFISH = ITEMS.register("bucket_of_blubberfish",
            () -> new BlubberFishBucketItem(Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH,
                    new Item.Properties().stacksTo(1).component(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY)));

    // Creates a new food item with the id "wetlandwilds:example_id", nutrition 1 and saturation 2
    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .alwaysEdible().nutrition(1).saturationModifier(2f).build())));

    // The mod's own creative tab: icon is the geyser, contents are every custom block/plant
    // and the blubber fish's spawn egg.
    public static final RegistryObject<CreativeModeTab> WETLANDWILDS_TAB = CREATIVE_MODE_TABS.register("wetlandwilds_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.wetlandwilds"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> GEYSER_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(DUCKWEED_ITEM.get());
                output.accept(CATTAILS_ITEM.get());
                output.accept(GEYSER_ITEM.get());
                output.accept(BLUBBER_FISH_SPAWN_EGG.get());
                output.accept(BUCKET_OF_BLUBBERFISH.get());
                output.accept(BLUBBERFISH_EXCREMENT.get());
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    public wetlandwilds(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so worldgen features get registered
        ModFeatures.FEATURES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so block entity types get registered
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so entity types get registered
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);

        // Register the blubber fish's attributes and spawn placement rules
        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::registerSpawnPlacements);

        // Register ourselves for server and other game events we are interested in.
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that FML can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Claims a dedicated climate region for the wetlands biome and injects its
            // surface rules, without hand-overriding vanilla's noise_settings/overworld.json.
            Regions.register(new WetlandwildsRegion(ResourceLocation.fromNamespaceAndPath(MODID, "overworld_wetlands"), 2));
            SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, MODID, WetlandwildsSurfaceRuleData.makeRules());
        });

        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.get()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.get());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(BLUBBER_FISH_SPAWN_EGG);
        }
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.BLUBBER_FISH.get(), AbstractFish.createAttributes().build());
    }

    private void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(ModEntityTypes.BLUBBER_FISH.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WaterAnimal::checkSurfaceWaterAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
