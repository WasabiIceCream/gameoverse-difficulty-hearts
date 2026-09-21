package net.gameoverse.difficultyhearts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Plain JSON config for the universal Heart Crystal drop rates, editable
 * without a rebuild - config/gameoverse_difficulty_hearts.json. Written
 * with defaults on first boot if missing; a restart is needed to pick up
 * edits (no live-reload/watcher, same as most of this server's other
 * config-driven mods).
 */
public final class HeartsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir()
        .resolve("gameoverse_difficulty_hearts.json");

    private static HeartsConfig instance;

    public float killChance = 0.005F;
    public float chestChance = 0.01F;
    public float matureCropChance = 0.002F;
    public float generalBlockChance = 0.0005F;

    public static synchronized HeartsConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static HeartsConfig load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                HeartsConfig loaded = GSON.fromJson(reader, HeartsConfig.class);
                if (loaded != null) {
                    return loaded;
                }
            } catch (IOException e) {
                // Fall through to defaults below - a malformed/unreadable
                // config shouldn't take the server down.
            }
        }

        HeartsConfig defaults = new HeartsConfig();
        defaults.save();
        return defaults;
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            // Non-fatal - the in-memory values (defaults or whatever was
            // already loaded) keep working either way.
        }
    }
}
