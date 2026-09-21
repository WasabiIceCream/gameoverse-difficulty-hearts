package net.gameoverse.difficultyhearts.mixin;

import net.gameoverse.difficultyhearts.UniversalHeartDrops;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Universal Heart Crystal fishing-catch roll. Fires right after
 * FishingHook#retrieve's real fishing loot table roll
 * (LootTable#getRandomItems) completes, via a shift=AFTER @Inject on that
 * exact call - not wrapping anything, just observing that a genuine catch
 * happened at this point in the method (as opposed to pulling in a hooked
 * mob/item, or an empty reel-in, which don't reach this call at all).
 * getRandomItems() itself flows through the same shared
 * getRandomItemsRaw() consumer Apotheosis wraps, which is exactly why this
 * hooks a point after that call returns rather than the call itself - see
 * UniversalHeartDrops' javadoc for why that method is never touched
 * directly.
 */
@Mixin(FishingHook.class)
public abstract class FishingHookMixin {
    @Inject(
        method = "retrieve",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            shift = At.Shift.AFTER
        )
    )
    private void gameoverse$rollFishingDrop(ItemStack rod, CallbackInfoReturnable<Integer> cir) {
        FishingHook self = (FishingHook) (Object) this;
        Player owner = self.getPlayerOwner();
        if (owner instanceof ServerPlayer && self.level() instanceof ServerLevel level) {
            UniversalHeartDrops.rollFishing(level, self.getX(), self.getY(), self.getZ());
        }
    }
}
