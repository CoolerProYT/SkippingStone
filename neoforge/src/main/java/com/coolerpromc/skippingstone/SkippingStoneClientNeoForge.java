package com.coolerpromc.skippingstone;

import com.coolerpromc.skippingstone.client.hud.PowerMeterHud;
import com.coolerpromc.skippingstone.client.particle.RippleParticle;
import com.coolerpromc.skippingstone.client.renderer.SkippingStoneRenderer;
import com.coolerpromc.skippingstone.entity.ModEntities;
import com.coolerpromc.skippingstone.particle.ModParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@Mod(value = Constants.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public class SkippingStoneClientNeoForge {
    public SkippingStoneClientNeoForge(IEventBus eventBus) {
        SkippingStoneClient.init();
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SKIPPING_STONE.get(), SkippingStoneRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.RIPPLE.get(), RippleParticle.Provider::new);
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.wrapLayer(VanillaGuiLayers.CONTEXTUAL_INFO_BAR_BACKGROUND, vanilla -> (graphics, deltaTracker) -> PowerMeterHud.extractInfoBar(graphics, deltaTracker, vanilla::render));
        event.wrapLayer(VanillaGuiLayers.CONTEXTUAL_INFO_BAR, vanilla -> (graphics, deltaTracker) -> PowerMeterHud.extractHiddenWhileActive(graphics, deltaTracker, vanilla::render));
        event.wrapLayer(VanillaGuiLayers.EXPERIENCE_LEVEL, vanilla -> (graphics, deltaTracker) -> PowerMeterHud.extractHiddenWhileActive(graphics, deltaTracker, vanilla::render));
    }
}
