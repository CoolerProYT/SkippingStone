package com.coolerpromc.skippingstone;

import com.coolerpromc.skippingstone.block.ModBlocks;
import com.coolerpromc.skippingstone.component.ModDataComponents;
import com.coolerpromc.skippingstone.config.ModCommonConfig;
import com.coolerpromc.skippingstone.entity.ModEntities;
import com.coolerpromc.skippingstone.item.ModCreativeTabs;
import com.coolerpromc.skippingstone.item.ModItems;
import com.coolerpromc.skippingstone.network.HandledCustomPacketPayload;
import com.coolerpromc.skippingstone.particle.ModParticles;
import com.coolerpromc.skippingstone.network.ServerBoundThrowStonePayload;
import com.coolerpromc.skippingstone.platform.Services;
import com.coolerpromc.skippingstone.stats.ModStats;
import com.coolerpromc.skippingstone.stats.RecordMirrors;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class SkippingStone {
    public static void init() {
        ModCommonConfig.init();

        ModDataComponents.load();
        ModBlocks.load();
        ModItems.load();
        ModEntities.load();
        ModParticles.load();
        ModStats.load();

        RecordMirrors.init();
        ModCreativeTabs.load();
    }

    private static boolean payloadTypesCollected;

    public static void initPayloadType() {
        if (payloadTypesCollected) {
            return;
        }
        payloadTypesCollected = true;

        registerServerboundPayload(ServerBoundThrowStonePayload.TYPE, ServerBoundThrowStonePayload.STREAM_CODEC);
    }

    private static <T extends HandledCustomPacketPayload> void registerServerboundPayload(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        Services.REGISTRY.registerServerBoundPayload(type, streamCodec);
    }
}
