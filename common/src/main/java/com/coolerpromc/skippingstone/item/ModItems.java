package com.coolerpromc.skippingstone.item;

import com.coolerpromc.skippingstone.item.custom.SkippingStoneItem;
import com.coolerpromc.skippingstone.platform.Services;
import com.coolerpromc.skippingstone.platform.util.RegistryHandler;

public class ModItems {
    public static final RegistryHandler.Items<SkippingStoneItem> SKIPPING_STONE = Services.REGISTRY.registerItem("skipping_stone", p -> new SkippingStoneItem(p.stacksTo(16)));

    public static void load() {
    }
}
