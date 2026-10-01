package net.gameoverse.difficultyhearts.mixin;

import dev.muon.dynamic_difficulty.api.BiomeBonus;
import dev.muon.dynamic_difficulty.data.DimensionLevelingSettingsStore;
import dev.muon.dynamic_difficulty.util.LocationBonusUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A dimension's leveling settings can turn biome bonuses off ({@code apply_level_bonuses.biome: false}, the Overworld
 * here since 2026-09-30). Mob leveling ({@code LevelingSystem}) honours that, but the area level shown to players (the
 * "Lv. N" popup, sent by {@code PlayerLocationTracker}) and {@code LevelingAPI.getLevelAt} still added the biome's bonus,
 * so a savanna by spawn showed level 5 while its mobs were level 1. With the switch off, the biome's bonus is 0
 * everywhere (the biome id is kept for the biome title).
 */
@Mixin(LocationBonusUtils.class)
public abstract class BiomeBonusSwitchMixin {
    @Inject(method = "getBiomeAt", at = @At("RETURN"), cancellable = true)
    private static void gameoverse$honourBiomeSwitch(ServerLevel level, BlockPos pos, CallbackInfoReturnable<BiomeBonus> cir) {
        BiomeBonus bonus = cir.getReturnValue();
        if (bonus == null || bonus.totalBonus() == 0) return;
        var apply = DimensionLevelingSettingsStore.get(level.dimension()).applyLevelBonuses();
        if (apply != null && !apply.biome()) {
            cir.setReturnValue(new BiomeBonus(bonus.biomeId(), 0, 0));
        }
    }
}
