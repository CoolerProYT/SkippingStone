package com.coolerpromc.skippingstone.throwing.logic;

public final class HopPhysics {
    public static final double GRAVITY = 0.03;
    public static final double AIR_DRAG = 0.99;

    private static final double MIN_VERTICAL_SPEED = 0.12;
    private static final double MAX_VERTICAL_SPEED = 0.42;
    private static final double VERTICAL_SPEED_PER_BLOCK = 0.035;
    private static final int MAX_FLIGHT_TICKS = 400;

    private HopPhysics() {
    }

    public record Hop(double verticalSpeed, double horizontalSpeed, int flightTicks) {
    }

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
