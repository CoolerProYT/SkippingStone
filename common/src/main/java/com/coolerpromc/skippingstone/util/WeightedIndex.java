package com.coolerpromc.skippingstone.util;

import java.util.List;

public final class WeightedIndex {
    private WeightedIndex() {
    }

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
        for (int i = weights.size() - 1; i >= 0; i--) {
            if (weights.get(i) > 0) {
                return i;
            }
        }
        return 0;
    }
}
