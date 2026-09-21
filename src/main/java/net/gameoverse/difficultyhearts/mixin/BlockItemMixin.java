package net.gameoverse.difficultyhearts.mixin;

import net.gameoverse.difficultyhearts.PlacedBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Records every successful player block placement for PlacedBlocks. Only
 * matters for the universal Heart Crystal loot roll's non-crop block-break
 * case; crops use their own age blockstate instead (see PlacedBlocks'
 * javadoc).
 */
@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Inject(method = "place", at = @At("RETURN"))
    private void gameoverse$recordPlacement(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue().consumesAction() && context.getLevel() instanceof ServerLevel level) {
            PlacedBlocks.markPlaced(level, context.getClickedPos());
        }
    }
}
