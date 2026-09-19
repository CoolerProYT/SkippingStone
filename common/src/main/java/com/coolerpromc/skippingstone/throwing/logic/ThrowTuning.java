package com.coolerpromc.skippingstone.throwing.logic;

import java.util.List;

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

    public double qualityVelocityMultiplier(int tier) {
        return tierVelocityMultipliers.get(Math.clamp(tier, 0, tierCount() - 1));
    }
}
