package com.coolerpromc.skippingstone.throwing.logic;

public record PowerMeter(double greenWidth, double yellowWidth) {
    public PowerMeter {
        if (greenWidth < 0 || yellowWidth < 0 || greenWidth + 2 * yellowWidth > 1) {
            throw new IllegalArgumentException("Meter bands must be non-negative and fit in the bar: green=" + greenWidth + ", yellow=" + yellowWidth);
        }
    }

    public static double greenWidthForTier(int tier, int tierCount, double lowestTierGreenWidth, double highestTierGreenWidth) {
        if (tierCount <= 1) {
            return lowestTierGreenWidth;
        }
        double t = Math.clamp(tier, 0, tierCount - 1) / (double) (tierCount - 1);
        return lowestTierGreenWidth + (highestTierGreenWidth - lowestTierGreenWidth) * t;
    }

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

    public double greenStart() {
        return 0.5 - greenWidth / 2;
    }

    public double yellowStart() {
        return greenStart() - yellowWidth;
    }
}
