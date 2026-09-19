package com.coolerpromc.skippingstone.throwing.logic;

import java.util.List;

/**
 * Outcome of one throw.
 *
 * @param zone            meter zone at release
 * @param powerFactor     0 for red, otherwise the configured green / yellow factor
 * @param angleEfficiency 0..1, how close the release pitch was to ideal
 * @param baseVelocity    {@code powerFactor * qualityVelocityMultiplier(tier)}
 * @param skipDistances   distance of each counted skip, in blocks, longest first
 * @param totalDistance   sum of {@code skipDistances}
 */
public record ThrowResult(MeterZone zone, double powerFactor, double angleEfficiency, double baseVelocity, List<Double> skipDistances, double totalDistance) {
    public ThrowResult {
        skipDistances = List.copyOf(skipDistances);
    }

    public int skipCount() {
        return skipDistances.size();
    }

    /** True when the stone never skipped: a red release, or too little power / too poor an angle for one skip. */
    public boolean sank() {
        return skipDistances.isEmpty();
    }

    /** Red releases never count towards best-skip / best-distance stats. */
    public boolean awardsStats() {
        return zone != MeterZone.RED && !sank();
    }
}
