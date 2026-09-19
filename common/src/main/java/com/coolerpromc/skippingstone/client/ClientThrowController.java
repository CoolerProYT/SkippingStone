package com.coolerpromc.skippingstone.client;

import com.coolerpromc.skippingstone.client.hud.PowerMeterHud;
import com.coolerpromc.skippingstone.network.ThrowStonePayload;
import com.coolerpromc.skippingstone.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ClientThrowController {
    /** Installed as {@code SkippingStoneItem.clientReleaseListener}. */
    public static void onRelease(Player player, ItemStack stack, int ticksUsed) {
        if (player != Minecraft.getInstance().player) {
            return;
        }
        Services.NETWORK.sendToServer(new ThrowStonePayload(PowerMeterHud.consumeRenderedCharge(ticksUsed)));
    }
}
