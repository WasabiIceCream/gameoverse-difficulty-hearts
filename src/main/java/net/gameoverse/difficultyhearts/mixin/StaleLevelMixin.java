package net.gameoverse.difficultyhearts.mixin;

import dev.muon.dynamic_difficulty.LevelingSystem;
import dev.muon.dynamic_difficulty.util.LevelingUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mobs that were leveled before {@link PassiveMobLevelsMixin} existed (villagers, fish, bats...)
 * keep Dynamic Difficulty's persistent level attachment. Reporting "no level" for any non-player
 * that can't have one stops that stale level being synced to clients (so no nameplate), used for
 * extra XP/loot, or matched by level loot conditions. Dynamic Difficulty's join handler then tries
 * to assign a level and its own canHaveLevel check skips it.
 */
@Mixin(value = LevelingSystem.class, remap = false)
public abstract class StaleLevelMixin {
    @Inject(method = "hasLevel", at = @At("HEAD"), cancellable = true)
    private static void gameoverse$noLevelWhenNotAllowed(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof LivingEntity && !(entity instanceof Player) && !LevelingUtils.canHaveLevel(entity)) {
            cir.setReturnValue(false);
        }
    }
}
