package com.coolerpromc.skippingstone.stats;

import com.coolerpromc.skippingstone.platform.Services;
import com.coolerpromc.skippingstone.platform.util.RegistryHandler;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

/**
 * Display-only copies of {@link SkipRecords} for the vanilla Statistics screen (General tab). {@link SkipRecords}
 * stays the source of truth; {@link RecordMirrors} overwrites these whenever a record changes or the player joins.
 */
public class ModStats {
    public static final RegistryHandler<Identifier, Identifier> BEST_SKIPS = Services.REGISTRY.registerStat("best_skips");
    /** In centimetres, like vanilla distance stats, so the screen shows "12.3 m" style values. */
    public static final RegistryHandler<Identifier, Identifier> BEST_SKIP_DISTANCE = Services.REGISTRY.registerStat("best_skip_distance");

    public static void load() {
    }

    /**
     * Creates the stat entries with their formatters. Must run once the custom stat registry holds our ids, and before
     * anything else looks the stats up, or they fall back to the plain number formatter and may not list on screen.
     */
    public static void createStats() {
        Stats.CUSTOM.get(BEST_SKIPS.get(), StatFormatter.DEFAULT);
        Stats.CUSTOM.get(BEST_SKIP_DISTANCE.get(), StatFormatter.DISTANCE);
    }
}
