package com.coolerpromc.skippingstone.component;

import com.coolerpromc.skippingstone.platform.Services;
import com.coolerpromc.skippingstone.platform.util.RegistryHandler;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;

public class ModDataComponents {
    /** Index into {@code stone.tiers}. Absent means tier 0. */
    public static final RegistryHandler.Components<Integer> STONE_TIER = Services.REGISTRY.registerDataComponent("stone_tier",
        builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static void load() {
    }
}
