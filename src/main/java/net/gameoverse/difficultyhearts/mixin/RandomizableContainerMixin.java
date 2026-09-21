package net.gameoverse.difficultyhearts.mixin;

import net.gameoverse.difficultyhearts.UniversalHeartDrops;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Universal Heart Crystal chest-loot roll. Plain @Inject(TAIL) on the
 * interface default method itself - no consumer-wrapping, no reentrancy
 * guard, nothing to compose with any other mod. Deliberately not
 * implemented via the same LootTable#getRandomItemsRaw hook the kill/block
 * cases used to share before v1.3.0's revert - see
 * UniversalHeartDrops' javadoc.
 */
@Mixin(RandomizableContainer.class)
public interface RandomizableContainerMixin {
    @Inject(method = "unpackLootTable", at = @At("TAIL"))
    private void gameoverse$rollChestDrop(Player player, CallbackInfo ci) {
        RandomizableContainer self = (RandomizableContainer) this;
        Level level = self.getLevel();
        if (player instanceof ServerPlayer && level instanceof ServerLevel serverLevel) {
            UniversalHeartDrops.rollChest(serverLevel, self.getBlockPos());
        }
    }
}
