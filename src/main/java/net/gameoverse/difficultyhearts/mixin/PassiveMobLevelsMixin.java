package net.gameoverse.difficultyhearts.mixin;

import dev.muon.dynamic_difficulty.config.Configs;
import dev.muon.dynamic_difficulty.util.LevelingUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Dynamic Difficulty's "cancel levels for passives" option (1.3.3, and 1.1.1 on 1.21.1 before
 * it) only covers {@link Animal}s with no attack damage, so fish, squid, bats, villagers,
 * wandering traders, allays and the like still got levels. With the option on, this treats
 * any non-hostile mob with no attack damage the same way. Mobs that can fight back (wolves,
 * bees, polar bears, golems, goats, dolphins: all have attack damage) keep their levels, and
 * Dynamic Difficulty's own {@code #dynamic_difficulty:passive_whitelist} tag still opts a mob
 * back in. Mobs that never attack but still carry an attack-damage attribute (the allay) are
 * listed in {@code #gameoverse_difficulty_hearts:peaceful}.
 */
@Mixin(value = LevelingUtils.class, remap = false)
public abstract class PassiveMobLevelsMixin {
    @Shadow @Final private static TagKey<EntityType<?>> PASSIVE_WHITELIST;

    private static final TagKey<EntityType<?>> GAMEOVERSE$PEACEFUL = TagKey.create(Registries.ENTITY_TYPE,
        Identifier.fromNamespaceAndPath("gameoverse_difficulty_hearts", "peaceful"));

    @Inject(method = "canHaveLevel", at = @At("HEAD"), cancellable = true)
    private static void gameoverse$peacefulMobsHaveNoLevel(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof Mob mob) || entity instanceof Enemy || entity instanceof Animal) {
            return;
        }
        if (!Boolean.TRUE.equals(Configs.SYNC.cancelLevelsForPassives.get())) {
            return;
        }
        if (entity.getType().builtInRegistryHolder().is(PASSIVE_WHITELIST)) {
            return;
        }
        AttributeInstance attack = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (entity.getType().builtInRegistryHolder().is(GAMEOVERSE$PEACEFUL)
                || attack == null || attack.getValue() <= 0) {
            cir.setReturnValue(false);
        }
    }
}
