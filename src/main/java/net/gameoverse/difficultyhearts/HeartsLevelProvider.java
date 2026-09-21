package net.gameoverse.difficultyhearts;

import dev.muon.dynamic_difficulty.api.PlayerLevelProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Bonus/malus mob level from the player's current max health, as set by the
 * gameoverse_hearts datapack's Heart Crystal system. See HeartsMath for the
 * formula. Uses the interface's default calculateBonusLevels() (average
 * across nearby players), matching this server's design choice - see
 * docs/decisions.md.
 */
public class HeartsLevelProvider implements PlayerLevelProvider {
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public int getPlayerLevel(ServerPlayer player) {
        double maxHealth = player.getAttributeValue(Attributes.MAX_HEALTH);
        return HeartsMath.level(HeartsMath.heartsFromMaxHealth(maxHealth));
    }
}
