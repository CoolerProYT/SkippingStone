package com.coolerpromc.skippingstone.particle;

import com.coolerpromc.skippingstone.platform.Services;
import com.coolerpromc.skippingstone.platform.util.RegistryHandler;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

public class ModParticles {
    public static final RegistryHandler<ParticleType<?>, SimpleParticleType> RIPPLE = Services.REGISTRY.registerParticleType("ripple", () -> new SimpleParticleType(false) {});

    public static void load() {
    }
}
