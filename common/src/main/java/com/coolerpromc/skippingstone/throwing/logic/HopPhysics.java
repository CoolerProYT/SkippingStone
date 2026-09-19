package com.coolerpromc.skippingstone.throwing.logic;

/**
 * Turns a skip distance from {@link StoneThrowCalculator} into the launch velocity that makes a thrown stone land
 * that far away. Mirrors vanilla {@code ThrowableProjectile} physics exactly (per tick: subtract gravity, multiply
 * by air drag, then move), so the entity lands where the formula says without any hardcoded positions.
 */
public final class HopPhysics {
    /** {@code ThrowableProjectile.getDefaultGravity()}. */
    public static final double GRAVITY = 0.03;
    /** {@code ThrowableProjectile} air inertia. */
    public static final double AIR_DRAG = 0.99;

    private static final double MIN_VERTICAL_SPEED = 0.12;
    private static final double MAX_VERTICAL_SPEED = 0.42;
    private static final double VERTICAL_SPEED_PER_BLOCK = 0.035;
    private static final int MAX_FLIGHT_TICKS = 400;

    private HopPhysics() {
    }

    /**
     * @param verticalSpeed   upward velocity to launch with, in blocks per tick
     * @param horizontalSpeed horizontal velocity to launch with, in blocks per tick
     * @param flightTicks     ticks until the stone is back below its launch height
     */
    public record Hop(double verticalSpeed, double horizontalSpeed, int flightTicks) {
    }

    /** Longer skips arc higher, short ones at the end of a run barely leave the water. */
    public static double verticalSpeedFor(double distance) {
        return Math.clamp(0.08 + VERTICAL_SPEED_PER_BLOCK * distance, MIN_VERTICAL_SPEED, MAX_VERTICAL_SPEED);
    }

    public static Hop plan(double distance) {
        double verticalSpeed = verticalSpeedFor(distance);
        double velocityY = verticalSpeed;
        double y = 0;
        double drag = 1;
        double horizontalTravelPerSpeed = 0;
        int ticks = 0;
        do {
            velocityY = (velocityY - GRAVITY) * AIR_DRAG;
            y += velocityY;
            drag *= AIR_DRAG;
            horizontalTravelPerSpeed += drag;
            ticks++;
        } while (y > 0 && ticks < MAX_FLIGHT_TICKS);
        return new Hop(verticalSpeed, distance / horizontalTravelPerSpeed, ticks);
    }

    /** Horizontal distance covered by {@code hop}, simulated the same way the entity moves. For tests and tuning. */
    public static double simulateDistance(Hop hop) {
        double velocityX = hop.horizontalSpeed();
        double x = 0;
        for (int i = 0; i < hop.flightTicks(); i++) {
            velocityX *= AIR_DRAG;
            x += velocityX;
        }
        return x;
    }
}
