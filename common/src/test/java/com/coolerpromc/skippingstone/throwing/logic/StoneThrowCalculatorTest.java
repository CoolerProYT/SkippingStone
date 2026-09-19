package com.coolerpromc.skippingstone.throwing.logic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StoneThrowCalculatorTest {
    private static final double EPS = 1e-9;

    private static ThrowTuning tuning(double decayRate, double minSkipThreshold) {
        return new ThrowTuning(-20, 40, List.of(1.0, 2.0), 10, decayRate, minSkipThreshold, 1.0, 0.55);
    }

    private static final StoneThrowCalculator CALC = new StoneThrowCalculator(tuning(0.5, 1.0));

    @Test
    void angleEfficiencyIsOneAtIdealAndFallsLinearly() {
        assertEquals(1.0, CALC.angleEfficiency(-20), EPS);
        assertEquals(0.5, CALC.angleEfficiency(0), EPS);
        assertEquals(0.5, CALC.angleEfficiency(-40), EPS);
    }

    @Test
    void angleEfficiencyClampsToZeroBeyondTolerance() {
        assertEquals(0.0, CALC.angleEfficiency(20), EPS);
        assertEquals(0.0, CALC.angleEfficiency(90), EPS);
        assertEquals(0.0, CALC.angleEfficiency(-90), EPS);
    }

    @Test
    void redZoneSinksWithNoSkipsAndNoStats() {
        ThrowResult result = CALC.calculate(MeterZone.RED, 1, -20);
        assertTrue(result.sank());
        assertFalse(result.awardsStats());
        assertEquals(0, result.skipCount());
        assertEquals(0, result.totalDistance(), EPS);
        assertEquals(0, result.powerFactor(), EPS);
    }

    @Test
    void greenAtIdealAngleProducesGeometricSkips() {
        // first = 1.0 * 1.0 * 1.0 * 10 = 10; then 5, 2.5, 1.25; 0.625 is below the threshold of 1
        ThrowResult result = CALC.calculate(MeterZone.GREEN, 0, -20);
        assertEquals(List.of(10.0, 5.0, 2.5, 1.25), result.skipDistances());
        assertEquals(4, result.skipCount());
        assertEquals(18.75, result.totalDistance(), EPS);
        assertTrue(result.awardsStats());
    }

    @Test
    void yellowUsesPartialPower() {
        ThrowResult result = CALC.calculate(MeterZone.YELLOW, 0, -20);
        assertEquals(0.55, result.powerFactor(), EPS);
        assertEquals(5.5, result.skipDistances().getFirst(), EPS);
    }

    @Test
    void higherTierMultipliesVelocity() {
        ThrowResult low = CALC.calculate(MeterZone.GREEN, 0, -20);
        ThrowResult high = CALC.calculate(MeterZone.GREEN, 1, -20);
        assertEquals(2.0 * low.baseVelocity(), high.baseVelocity(), EPS);
        assertEquals(20.0, high.skipDistances().getFirst(), EPS);
        assertTrue(high.skipCount() > low.skipCount());
    }

    @Test
    void outOfRangeTierClampsInsteadOfThrowing() {
        assertEquals(CALC.calculate(MeterZone.GREEN, 1, -20), CALC.calculate(MeterZone.GREEN, 99, -20));
        assertEquals(CALC.calculate(MeterZone.GREEN, 0, -20), CALC.calculate(MeterZone.GREEN, -5, -20));
    }

    @Test
    void terribleAngleSinksEvenOnGreen() {
        ThrowResult result = CALC.calculate(MeterZone.GREEN, 1, 60);
        assertTrue(result.sank());
        assertFalse(result.awardsStats());
    }

    @Test
    void firstSkipExactlyAtThresholdIsNotCounted() {
        StoneThrowCalculator calc = new StoneThrowCalculator(tuning(0.5, 10.0));
        assertTrue(calc.calculate(MeterZone.GREEN, 0, -20).sank());
    }

    @Test
    void skipCountMatchesClosedForm() {
        StoneThrowCalculator calc = new StoneThrowCalculator(tuning(0.78, 0.5));
        ThrowResult result = calc.calculate(MeterZone.GREEN, 1, -20);
        double first = result.skipDistances().getFirst();
        int expected = (int) Math.ceil(Math.log(0.5 / first) / Math.log(0.78));
        assertEquals(expected, result.skipCount());
        assertEquals(result.skipDistances().stream().mapToDouble(Double::doubleValue).sum(), result.totalDistance(), EPS);
    }

    @Test
    void tuningRejectsNonTerminatingConfigs() {
        assertThrows(IllegalArgumentException.class, () -> tuning(1.0, 0.5));
        assertThrows(IllegalArgumentException.class, () -> tuning(0.0, 0.5));
        assertThrows(IllegalArgumentException.class, () -> tuning(0.5, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new ThrowTuning(-20, 0, List.of(1.0), 10, 0.5, 1, 1, 0.55));
        assertThrows(IllegalArgumentException.class, () -> new ThrowTuning(-20, 40, List.of(), 10, 0.5, 1, 1, 0.55));
    }
}
