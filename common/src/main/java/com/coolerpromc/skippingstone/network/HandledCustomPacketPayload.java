package com.coolerpromc.skippingstone.network;

import com.coolerpromc.skippingstone.platform.util.PayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface HandledCustomPacketPayload extends CustomPacketPayload {
    void handle(PayloadContext context);
}
