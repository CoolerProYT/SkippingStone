package com.coolerpromc.skippingstone.entity;

import com.coolerpromc.skippingstone.entity.custom.SkippingStoneEntity;
import com.coolerpromc.skippingstone.platform.Services;
import com.coolerpromc.skippingstone.platform.util.RegistryHandler;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    public static final RegistryHandler.Entities<SkippingStoneEntity> SKIPPING_STONE = Services.REGISTRY.registerEntity("skipping_stone", SkippingStoneEntity::new, MobCategory.MISC,
        b -> b.noLootTable().sized(0.25F, 0.25F).clientTrackingRange(8).updateInterval(2));

    public static void load() {
    }
}
