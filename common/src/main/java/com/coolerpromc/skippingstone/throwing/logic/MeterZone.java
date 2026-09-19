package com.coolerpromc.skippingstone.throwing.logic;

/**
 * The colored band of the power meter the indicator was in when the player released.
 */
public enum MeterZone {
    /** Outer band: the stone sinks immediately and no stats are awarded. */
    RED,
    /** Middle band: partial power. */
    YELLOW,
    /** Centre band: full power. Its width shrinks as the stone tier rises. */
    GREEN
}
