package com.coolerpromc.skippingstone.throwing.logic;

/**
 * Pure geometry of the power meter. No Minecraft classes, so it is shared verbatim by the HUD
 * (what the player sees) and the server (what the throw resolves to).
 *
 * <p>The bar is laid out symmetrically around its centre:
 * <pre>
 *   0.0                         0.5                         1.0
 *   | RED | YELLOW |        GREEN        | YELLOW | RED |
 * </pre>
 * The indicator sweeps 0 → 1 → 0 on a loop (a triangle wave), so it passes
 * Red → Yellow → Green → Yellow → Red twice per period.
 *
 * @param greenWidth  total width of the green band, as a fraction of the bar
 * @param yellowWidth width of <b>each</b> yellow band, as a fraction of the bar
 */
public record PowerMeter(double greenWidth, double yellowWidth) {
    public PowerMeter {
        if (greenWidth < 0 || yellowWidth < 0 || greenWidth + 2 * yellowWidth > 1) {
            throw new IllegalArgumentException("Meter bands must be non-negative and fit in the bar: green=" + greenWidth + ", yellow=" + yellowWidth);
        }
    }

    /**
     * The green band narrows linearly from {@code lowestTierGreenWidth} (tier 0) to
     * {@code highestTierGreenWidth} (tier {@code tierCount - 1}).
     */
    public static double greenWidthForTier(int tier, int tierCount, double lowestTierGreenWidth, double highestTierGreenWidth) {
        if (tierCount <= 1) {
            return lowestTierGreenWidth;
        }
        double t = Math.clamp(tier, 0, tierCount - 1) / (double) (tierCount - 1);
        return lowestTierGreenWidth + (highestTierGreenWidth - lowestTierGreenWidth) * t;
    }

    /**
     * Indicator position for a given charge time.
     *
     * @param chargeTicks ticks since the player started holding use (may include a partial tick)
     * @param periodTicks ticks for one full 0 → 1 → 0 sweep
     * @return position in {@code [0, 1]}, starting at 0 (the left red edge)
     */
    public static double position(double chargeTicks, double periodTicks) {
        if (periodTicks <= 0) {
            throw new IllegalArgumentException("periodTicks must be positive: " + periodTicks);
        }
        double phase = Math.max(chargeTicks, 0) % periodTicks / periodTicks;
        return phase < 0.5 ? phase * 2 : 2 - phase * 2;
    }

    public MeterZone zoneAt(double position) {
        double fromCentre = Math.abs(position - 0.5);
        double greenHalf = greenWidth / 2;
        if (fromCentre <= greenHalf) {
            return MeterZone.GREEN;
        }
        if (fromCentre <= greenHalf + yellowWidth) {
            return MeterZone.YELLOW;
        }
        return MeterZone.RED;
    }

    /** Left edge of the green band, as a fraction of the bar. */
    public double greenStart() {
        return 0.5 - greenWidth / 2;
    }

    /** Left edge of the left yellow band, as a fraction of the bar. */
    public double yellowStart() {
        return greenStart() - yellowWidth;
    }
}
