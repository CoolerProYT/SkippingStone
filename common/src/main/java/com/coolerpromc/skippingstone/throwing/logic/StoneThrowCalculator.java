package com.coolerpromc.skippingstone.throwing.logic;

import java.util.ArrayList;
import java.util.List;

public final class StoneThrowCalculator {
    static final int SAFETY_SKIP_LIMIT = 10_000;

    private final ThrowTuning tuning;

    public StoneThrowCalculator(ThrowTuning tuning) {
        this.tuning = tuning;
    }

    public ThrowResult calculate(MeterZone zone, int tier, double pitch) {
        double powerFactor = powerFactor(zone);
        double angleEfficiency = angleEfficiency(pitch);
        double baseVelocity = powerFactor * tuning.qualityVelocityMultiplier(tier);

        if (zone == MeterZone.RED) {
            return new ThrowResult(zone, powerFactor, angleEfficiency, baseVelocity, List.of(), 0);
        }

        double firstSkipDistance = baseVelocity * angleEfficiency * tuning.baseDistance();
        List<Double> skips = new ArrayList<>();
        double total = 0;
        double distance = firstSkipDistance;
        while (distance > tuning.minSkipThreshold() && skips.size() < SAFETY_SKIP_LIMIT) {
            skips.add(distance);
            total += distance;
            distance *= tuning.decayRate();
        }
        return new ThrowResult(zone, powerFactor, angleEfficiency, baseVelocity, skips, total);
    }

    public double angleEfficiency(double pitch) {
        double deviation = Math.abs(tuning.idealAngle() - pitch) / tuning.angleTolerance();
        return 1 - Math.clamp(deviation, 0, 1);
    }

    public double powerFactor(MeterZone zone) {
        return switch (zone) {
            case GREEN -> tuning.greenPowerFactor();
            case YELLOW -> tuning.yellowPowerFactor();
            case RED -> 0;
        };
    }
}
