package net.gameoverse.difficultyhearts;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

/**
 * Builds a real Heart Crystal ItemStack for code-driven drops (the universal
 * rare-loot system), by resolving the same
 * gameoverse:hearts/heart_crystal_item loot table the datapack defines
 * (data/gameoverse/loot_table/hearts/heart_crystal_item.json) rather than
 * hand-reconstructing its item components in Java. Single source of truth:
 * that loot table's components are kept in sync with the crafting recipe's
 * own result components (gameoverse:heart_crystal recipe) by hand, since
 * both are just JSON, but at least nothing here duplicates them a third
 * time in Java code that could drift independently.
 */
public final class HeartCrystalItems {
    private static final ResourceKey<LootTable> TABLE = ResourceKey.create(
        Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("gameoverse", "hearts/heart_crystal_item"));

    private HeartCrystalItems() {
    }

    public static ItemStack create(ServerLevel level) {
        LootTable table = level.getServer().reloadableRegistries().getLootTable(TABLE);
        LootParams params = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
        ObjectArrayList<ItemStack> results = table.getRandomItems(params);
        return results.isEmpty() ? ItemStack.EMPTY : results.get(0);
    }
}
