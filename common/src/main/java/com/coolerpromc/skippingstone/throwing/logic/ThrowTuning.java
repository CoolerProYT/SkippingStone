package com.coolerpromc.skippingstone.throwing.logic;

import java.util.List;

/**
 * Every tunable input to {@link StoneThrowCalculator}. In game this is built from the config; tests build it directly.
 *
 * @param idealAngle               release pitch that gives full angle efficiency, in Minecraft pitch degrees
 *                                 (negative = looking up)
 * @param angleTolerance           degrees away from {@code idealAngle} at which efficiency reaches zero
 * @param tierVelocityMultipliers  {@code qualityVelocityMultiplier(tier)}, indexed by tier
 * @param baseDistance             {@code BASE_DISTANCE_CONSTANT}, in blocks
 * @param decayRate                each skip travels this fraction of the previous one; must be in (0, 1)
 * @param minSkipThreshold         skips shorter than this (in blocks) are not counted and end the throw
 * @param greenPowerFactor         power factor for a release in the green zone
 * @param yellowPowerFactor        power factor for a release in a yellow zone
 */
public record ThrowTuning(
    double idealAngle,
    double angleTolerance,
    List<Double> tierVelocityMultipliers,
    double baseDistance,
    double decayRate,
    double minSkipThreshold,
    double greenPowerFactor,
    double yellowPowerFactor
) {
    public ThrowTuning {
        if (angleTolerance <= 0) {
            throw new IllegalArgumentException("angleTolerance must be positive: " + angleTolerance);
        }
        if (decayRate <= 0 || decayRate >= 1) {
            throw new IllegalArgumentException("decayRate must be in (0, 1), otherwise skips never end: " + decayRate);
        }
        if (minSkipThreshold <= 0) {
            throw new IllegalArgumentException("minSkipThreshold must be positive, otherwise skips never end: " + minSkipThreshold);
        }
        if (tierVelocityMultipliers.isEmpty()) {
            throw new IllegalArgumentException("At least one tier velocity multiplier is required");
        }
        tierVelocityMultipliers = List.copyOf(tierVelocityMultipliers);
    }

    public int tierCount() {
        return tierVelocityMultipliers.size();
    }

    /** Out-of-range tiers clamp to the nearest defined tier, so a shrunk config never crashes old stones. */
    public double qualityVelocityMultiplier(int tier) {
        return tierVelocityMultipliers.get(Math.clamp(tier, 0, tierCount() - 1));
    }
}
