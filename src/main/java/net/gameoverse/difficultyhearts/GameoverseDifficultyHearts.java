package net.gameoverse.difficultyhearts;

import dev.muon.dynamic_difficulty.api.LevelingAPI;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.level.ServerLevel;

public class GameoverseDifficultyHearts implements ModInitializer {
    @Override
    public void onInitialize() {
        LevelingAPI.registerPlayerLevelProvider(new HeartsLevelProvider());

        // Whether or not this broken block dropped a Heart Crystal, it's no
        // longer "placed" - clears PlacedBlocks tracking regardless of loot
        // outcome, so a natural block later placed over the same position
        // starts untracked again.
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world instanceof ServerLevel level) {
                PlacedBlocks.clear(level, pos);
            }
        });
    }
}
