package com.coolerpromc.skippingstone.item.custom;

import com.coolerpromc.skippingstone.component.ModDataComponents;
import com.coolerpromc.skippingstone.config.ModCommonConfig;
import com.coolerpromc.skippingstone.throwing.ThrowHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Held-use item: holding use charges the power meter, releasing throws. The meter itself is only drawn on the
 * client ({@code PowerMeterHud}); the server resolves the throw once the client's {@code ThrowStonePayload} arrives.
 */
public class SkippingStoneItem extends Item {
    private static final int MAX_USE_DURATION = 72000;

    /**
     * Client-only hook, installed by client init, so this class never references client classes and stays
     * safe to load on a dedicated server.
     */
    public static ReleaseListener clientReleaseListener = (player, stack, ticksUsed) -> {};

    public SkippingStoneItem(Properties properties) {
        super(properties);
    }

    public static int getTier(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.STONE_TIER.get(), 0);
    }

    public static ItemStack withTier(ItemStack stack, int tier) {
        stack.set(ModDataComponents.STONE_TIER.get(), tier);
        return stack;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return MAX_USE_DURATION;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        // TODO(animation): pick a wind-up pose once the thrown stone entity exists
        return ItemUseAnimation.NONE;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        int ticksUsed = this.getUseDuration(stack, entity) - timeLeft;
        if (level.isClientSide()) {
            clientReleaseListener.onRelease(player, stack, ticksUsed);
        } else if (player instanceof ServerPlayer serverPlayer) {
            ThrowHandler.recordRelease(serverPlayer, player.getUsedItemHand(), ticksUsed);
        }
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.skippingstone.stone_tier", ModCommonConfig.tier(getTier(stack)).displayName()).withStyle(ChatFormatting.GRAY));
    }

    @FunctionalInterface
    public interface ReleaseListener {
        void onRelease(Player player, ItemStack stack, int ticksUsed);
    }
}
