package com.coolerpromc.skippingstone.throwing;

import com.coolerpromc.skippingstone.Constants;
import com.coolerpromc.skippingstone.config.ModCommonConfig;
import com.coolerpromc.skippingstone.entity.custom.SkippingStoneEntity;
import com.coolerpromc.skippingstone.item.custom.SkippingStoneItem;
import com.coolerpromc.skippingstone.throwing.logic.MeterZone;
import com.coolerpromc.skippingstone.throwing.logic.PowerMeter;
import com.coolerpromc.skippingstone.throwing.logic.StoneThrowCalculator;
import com.coolerpromc.skippingstone.throwing.logic.ThrowResult;
import com.coolerpromc.skippingstone.stats.SkipRecords;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server side of a throw. Vanilla sends RELEASE_USE_ITEM before the client's {@code ThrowStonePayload}, so
 * {@link #recordRelease} runs first and stores what the server observed; {@link #onThrowPayload} then pairs it
 * with the charge time the client displayed, resolves the throw and launches the stone.
 */
public class ThrowHandler {
    /** A payload arriving later than this after its release is ignored. */
    private static final int PENDING_TIMEOUT_TICKS = 40;
    private static final float MIN_LAUNCH_SPEED = 0.6F;
    private static final float MAX_LAUNCH_SPEED = 1.5F;
    private static final float LAUNCH_INACCURACY = 0.5F;

    private static final Map<UUID, PendingRelease> PENDING = new HashMap<>();

    private record PendingRelease(InteractionHand hand, int ticksUsed, long gameTime) {
    }

    public static void recordRelease(ServerPlayer player, InteractionHand hand, int ticksUsed) {
        PENDING.put(player.getUUID(), new PendingRelease(hand, ticksUsed, player.level().getGameTime()));
    }

    public static void onThrowPayload(ServerPlayer player, float clientChargeTicks) {
        PendingRelease pending = PENDING.remove(player.getUUID());
        if (pending == null || player.level().getGameTime() - pending.gameTime() > PENDING_TIMEOUT_TICKS) {
            return;
        }

        ItemStack stack = player.getItemInHand(pending.hand());
        if (!(stack.getItem() instanceof SkippingStoneItem)) {
            return;
        }

        // Trust the client's sub-tick timing (it is what the player saw) unless it strays too far from ours
        double chargeTicks = clientChargeTicks;
        if (!Float.isFinite(clientChargeTicks) || Math.abs(clientChargeTicks - pending.ticksUsed()) > ModCommonConfig.RELEASE_TOLERANCE_TICKS.get()) {
            Constants.LOGGER.debug("Rejected client charge {} for {} (server saw {} ticks)", clientChargeTicks, player.getName().getString(), pending.ticksUsed());
            chargeTicks = pending.ticksUsed();
        }

        int tier = SkippingStoneItem.getTier(stack);
        MeterZone zone = ModCommonConfig.meterForTier(tier).zoneAt(PowerMeter.position(chargeTicks, ModCommonConfig.METER_PERIOD_TICKS.get()));
        ThrowResult result = new StoneThrowCalculator(ModCommonConfig.throwTuning()).calculate(zone, tier, player.getXRot());

        ServerLevel level = player.level();
        // Stronger releases leave the hand faster; red still throws, it just sinks on first contact
        float launchSpeed = MIN_LAUNCH_SPEED + (MAX_LAUNCH_SPEED - MIN_LAUNCH_SPEED) * (float) Math.min(result.powerFactor(), 1.0);
        Projectile.spawnProjectileFromRotation((l, shooter, thrown) -> new SkippingStoneEntity(l, shooter, thrown, result), level, stack, player, 0.0F, launchSpeed, LAUNCH_INACCURACY);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5F, 0.9F + level.getRandom().nextFloat() * 0.3F);
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        stack.consume(1, player);
    }

    /**
     * Called by the stone entity once it sinks, lands or is removed. Records the throw and reports the result.
     *
     * @return the record update, or {@code null} when the throw does not count (red release or no skips)
     */
    public static SkipRecords.Update onThrowFinished(ServerPlayer player, int skips, double distance, boolean awardsStats) {
        String blocks = String.format("%.1f", distance);
        if (!awardsStats) {
            player.sendOverlayMessage(skips == 0
                ? Component.translatable("message.skippingstone.throw.sank")
                : Component.translatable("message.skippingstone.throw.result", skips, blocks));
            return null;
        }

        SkipRecords.Update update = SkipRecords.get(player.level().getServer()).submit(player, skips, distance);
        if (update.improved() && update.previous() != SkipRecords.PlayerRecord.NONE) {
            player.sendOverlayMessage(Component.translatable("message.skippingstone.throw.record", skips, blocks).withStyle(ChatFormatting.GOLD));
        } else {
            player.sendOverlayMessage(Component.translatable("message.skippingstone.throw.result", skips, blocks));
        }
        return update;
    }

    /** Personal best at the start of a throw, so the stone can tell mid-flight when it beats it. */
    public static SkipRecords.PlayerRecord currentRecord(ServerPlayer player) {
        return SkipRecords.get(player.level().getServer()).get(player.getUUID());
    }
}
