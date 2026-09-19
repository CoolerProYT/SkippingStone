package com.coolerpromc.skippingstone.network;

import com.coolerpromc.skippingstone.Constants;
import com.coolerpromc.skippingstone.platform.util.PayloadContext;
import com.coolerpromc.skippingstone.throwing.ThrowHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Client → server, sent right after the vanilla release-use packet.
 *
 * @param chargeTicks how long the client saw the meter charging, including the partial tick it last rendered.
 *                    The server checks this against its own tick count before trusting it.
 */
public record ThrowStonePayload(float chargeTicks) implements HandledCustomPacketPayload {
    public static final Type<ThrowStonePayload> TYPE = new Type<>(Constants.id("throw_stone"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ThrowStonePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ThrowStonePayload::chargeTicks,
            ThrowStonePayload::new
    );

    @Override
    public void handle(PayloadContext context) {
        context.execute(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ThrowHandler.onThrowPayload(player, this.chargeTicks);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
