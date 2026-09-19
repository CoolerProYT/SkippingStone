package com.coolerpromc.skippingstone.stats;

import com.coolerpromc.skippingstone.platform.Services;
import com.coolerpromc.skippingstone.platform.util.RegistryHandler;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public class ModStats {
    public static final RegistryHandler<Identifier, Identifier> BEST_SKIPS = Services.REGISTRY.registerStat("best_skips");
    public static final RegistryHandler<Identifier, Identifier> BEST_SKIP_DISTANCE = Services.REGISTRY.registerStat("best_skip_distance");

    public static void load() {
    }

    public static void createStats() {
        Stats.CUSTOM.get(BEST_SKIPS.get(), StatFormatter.DEFAULT);
        Stats.CUSTOM.get(BEST_SKIP_DISTANCE.get(), StatFormatter.DISTANCE);
    }
}
