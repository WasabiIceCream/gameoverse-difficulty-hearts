package net.gameoverse.difficultyhearts;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks which block positions were placed by a player (as opposed to
 * naturally world-generated or grown), so the universal Heart Crystal loot
 * roll can exclude "place a cheap block, immediately break it" farming for
 * non-growing block types (see UniversalHeartDropMixin). Crop blocks don't
 * need this at all - their own age/maturity blockstate is already proof of
 * real elapsed growth time, regardless of who planted the seed.
 * <p>
 * Stored per-chunk via Fabric's Data Attachment API, keyed by the block's
 * full BlockPos.asLong() (redundant chunk info in the key, but trivial and
 * symmetric to read/write). Absence = assumed natural, so pre-existing
 * world terrain (anything placed before this mod existed) is correctly
 * treated as natural by default.
 */
public final class PlacedBlocks {
    public static final AttachmentType<LongSet> PLACED = AttachmentRegistry.createPersistent(
        Identifier.fromNamespaceAndPath("gameoverse", "placed_blocks"),
        Codec.LONG.listOf().xmap(LongOpenHashSet::new, PlacedBlocks::toList)
    );

    private PlacedBlocks() {
    }

    /**
     * Forces this class to load, and so PLACED to register, during mod
     * init. The registration lives in a static field, which Java only
     * initializes on first use of the class - that used to be the first
     * block place/break after boot, so any chunk loaded before then (e.g.
     * on a player's join) hit "unknown attachment type
     * gameoverse:placed_blocks" and silently dropped its saved placed-block
     * markers (seen on production 2026-09-25, 18 chunks in one join).
     */
    public static void init() {
    }

    private static List<Long> toList(LongSet set) {
        return new ArrayList<>(set);
    }

    public static void markPlaced(ServerLevel level, BlockPos pos) {
        ChunkAccess chunk = level.getChunk(pos);
        ((AttachmentTarget) chunk).getAttachedOrCreate(PLACED, LongOpenHashSet::new).add(pos.asLong());
    }

    public static void clear(ServerLevel level, BlockPos pos) {
        ChunkAccess chunk = level.getChunk(pos);
        LongSet set = ((AttachmentTarget) chunk).getAttached(PLACED);
        if (set != null) {
            set.remove(pos.asLong());
        }
    }

    public static boolean isPlaced(ServerLevel level, BlockPos pos) {
        ChunkAccess chunk = level.getChunk(pos);
        LongSet set = ((AttachmentTarget) chunk).getAttached(PLACED);
        return set != null && set.contains(pos.asLong());
    }
}
