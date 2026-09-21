package net.gameoverse.difficultyhearts.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.gameoverse.difficultyhearts.HeartCrystalItems;
import net.gameoverse.difficultyhearts.PlacedBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.function.Consumer;

/**
 * Universal rare Heart Crystal drop, across basically every loot source, at
 * a flat/fixed chance per source category (not scaled by hearts - that's
 * the separate Looting/affix-chance boost, this is a discovery path for
 * players with zero hearts invested yet). Same target/pattern as
 * apotheosis-fabric's own LootTableGlobalModifiersMixin (that project's own
 * javadoc confirms LootTable#getRandomItemsRaw is the single funnel point
 * for every real drop/chest-fill/block-break query, with recursive
 * loot-table references reusing the same call needing a reentrancy guard),
 * but implemented fully independently here - own ThreadLocals, no
 * dependency on or coupling to Apotheosis's internals. Multiple mods
 * wrapping the same method's consumer parameter compose safely in Mixin
 * (each sees the prior wrapper as its own "original").
 * <p>
 * Source categories, in priority order (mutually exclusive by construction
 * - a single query is never both a kill and a block break):
 * <ul>
 * <li>Kill (LAST_DAMAGE_PLAYER present): 0.5%</li>
 * <li>Block break, mature crop (BLOCK_STATE present, block is a CropBlock
 * at max age): 0.2%. No placement check needed - maturity itself proves
 * real elapsed growth time, regardless of who planted the seed.</li>
 * <li>Block break, everything else (BLOCK_STATE present): 0.05%, and only
 * if PlacedBlocks says this position wasn't player-placed - otherwise a
 * trivial place-and-break loop would farm this. Extremely low because
 * normal play involves breaking a huge number of blocks.</li>
 * <li>Chest open (THIS_ENTITY is a ServerPlayer, no BLOCK_STATE): 1%</li>
 * </ul>
 * No trade-table case - this server has no villager trading.
 */
@Mixin(LootTable.class)
public abstract class UniversalHeartDropMixin {
    private static final float KILL_CHANCE = 0.005F;
    private static final float CHEST_CHANCE = 0.01F;
    private static final float MATURE_CROP_CHANCE = 0.002F;
    private static final float GENERAL_BLOCK_CHANCE = 0.0005F;

    @Unique
    private static final ThreadLocal<Integer> gameoverse$depth = ThreadLocal.withInitial(() -> 0);

    @Unique
    private static final ThreadLocal<ArrayDeque<Object[]>> gameoverse$frames = ThreadLocal.withInitial(ArrayDeque::new);

    @ModifyVariable(
        method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V",
        at = @At("HEAD"), argsOnly = true, index = 2)
    private Consumer<ItemStack> gameoverse$wrapConsumer(Consumer<ItemStack> original, LootContext ctx) {
        int depth = gameoverse$depth.get();
        gameoverse$depth.set(depth + 1);
        if (depth != 0) {
            return original;
        }
        ObjectArrayList<ItemStack> buffer = new ObjectArrayList<>();
        gameoverse$frames.get().push(new Object[] {buffer, original});
        return buffer::add;
    }

    @Inject(
        method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V",
        at = @At("RETURN"))
    private void gameoverse$flushAndRoll(LootContext ctx, Consumer<ItemStack> consumer, CallbackInfo ci) {
        int depth = gameoverse$depth.get() - 1;
        gameoverse$depth.set(depth);
        if (depth != 0) {
            return;
        }
        Object[] frame = gameoverse$frames.get().pop();
        @SuppressWarnings("unchecked")
        ObjectArrayList<ItemStack> buffer = (ObjectArrayList<ItemStack>) frame[0];
        @SuppressWarnings("unchecked")
        Consumer<ItemStack> original = (Consumer<ItemStack>) frame[1];

        buffer.forEach(original);

        ServerLevel level = ctx.getLevel();
        if (level.getRandom().nextFloat() < gameoverse$chanceFor(ctx)) {
            ItemStack crystal = HeartCrystalItems.create(level);
            if (!crystal.isEmpty()) {
                original.accept(crystal);
            }
        }
    }

    @Unique
    private static float gameoverse$chanceFor(LootContext ctx) {
        if (ctx.getOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER) instanceof ServerPlayer) {
            return KILL_CHANCE;
        }

        if (ctx.getOptionalParameter(LootContextParams.BLOCK_STATE) instanceof BlockState state) {
            if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                return MATURE_CROP_CHANCE;
            }

            var origin = ctx.getOptionalParameter(LootContextParams.ORIGIN);
            if (origin != null) {
                BlockPos pos = BlockPos.containing(origin);
                if (!PlacedBlocks.isPlaced(ctx.getLevel(), pos)) {
                    return GENERAL_BLOCK_CHANCE;
                }
            }
            return 0F;
        }

        if (ctx.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof ServerPlayer) {
            return CHEST_CHANCE;
        }

        return 0F;
    }
}
