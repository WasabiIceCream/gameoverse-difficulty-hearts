package net.gameoverse.difficultyhearts;

import dev.muon.dynamic_difficulty.api.PlayerLevelProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Bonus/malus mob level from the player's current max health, as set by the
 * gameoverse_hearts datapack's Heart Crystal system. 6 hearts (the datapack's
 * starting baseline) is level 0; every +/-4 hearts from there is +/-1 level.
 * Uses the interface's default calculateBonusLevels() (average across nearby
 * players), matching this server's design choice - see docs/decisions.md.
 */
public class HeartsLevelProvider implements PlayerLevelProvider {
    private static final double BASELINE_HEARTS = 6.0;
    private static final double HEARTS_PER_LEVEL = 4.0;

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public int getPlayerLevel(ServerPlayer player) {
        double maxHealth = player.getAttributeValue(Attributes.MAX_HEALTH);
        double hearts = maxHealth / 2.0;
        return (int) Math.floor((hearts - BASELINE_HEARTS) / HEARTS_PER_LEVEL);
    }
}
