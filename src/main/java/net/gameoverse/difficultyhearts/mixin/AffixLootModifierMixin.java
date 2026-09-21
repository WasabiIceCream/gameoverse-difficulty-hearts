package net.gameoverse.difficultyhearts.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.shadowsoffire.apotheosis.loot.modifiers.AffixLootModifier;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.gameoverse.difficultyhearts.HeartsMath;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Boosts the chance Apotheosis rolls for an affix item to drop at all, on
 * both player kills and chest opens, scaled by hearts. Rarity/World Tier
 * resolution downstream of this roll is untouched - this only affects
 * *whether* an affix item drops, never what tier it's allowed to be, since
 * that stays gated by the player's own actually-earned World Tier.
 * <p>
 * Each heart-derived level adds 5 percentage points to the roll, capped at
 * 100%. Chosen to be a real, felt bonus without guaranteeing a drop outright
 * even at high hearts (Apotheosis's base chances are already fairly low
 * per-roll).
 * <p>
 * Which LootContext param carries the relevant player differs by context
 * (confirmed by decompiling vanilla's actual RandomizableContainer and
 * LootContext-building code for this exact version, not assumed): a mob
 * kill sets LAST_DAMAGE_PLAYER to the killer (THIS_ENTITY there is the dying
 * mob, not a player); a chest open sets THIS_ENTITY to the opening player
 * instead (RandomizableContainer#unpackLootTable). The two contexts never
 * overlap, so checking both in order is safe.
 */
@Mixin(AffixLootModifier.class)
public abstract class AffixLootModifierMixin {
    private static final float BONUS_PER_LEVEL = 0.05F;

    @ModifyExpressionValue(
        method = "doApply",
        at = @At(
            value = "INVOKE",
            target = "Ldev/shadowsoffire/apotheosis/loot/modifiers/AffixLootModifier$AffixTableEntry;chance()F"
        )
    )
    private float gameoverse$boostAffixChance(float original, ObjectArrayList<ItemStack> generatedLoot, LootContext ctx, GenContext gCtx) {
        ServerPlayer player = gameoverse$relevantPlayer(ctx);
        if (player == null) {
            return original;
        }

        double maxHealth = player.getAttributeValue(Attributes.MAX_HEALTH);
        int bonusLevel = HeartsMath.lootBonus(HeartsMath.heartsFromMaxHealth(maxHealth));
        if (bonusLevel <= 0) {
            return original;
        }

        return Math.min(1.0F, original + bonusLevel * BONUS_PER_LEVEL);
    }

    private static ServerPlayer gameoverse$relevantPlayer(LootContext ctx) {
        if (ctx.getOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER) instanceof ServerPlayer killer) {
            return killer;
        }
        if (ctx.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof ServerPlayer opener) {
            return opener;
        }
        return null;
    }
}
