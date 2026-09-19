package com.coolerpromc.skippingstone;

import com.coolerpromc.skippingstone.client.hud.PowerMeterHud;
import com.coolerpromc.skippingstone.client.particle.RippleParticle;
import com.coolerpromc.skippingstone.client.renderer.SkippingStoneRenderer;
import com.coolerpromc.skippingstone.entity.ModEntities;
import com.coolerpromc.skippingstone.particle.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public class SkippingStoneClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SkippingStoneClient.init();

        EntityRendererRegistry.register(ModEntities.SKIPPING_STONE.get(), SkippingStoneRenderer::new);
        ParticleProviderRegistry.getInstance().register(ModParticles.RIPPLE.get(), RippleParticle.Provider::new);

        HudElementRegistry.replaceElement(VanillaHudElements.INFO_BAR, vanilla -> (graphics, deltaTracker) -> PowerMeterHud.extractInfoBar(graphics, deltaTracker, vanilla::extractRenderState));
        HudElementRegistry.replaceElement(VanillaHudElements.EXPERIENCE_LEVEL, vanilla -> (graphics, deltaTracker) -> PowerMeterHud.extractHiddenWhileActive(graphics, deltaTracker, vanilla::extractRenderState));
    }
}
