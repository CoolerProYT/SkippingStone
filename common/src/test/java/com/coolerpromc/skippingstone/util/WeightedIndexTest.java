package com.coolerpromc.skippingstone.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeightedIndexTest {
    @Test
    void picksByCumulativeWeight() {
        List<Integer> weights = List.of(6, 3, 1);
        assertEquals(0, WeightedIndex.pick(weights, 0.0));
        assertEquals(0, WeightedIndex.pick(weights, 0.59));
        assertEquals(1, WeightedIndex.pick(weights, 0.6));
        assertEquals(1, WeightedIndex.pick(weights, 0.89));
        assertEquals(2, WeightedIndex.pick(weights, 0.9));
        assertEquals(2, WeightedIndex.pick(weights, 0.999999));
    }

    @Test
    void zeroWeightsAreNeverPicked() {
        List<Integer> weights = List.of(0, 5, 0);
        for (double roll = 0; roll < 1; roll += 0.05) {
            assertEquals(1, WeightedIndex.pick(weights, roll));
        }
        assertEquals(1, WeightedIndex.pick(weights, 1.0));
    }

    @Test
    void degenerateInputsFallBackToZero() {
        assertEquals(0, WeightedIndex.pick(List.of(), 0.5));
        assertEquals(0, WeightedIndex.pick(List.of(0, 0), 0.5));
        assertEquals(0, WeightedIndex.pick(List.of(-3, -1), 0.5));
    }
}
