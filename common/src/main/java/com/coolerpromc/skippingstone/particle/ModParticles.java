package com.coolerpromc.skippingstone.particle;

import com.coolerpromc.skippingstone.platform.Services;
import com.coolerpromc.skippingstone.platform.util.RegistryHandler;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

public class ModParticles {
    /**
     * Expanding ring lying flat on the water where a stone touched it. The x velocity argument carries the ring's
     * strength (roughly its final radius in blocks), so send it with a count of 0.
     */
    public static final RegistryHandler<ParticleType<?>, SimpleParticleType> RIPPLE = Services.REGISTRY.registerParticleType("ripple", () -> new SimpleParticleType(false) {});

    public static void load() {
    }
}
