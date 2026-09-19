package com.coolerpromc.skippingstone.throwing.logic;

import java.util.List;

public record ThrowResult(MeterZone zone, double powerFactor, double angleEfficiency, double baseVelocity, List<Double> skipDistances, double totalDistance) {
    public ThrowResult {
        skipDistances = List.copyOf(skipDistances);
    }

    public int skipCount() {
        return skipDistances.size();
    }

    public boolean sank() {
        return skipDistances.isEmpty();
    }

    public boolean awardsStats() {
        return zone != MeterZone.RED && !sank();
    }
}
