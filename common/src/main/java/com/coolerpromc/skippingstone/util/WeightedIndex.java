package com.coolerpromc.skippingstone.util;

import java.util.List;

/** Weighted random index selection, kept free of Minecraft's RandomSource so it can be unit-tested. */
public final class WeightedIndex {
    private WeightedIndex() {
    }

    /**
     * @param weights non-negative weights; negative entries are treated as 0
     * @param roll    uniform random value in {@code [0, 1)}
     * @return the chosen index, or 0 when every weight is 0 (or the list is empty)
     */
    public static int pick(List<Integer> weights, double roll) {
        long total = 0;
        for (int weight : weights) {
            total += Math.max(weight, 0);
        }
        if (total <= 0) {
            return 0;
        }

        double target = roll * total;
        long cumulative = 0;
        for (int i = 0; i < weights.size(); i++) {
            cumulative += Math.max(weights.get(i), 0);
            if (target < cumulative) {
                return i;
            }
        }
        // roll was >= 1 (or rounding pushed it past the end): fall back to the last non-zero weight
        for (int i = weights.size() - 1; i >= 0; i--) {
            if (weights.get(i) > 0) {
                return i;
            }
        }
        return 0;
    }
}
