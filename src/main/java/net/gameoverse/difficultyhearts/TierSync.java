package net.gameoverse.difficultyhearts;

import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Keeps each online player's Apotheosis World Tier in lockstep with their
 * current hearts (gameoverse_hearts datapack), using the same level formula
 * as the Dynamic Difficulty mob scaling (HeartsMath) so "10 hearts" always
 * means the same bonus everywhere. More hearts -> higher tier -> better loot
 * odds and stronger tier buffs (see the tier_augments data in
 * mod-dev/apotheosis-fabric), on top of tougher nearby mobs from the
 * Dynamic Difficulty side - the risk and the reward scale together.
 * <p>
 * Bypasses WorldTier's advancement-gated unlock check entirely (that check
 * only gates the player-facing Tier Select GUI, not the underlying
 * WorldTier.setTier call), since this server wants hearts alone to drive
 * tier, not the vanilla Apotheosis progression ladder.
 */
public final class TierSync {
    private static final WorldTier[] TIERS = WorldTier.values();
    private static int tickCounter = 0;

    private TierSync() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (++tickCounter < 20) {
                return;
            }
            tickCounter = 0;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                double maxHealth = player.getAttributeValue(Attributes.MAX_HEALTH);
                int level = HeartsMath.level(HeartsMath.heartsFromMaxHealth(maxHealth));
                int tierIndex = Math.max(0, Math.min(TIERS.length - 1, level));
                WorldTier desired = TIERS[tierIndex];

                if (WorldTier.getTier(player) != desired) {
                    WorldTier.setTier(player, desired);
                }
            }
        });
    }
}
