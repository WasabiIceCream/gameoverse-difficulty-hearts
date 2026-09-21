package net.gameoverse.difficultyhearts;

import dev.muon.dynamic_difficulty.api.LevelingAPI;
import net.fabricmc.api.ModInitializer;

public class GameoverseDifficultyHearts implements ModInitializer {
    @Override
    public void onInitialize() {
        LevelingAPI.registerPlayerLevelProvider(new HeartsLevelProvider());
        TierSync.register();
    }
}
