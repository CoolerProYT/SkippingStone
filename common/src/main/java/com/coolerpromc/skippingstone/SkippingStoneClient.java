package com.coolerpromc.skippingstone;

import com.coolerpromc.skippingstone.client.ClientThrowController;
import com.coolerpromc.skippingstone.item.custom.SkippingStoneItem;

public class SkippingStoneClient {
    public static void init() {
        SkippingStoneItem.clientReleaseListener = ClientThrowController::onRelease;
    }
}
