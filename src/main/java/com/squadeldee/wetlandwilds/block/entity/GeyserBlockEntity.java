package com.squadeldee.wetlandwilds.block.entity;

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Drives the geyser's eruption with its own tick countdown rather than vanilla's
 * ambient randomTick (too sparse and imprecise -- a single block's expected
 * randomTick gap is ~68 seconds at default settings) or a scheduled tick queued
 * from onPlace (worldgen's own block placement skips that callback, so a
 * worldgen-placed geyser would never get its first tick queued). A block entity's
 * ticker is wired up unconditionally whenever the block is set into the world --
 * confirmed against LevelChunk#setBlockState, the same code path worldgen itself
 * uses -- so this works identically whether the geyser was placed by a player or
 * generated with the world.
 *
 * Each eruption is a shaped ~5 second event -- rise, sustain, fall -- rather than
 * an instant burst, like a real geyser or a pressure release: the column's height
 * and the entity-launching push both ramp up, hold, then taper back down over the
 * course of the eruption instead of firing all at once.
 */
public class GeyserBlockEntity extends BlockEntity {
    private static final int MIN_COOLDOWN = 40; // 2 seconds between eruptions
    private static final int MAX_COOLDOWN = 100; // 5 seconds between eruptions

    private static final int RISE_TICKS = 20; // 1s
    private static final int SUSTAIN_TICKS = 60; // 3s
    private static final int FALL_TICKS = 20; // 1s
    private static final int ERUPTION_DURATION = RISE_TICKS + SUSTAIN_TICKS + FALL_TICKS; // 5s total

    private static final double MAX_HEIGHT = 8.0;
    private static final double PEAK_PUSH_PER_TICK = 0.12;

    private int cooldown;
    private int eruptionElapsed = -1; // -1 means not currently erupting

    public GeyserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GEYSER.get(), pos, state);
        this.cooldown = ThreadLocalRandom.current().nextInt(MIN_COOLDOWN, MAX_COOLDOWN + 1);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GeyserBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (blockEntity.eruptionElapsed >= 0) {
            tickEruption(serverLevel, pos, blockEntity.eruptionElapsed);
            blockEntity.eruptionElapsed++;
            if (blockEntity.eruptionElapsed >= ERUPTION_DURATION) {
                blockEntity.eruptionElapsed = -1;
                blockEntity.cooldown = MIN_COOLDOWN + level.getRandom().nextInt(MAX_COOLDOWN - MIN_COOLDOWN + 1);
            }
        } else if (--blockEntity.cooldown <= 0) {
            blockEntity.eruptionElapsed = 0;
            tickEruption(serverLevel, pos, 0);
        }
    }

    private static double phaseFactor(int elapsed) {
        if (elapsed < RISE_TICKS) {
            return elapsed / (double) RISE_TICKS;
        } else if (elapsed < RISE_TICKS + SUSTAIN_TICKS) {
            return 1.0;
        } else {
            int fallElapsed = elapsed - RISE_TICKS - SUSTAIN_TICKS;
            return Math.max(0.0, 1.0 - fallElapsed / (double) FALL_TICKS);
        }
    }

    private static void tickEruption(ServerLevel level, BlockPos pos, int elapsed) {
        double x = pos.getX() + 0.5;
        double baseY = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;

        if (elapsed == 0) {
            // the initial pressure-release "pop" as the vent first opens
            level.sendParticles(ParticleTypes.BUBBLE_POP, x, baseY, z, 18, 0.3, 0.05, 0.3, 0.08);
        }

        double factor = phaseFactor(elapsed);
        double height = Math.max(0.3, MAX_HEIGHT * factor);
        double midY = baseY + height / 2.0;

        level.sendParticles(ParticleTypes.WHITE_SMOKE, x, midY, z, 3, 0.22, height / 2.0, 0.22, 0.01);
        level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, x, midY, z, 2, 0.18, height / 2.0, 0.18, 0.04);
        if (level.getRandom().nextInt(4) == 0) {
            level.sendParticles(ParticleTypes.SPLASH, x, baseY + 0.1, z, 2, 0.25, 0.05, 0.25, 0.08);
        }

        if (factor > 0.05) {
            double strength = PEAK_PUSH_PER_TICK * factor;
            AABB column = new AABB(pos).expandTowards(0, height, 0).inflate(0.4, 0, 0.4);
            for (Entity entity : level.getEntities((Entity) null, column, EntitySelector.NO_SPECTATORS)) {
                entity.push(0, strength, 0);
                entity.hurtMarked = true;
            }
        }
    }
}
