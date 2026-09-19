package com.coolerpromc.skippingstone.item;

import com.coolerpromc.skippingstone.block.ModBlocks;
import com.coolerpromc.skippingstone.config.ModCommonConfig;
import com.coolerpromc.skippingstone.item.custom.SkippingStoneItem;
import com.coolerpromc.skippingstone.platform.Services;
import com.coolerpromc.skippingstone.platform.util.RegistryHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

public class ModCreativeTabs {
    public static final RegistryHandler<CreativeModeTab, CreativeModeTab> SKIPPING_STONE = Services.REGISTRY.registerCreativeTab("skipping_stone",
        ModItems.SKIPPING_STONE::toStack,
        Component.translatable("itemGroup.skippingstone"),
        (output, parameters) -> {
            for (int tier = 0; tier < ModCommonConfig.tierCount(); tier++) {
                output.accept(SkippingStoneItem.withTier(ModItems.SKIPPING_STONE.toStack(), tier));
            }
            ModBlocks.SIFTED_BLOCKS.forEach(output::accept);
        });

    public static void load() {
    }
}
