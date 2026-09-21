package net.gameoverse.difficultyhearts;

/**
 * Shared hearts -> level math, used by both the Dynamic Difficulty
 * PlayerLevelProvider and the Apotheosis World Tier sync, so the two systems
 * stay in lockstep (10 hearts is always "+1 tier of everything").
 */
public final class HeartsMath {
    private static final double BASELINE_HEARTS = 6.0;
    private static final double HEARTS_PER_LEVEL = 4.0;

    private HeartsMath() {
    }

    /** Current hearts from a live minecraft:max_health attribute value (in HP/half-hearts). */
    public static double heartsFromMaxHealth(double maxHealth) {
        return maxHealth / 2.0;
    }

    /** floor((hearts - 6) / 4): 6 hearts is level 0, 10 is +1, 4 is -1, uncapped either direction. */
    public static int level(double hearts) {
        return (int) Math.floor((hearts - BASELINE_HEARTS) / HEARTS_PER_LEVEL);
    }
}
