package net.gameoverse.difficultyhearts;

import dev.muon.dynamic_difficulty.api.PlayerLevelProvider;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Bonus mob levels from the player's active Apotheosis World Tier: levelsPerWorldTier (default 10) per tier above
 * Haven, so Frontier +10 up to Pinnacle +40. Like the hearts provider, Dynamic Difficulty averages it across nearby
 * players, and it bypasses the dimension level cap (playerLevelBypassesCap). The tier unlock criteria in
 * gameoverse-farming-path (kill a level 20/40/60+ monster) are set about 10 levels above the bonus of the tier each
 * is unlocked from, so where you fight still matters.
 */
public class WorldTierLevelProvider implements PlayerLevelProvider {

    @Override
    public boolean isEnabled() {
        return HeartsConfig.get().levelsPerWorldTier != 0;
    }

    @Override
    public int getPlayerLevel(ServerPlayer player) {
        return WorldTier.getTier(player).ordinal() * HeartsConfig.get().levelsPerWorldTier;
    }
}
