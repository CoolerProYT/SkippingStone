package com.coolerpromc.skippingstone.client.hud;

import com.coolerpromc.skippingstone.config.ModCommonConfig;
import com.coolerpromc.skippingstone.item.custom.SkippingStoneItem;
import com.coolerpromc.skippingstone.throwing.logic.PowerMeter;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Takes over the XP bar slot while a skipping stone is being charged. Loaders wrap the vanilla info-bar and
 * experience-level layers with {@link #extractInfoBar} / {@link #extractHiddenWhileActive}, so the real XP display
 * comes back automatically the moment the player stops using the stone.
 */
public class PowerMeterHud implements ContextualBarRenderer {
    private static final Identifier BAR_FRAME_SPRITE = Identifier.withDefaultNamespace("hud/experience_bar_background");
    private static final int RED = 0xFFD8403A;
    private static final int YELLOW = 0xFFE8C547;
    private static final int GREEN = 0xFF5ACF4A;
    private static final int MARKER = 0xFFFFFFFF;
    private static final int MARKER_OUTLINE = 0xFF000000;

    private static final PowerMeterHud INSTANCE = new PowerMeterHud();

    // What the player last saw, so the throw resolves against the rendered frame rather than the whole tick
    private static float lastRenderedCharge;
    private static int lastRenderedTicks = -1;

    @FunctionalInterface
    public interface HudLayer {
        void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker);
    }

    public static boolean isActive() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && player.isUsingItem() && player.getUseItem().getItem() instanceof SkippingStoneItem;
    }

    public static void extractInfoBar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, HudLayer vanilla) {
        if (isActive()) {
            INSTANCE.extractBackground(graphics, deltaTracker);
        } else {
            vanilla.extract(graphics, deltaTracker);
        }
    }

    public static void extractHiddenWhileActive(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, HudLayer vanilla) {
        if (!isActive()) {
            vanilla.extract(graphics, deltaTracker);
        }
    }

    /**
     * Charge time to report for a release. Uses the sub-tick value of the last rendered frame when it belongs to the
     * same tick as the release (the HUD may be hidden with F1, or skipped a frame), otherwise the whole-tick count.
     */
    public static float consumeRenderedCharge(int ticksUsed) {
        float charge = lastRenderedTicks == ticksUsed ? lastRenderedCharge : ticksUsed;
        lastRenderedTicks = -1;
        return charge;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        PowerMeter meter = ModCommonConfig.meterForTier(SkippingStoneItem.getTier(player.getUseItem()));
        float charge = player.getTicksUsingItem(deltaTracker.getGameTimeDeltaPartialTick(false));
        lastRenderedCharge = charge;
        lastRenderedTicks = player.getTicksUsingItem();

        int left = this.left(minecraft.getWindow());
        int top = this.top(minecraft.getWindow());
        int yellowStart = toPixels(meter.yellowStart());
        int greenStart = toPixels(meter.greenStart());

        // TODO(art): dedicated meter sprites; for now the vanilla XP bar frame with solid zone fills
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_FRAME_SPRITE, left, top, WIDTH, HEIGHT);
        fillZone(graphics, left, top, 0, yellowStart, RED);
        fillZone(graphics, left, top, yellowStart, greenStart, YELLOW);
        fillZone(graphics, left, top, greenStart, WIDTH - greenStart, GREEN);
        fillZone(graphics, left, top, WIDTH - greenStart, WIDTH - yellowStart, YELLOW);
        fillZone(graphics, left, top, WIDTH - yellowStart, WIDTH, RED);

        double position = PowerMeter.position(charge, ModCommonConfig.METER_PERIOD_TICKS.get());
        int markerX = left + (int) Math.round(position * (WIDTH - 1));
        graphics.fill(markerX - 1, top - 2, markerX + 2, top + HEIGHT + 2, MARKER_OUTLINE);
        graphics.fill(markerX, top - 1, markerX + 1, top + HEIGHT + 1, MARKER);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
    }

    private static int toPixels(double fraction) {
        return (int) Math.round(fraction * WIDTH);
    }

    private static void fillZone(GuiGraphicsExtractor graphics, int left, int top, int start, int end, int color) {
        // Inset by one pixel so the frame's border stays visible
        int from = Math.max(start, 1);
        int to = Math.min(end, WIDTH - 1);
        if (to > from) {
            graphics.fill(left + from, top + 1, left + to, top + HEIGHT - 1, color);
        }
    }
}
