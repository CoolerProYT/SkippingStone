package com.coolerpromc.skippingstone;

import com.coolerpromc.skippingstone.command.SkippingStoneCommand;
import com.coolerpromc.skippingstone.network.HandledCustomPacketPayload;
import com.coolerpromc.skippingstone.pickup.StonePickupHandler;
import com.coolerpromc.skippingstone.platform.Services;
import com.coolerpromc.skippingstone.platform.util.FabricServerPayloadContext;
import com.coolerpromc.skippingstone.stats.ModStats;
import com.coolerpromc.skippingstone.stats.RecordMirrors;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class SkippingStoneFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SkippingStone.init();
        SkippingStone.initPayloadType();
        ModStats.createStats();

        Services.REGISTRY.applyServerboundPayloadRegistrations(SkippingStoneFabric::registerServerboundPayload);

        UseBlockCallback.EVENT.register(StonePickupHandler::onUseBlock);
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> SkippingStoneCommand.register(dispatcher));
        ServerPlayerEvents.JOIN.register(RecordMirrors::onPlayerJoin);
        ServerLifecycleEvents.SERVER_STARTED.register(RecordMirrors::onServerStarted);
    }

    private static <T extends HandledCustomPacketPayload> void registerServerboundPayload(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        PayloadTypeRegistry.serverboundPlay().register(type, streamCodec);
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> payload.handle(new FabricServerPayloadContext(context)));
    }
}
