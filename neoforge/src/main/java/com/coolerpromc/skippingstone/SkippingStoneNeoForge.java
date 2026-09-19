package com.coolerpromc.skippingstone;

import com.coolerpromc.skippingstone.command.SkippingStoneCommand;
import com.coolerpromc.skippingstone.gametest.SkippingStoneGameTests;
import com.coolerpromc.skippingstone.network.HandledCustomPacketPayload;
import com.coolerpromc.skippingstone.pickup.StonePickupHandler;
import com.coolerpromc.skippingstone.platform.NeoForgeRegistryHelper;
import com.coolerpromc.skippingstone.platform.Services;
import com.coolerpromc.skippingstone.platform.services.IRegistryHelper;
import com.coolerpromc.skippingstone.platform.util.NeoForgePayloadContext;
import com.coolerpromc.skippingstone.stats.ModStats;
import com.coolerpromc.skippingstone.stats.RecordMirrors;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Constants.MODID)
@EventBusSubscriber(modid = Constants.MODID)
public class SkippingStoneNeoForge {
    public SkippingStoneNeoForge(IEventBus eventBus) {
        SkippingStone.init();
        NeoForgeRegistryHelper.register(eventBus);
        SkippingStoneGameTests.register(eventBus);
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModStats::createStats);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        SkippingStoneCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RecordMirrors.onPlayerJoin(player);
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        RecordMirrors.onServerStarted(event.getServer());
    }

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        SkippingStone.initPayloadType();
        PayloadRegistrar registrar = event.registrar("1");
        Services.REGISTRY.applyServerboundPayloadRegistrations(new IRegistryHelper.ServerboundPayloadRegistrar() {
            @Override
            public <T extends HandledCustomPacketPayload> void register(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
                registrar.playToServer(type, streamCodec, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
            }
        });
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        InteractionResult result = StonePickupHandler.onUseBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }
}
