package com.coolerpromc.skippingstone.throwing.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HopPhysicsTest {
    @Test
    void plannedHopLandsAtTheRequestedDistance() {
        for (double distance : new double[]{0.51, 1.0, 2.5, 6.0, 8.1, 20.0}) {
            HopPhysics.Hop hop = HopPhysics.plan(distance);
            assertEquals(distance, HopPhysics.simulateDistance(hop), 1e-9, "distance " + distance);
        }
    }

    @Test
    void longerHopsFlyHigherAndLonger() {
        HopPhysics.Hop shortHop = HopPhysics.plan(0.6);
        HopPhysics.Hop longHop = HopPhysics.plan(8.0);
        assertTrue(longHop.verticalSpeed() > shortHop.verticalSpeed());
        assertTrue(longHop.flightTicks() > shortHop.flightTicks());
    }

    @Test
    void verticalSpeedIsClamped() {
        assertEquals(0.12, HopPhysics.verticalSpeedFor(0), 1e-9);
        assertEquals(0.42, HopPhysics.verticalSpeedFor(100), 1e-9);
    }

    @Test
    void hopsStayPlausible() {
        // A perfect first skip should read as a quick flat hop, not a lob
        HopPhysics.Hop hop = HopPhysics.plan(8.1);
        assertTrue(hop.flightTicks() >= 10 && hop.flightTicks() <= 40, "ticks " + hop.flightTicks());
        assertTrue(hop.horizontalSpeed() < 1.0, "speed " + hop.horizontalSpeed());
    }
}
