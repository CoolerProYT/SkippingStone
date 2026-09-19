package com.coolerpromc.skippingstone.throwing.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PowerMeterTest {
    private static final double EPS = 1e-9;

    @Test
    void positionIsATriangleWave() {
        assertEquals(0.0, PowerMeter.position(0, 40), EPS);
        assertEquals(0.5, PowerMeter.position(10, 40), EPS);
        assertEquals(1.0, PowerMeter.position(20, 40), EPS);
        assertEquals(0.5, PowerMeter.position(30, 40), EPS);
        assertEquals(0.0, PowerMeter.position(40, 40), EPS);
        assertEquals(0.5, PowerMeter.position(50, 40), EPS);
    }

    @Test
    void positionSupportsPartialTicksAndClampsNegativeTime() {
        assertEquals(0.525, PowerMeter.position(10.5, 40), EPS);
        assertEquals(0.0, PowerMeter.position(-3, 40), EPS);
        assertThrows(IllegalArgumentException.class, () -> PowerMeter.position(1, 0));
    }

    @Test
    void zonesAreSymmetricRedYellowGreenYellowRed() {
        PowerMeter meter = new PowerMeter(0.2, 0.15);
        // green: [0.4, 0.6], yellow: [0.25, 0.4) and (0.6, 0.75], red elsewhere
        assertEquals(MeterZone.RED, meter.zoneAt(0.0));
        assertEquals(MeterZone.RED, meter.zoneAt(0.24));
        assertEquals(MeterZone.YELLOW, meter.zoneAt(0.26));
        assertEquals(MeterZone.GREEN, meter.zoneAt(0.41));
        assertEquals(MeterZone.GREEN, meter.zoneAt(0.5));
        assertEquals(MeterZone.GREEN, meter.zoneAt(0.59));
        assertEquals(MeterZone.YELLOW, meter.zoneAt(0.74));
        assertEquals(MeterZone.RED, meter.zoneAt(0.76));
        assertEquals(MeterZone.RED, meter.zoneAt(1.0));
    }

    @Test
    void sweepPassesThroughZonesInOrder() {
        PowerMeter meter = new PowerMeter(0.2, 0.15);
        MeterZone previous = null;
        StringBuilder sequence = new StringBuilder();
        for (int tick = 0; tick <= 20; tick++) {
            MeterZone zone = meter.zoneAt(PowerMeter.position(tick, 40));
            if (zone != previous) {
                sequence.append(zone.name().charAt(0));
                previous = zone;
            }
        }
        assertEquals("RYGYR", sequence.toString());
    }

    @Test
    void greenWidthNarrowsLinearlyWithTier() {
        assertEquals(0.30, PowerMeter.greenWidthForTier(0, 3, 0.30, 0.10), EPS);
        assertEquals(0.20, PowerMeter.greenWidthForTier(1, 3, 0.30, 0.10), EPS);
        assertEquals(0.10, PowerMeter.greenWidthForTier(2, 3, 0.30, 0.10), EPS);
        assertEquals(0.10, PowerMeter.greenWidthForTier(7, 3, 0.30, 0.10), EPS);
        assertEquals(0.30, PowerMeter.greenWidthForTier(0, 1, 0.30, 0.10), EPS);
    }

    @Test
    void bandsMustFitInTheBar() {
        assertThrows(IllegalArgumentException.class, () -> new PowerMeter(0.6, 0.3));
        assertThrows(IllegalArgumentException.class, () -> new PowerMeter(-0.1, 0.1));
        assertEquals(0.4, new PowerMeter(0.2, 0.15).greenStart(), EPS);
        assertEquals(0.25, new PowerMeter(0.2, 0.15).yellowStart(), EPS);
    }
}
