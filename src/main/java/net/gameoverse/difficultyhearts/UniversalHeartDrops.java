package net.gameoverse.difficultyhearts;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Universal rare Heart Crystal drop, across basically every loot source, at
 * a flat/fixed chance per source category (not heart-scaled, unlike the
 * Looting/affix-chance bonuses - this is a discovery path for players with
 * zero hearts invested yet). No trade-table case, this server has no
 * villager trading.
 * <p>
 * Deliberately NOT implemented via a mixin on the shared
 * LootTable#getRandomItemsRaw consumer - see v1.3.0's postmortem in
 * docs/current-state.md (mod-dev git history has the full commit). That
 * approach broke every block drop server-wide by fighting with
 * Apotheosis's own wrap of the same method. Each source here hooks its own
 * independent, non-wrapping Fabric event/injection point instead, so there
 * is nothing to compose (safely or otherwise) with any other mod's mixin.
 * <p>
 * Rates themselves live in HeartsConfig (config/gameoverse_difficulty_hearts.json),
 * not hardcoded here, so they're tunable without a rebuild.
 */
public final class UniversalHeartDrops {
    private UniversalHeartDrops() {
    }

    public static void rollKill(ServerLevel level, double x, double y, double z) {
        roll(level, HeartsConfig.get().killChance, x, y, z);
    }

    public static void rollChest(ServerLevel level, BlockPos pos) {
        roll(level, HeartsConfig.get().chestChance, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static void rollFishing(ServerLevel level, double x, double y, double z) {
        roll(level, HeartsConfig.get().fishingChance, x, y, z);
    }

    public static void rollBlockBreak(ServerLevel level, BlockPos pos, BlockState state) {
        HeartsConfig config = HeartsConfig.get();
        float chance;
        if (state.getBlock() instanceof CropBlock crop) {
            if (!crop.isMaxAge(state)) {
                // Immature crops never drop, full stop - not eligible for
                // the general-block placement fallback below. Clearing your
                // own unripe farm shouldn't have any chance at all.
                return;
            }
            // Maturity itself proves real elapsed growth time, regardless of
            // who planted the seed - no placement check needed.
            chance = config.matureCropChance;
        } else if (!PlacedBlocks.isPlaced(level, pos)) {
            chance = config.generalBlockChance;
        } else {
            return;
        }
        roll(level, chance, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    private static void roll(ServerLevel level, float chance, double x, double y, double z) {
        if (level.getRandom().nextFloat() >= chance) {
            return;
        }
        ItemStack crystal = HeartCrystalItems.create(level);
        if (!crystal.isEmpty()) {
            Containers.dropItemStack(level, x, y, z, crystal);
        }
    }
}
