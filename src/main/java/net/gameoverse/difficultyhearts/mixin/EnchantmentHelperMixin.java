package net.gameoverse.difficultyhearts.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.gameoverse.difficultyhearts.HeartsMath;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * General "more hearts, more loot" bonus: boosts the attacking player's
 * effective Looting enchantment level (without touching their actual
 * weapon), which is what vanilla's EnchantedCountIncreaseFunction reads for
 * mob drop counts on essentially every standard loot table, vanilla or
 * modded. Same mechanism skill-proficiencies-fabric already uses for its
 * combat skill's passive looting bonus (see that project's
 * EnchantmentHelperMixin) - proven to work on this exact server/version.
 * <p>
 * Unlike the Dynamic Difficulty / mob-level side of this mod, this bonus is
 * never negative (see HeartsMath.lootBonus): low-heart casual players get an
 * easier world, not also worse loot.
 */
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @ModifyReturnValue(method = "getEnchantmentLevel", at = @At("RETURN"))
    private static int gameoverse$boostLooting(int original, Holder<Enchantment> enchantment, LivingEntity entity) {
        if (!enchantment.is(Enchantments.LOOTING) || !(entity instanceof ServerPlayer player)) {
            return original;
        }

        double maxHealth = player.getAttributeValue(Attributes.MAX_HEALTH);
        int bonus = HeartsMath.lootBonus(HeartsMath.heartsFromMaxHealth(maxHealth));
        return bonus > 0 ? original + bonus : original;
    }
}
