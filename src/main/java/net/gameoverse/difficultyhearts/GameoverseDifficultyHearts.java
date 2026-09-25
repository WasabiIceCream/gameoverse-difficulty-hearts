package net.gameoverse.difficultyhearts;

import dev.muon.dynamic_difficulty.api.LevelingAPI;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class GameoverseDifficultyHearts implements ModInitializer {
    @Override
    public void onInitialize() {
        // Must run before any world loads; see PlacedBlocks.init().
        PlacedBlocks.init();
        LevelingAPI.registerPlayerLevelProvider(new HeartsLevelProvider());

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (damageSource.getEntity() instanceof ServerPlayer && entity.level() instanceof ServerLevel level) {
                UniversalHeartDrops.rollKill(level, entity.getX(), entity.getY(), entity.getZ());
            }
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world instanceof ServerLevel level) {
                // Roll before clearing - rollBlockBreak needs the (about to
                // be cleared) placed-flag to decide eligibility.
                UniversalHeartDrops.rollBlockBreak(level, pos, state);
                PlacedBlocks.clear(level, pos);
            }
        });
    }
}
