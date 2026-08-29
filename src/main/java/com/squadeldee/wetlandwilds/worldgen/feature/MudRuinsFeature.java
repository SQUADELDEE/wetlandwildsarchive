package com.squadeldee.wetlandwilds.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * A small, rare mud-brick ruin: a low, partly-collapsed rectangular footprint with
 * a random size, decay pattern (missing/cracked bricks), suspicious gravel dig
 * spots wired to vanilla's own trail-ruins loot tables, and a handful of decorated
 * pots with randomized sherd patterns -- reusing vanilla's own archaeology system
 * (brushable blocks, pot decorations/loot) rather than a bespoke one. No two ruins
 * are identical: footprint size, wall gaps/height, brick decay, and pot/gravel
 * placement are all randomized per instance.
 */
public class MudRuinsFeature extends Feature<NoneFeatureConfiguration> {
    // Vanilla has no cracked/weathered mud brick variant; plain packed mud stands in
    // for a brick position that's crumbled back to raw material -- reads as decay
    // without needing a block that doesn't exist.
    private static final BlockState MUD_BRICKS = Blocks.MUD_BRICKS.defaultBlockState();
    private static final BlockState WEATHERED_MUD_BRICKS = Blocks.PACKED_MUD.defaultBlockState();
    private static final BlockState SUSPICIOUS_GRAVEL = Blocks.SUSPICIOUS_GRAVEL.defaultBlockState();

    private static final Item[] SHERD_POOL = {
            Items.ANGLER_POTTERY_SHERD, Items.BREWER_POTTERY_SHERD, Items.DANGER_POTTERY_SHERD,
            Items.EXPLORER_POTTERY_SHERD, Items.FLOW_POTTERY_SHERD, Items.FRIEND_POTTERY_SHERD,
            Items.HEART_POTTERY_SHERD, Items.MINER_POTTERY_SHERD, Items.PLENTY_POTTERY_SHERD,
            Items.SCRAPE_POTTERY_SHERD, Items.SHEAF_POTTERY_SHERD, Items.SHELTER_POTTERY_SHERD,
            Items.SNORT_POTTERY_SHERD
    };

    public MudRuinsFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        BlockPos originGround = groundPos(level, origin);
        if (!isValidGround(level, originGround)) {
            return false;
        }
        int floorY = originGround.getY();

        int halfW = 2 + random.nextInt(2); // 5-7 wide
        int halfL = 2 + random.nextInt(2); // 5-7 long

        // Require the whole footprint to be level with the origin -- a ruin sitting on
        // uneven terrain would look like it's floating or half-buried, not a real ruin.
        for (int dx = -halfW; dx <= halfW; dx++) {
            for (int dz = -halfL; dz <= halfL; dz++) {
                BlockPos columnGround = groundPos(level, origin.offset(dx, 0, dz));
                if (columnGround.getY() != floorY || !isValidGround(level, columnGround)) {
                    return false;
                }
            }
        }

        // Floor: mostly mud bricks, some cracked, occasional eroded gaps down to bare ground.
        for (int dx = -halfW; dx <= halfW; dx++) {
            for (int dz = -halfL; dz <= halfL; dz++) {
                BlockPos pos = new BlockPos(origin.getX() + dx, floorY, origin.getZ() + dz);
                float roll = random.nextFloat();
                if (roll < 0.08F) {
                    continue; // eroded gap, leave the natural ground showing through
                }
                level.setBlock(pos, roll < 0.3F ? WEATHERED_MUD_BRICKS : MUD_BRICKS, 3);
            }
        }

        // Perimeter wall: low and ruined, with random collapsed sections and height variation.
        for (int dx = -halfW; dx <= halfW; dx++) {
            for (int dz = -halfL; dz <= halfL; dz++) {
                boolean edge = dx == -halfW || dx == halfW || dz == -halfL || dz == halfL;
                if (!edge || random.nextFloat() < 0.35F) {
                    continue; // interior, or a collapsed section of wall
                }

                int wallHeight = random.nextFloat() < 0.3F ? 2 : 1;
                for (int y = 1; y <= wallHeight; y++) {
                    BlockPos pos = new BlockPos(origin.getX() + dx, floorY + y, origin.getZ() + dz);
                    if (!level.getBlockState(pos).isAir()) {
                        break;
                    }
                    level.setBlock(pos, random.nextFloat() < 0.25F ? WEATHERED_MUD_BRICKS : MUD_BRICKS, 3);
                }
            }
        }

        int width = halfW * 2 + 1;
        int length = halfL * 2 + 1;

        int gravelCount = 2 + random.nextInt(3); // 2-4 dig spots
        for (int i = 0; i < gravelCount; i++) {
            placeSuspiciousGravel(level, random, origin, random.nextInt(width) - halfW, random.nextInt(length) - halfL, floorY);
        }

        int potCount = 1 + random.nextInt(3); // 1-3 pots
        for (int i = 0; i < potCount; i++) {
            placePot(level, random, origin, random.nextInt(width) - halfW, random.nextInt(length) - halfL, floorY);
        }

        return true;
    }

    private void placeSuspiciousGravel(WorldGenLevel level, RandomSource random, BlockPos origin, int dx, int dz, int floorY) {
        BlockPos pos = new BlockPos(origin.getX() + dx, floorY + 1, origin.getZ() + dz);
        if (!level.getBlockState(pos).isAir()) {
            return;
        }

        level.setBlock(pos, SUSPICIOUS_GRAVEL, 3);
        if (level.getBlockEntity(pos) instanceof BrushableBlockEntity brushable) {
            ResourceKey<LootTable> lootTable = random.nextFloat() < 0.25F
                    ? BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_RARE
                    : BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON;
            brushable.setLootTable(lootTable, random.nextLong());
        }
    }

    private void placePot(WorldGenLevel level, RandomSource random, BlockPos origin, int dx, int dz, int floorY) {
        BlockPos pos = new BlockPos(origin.getX() + dx, floorY + 1, origin.getZ() + dz);
        if (!level.getBlockState(pos).isAir()) {
            return;
        }

        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        level.setBlock(pos, Blocks.DECORATED_POT.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing), 3);

        if (level.getBlockEntity(pos) instanceof DecoratedPotBlockEntity pot) {
            pot.setFromItem(DecoratedPotBlockEntity.createDecoratedPotItem(randomDecorations(random)));
            if (random.nextFloat() < 0.2F) {
                pot.setLootTable(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON, random.nextLong());
            }
        }
    }

    // Mostly blank sides, with the occasional sherd -- matches how vanilla's own
    // trail ruins pots read (rarely fully decorated on every side).
    private PotDecorations randomDecorations(RandomSource random) {
        return new PotDecorations(randomSide(random), randomSide(random), randomSide(random), randomSide(random));
    }

    private Item randomSide(RandomSource random) {
        return random.nextFloat() < 0.5F ? Items.BRICK : SHERD_POOL[random.nextInt(SHERD_POOL.length)];
    }

    private BlockPos groundPos(WorldGenLevel level, BlockPos columnPos) {
        int airY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, columnPos.getX(), columnPos.getZ());
        return new BlockPos(columnPos.getX(), airY - 1, columnPos.getZ());
    }

    private boolean isValidGround(WorldGenLevel level, BlockPos ground) {
        BlockState state = level.getBlockState(ground);
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MUD) || state.is(Blocks.DIRT)
                || state.is(Blocks.SAND) || state.is(Blocks.CLAY);
    }
}
