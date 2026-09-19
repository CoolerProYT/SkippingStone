package com.coolerpromc.skippingstone.pickup;

import com.coolerpromc.skippingstone.config.ModCommonConfig;
import com.coolerpromc.skippingstone.config.value.BiomeTierWeights;
import com.coolerpromc.skippingstone.item.ModItems;
import com.coolerpromc.skippingstone.item.custom.SkippingStoneItem;
import com.coolerpromc.skippingstone.util.WeightedIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Prediction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.Optional;

public class StonePickupHandler {
    public static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (hand != InteractionHand.MAIN_HAND || player.isSpectator() || !player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);
        Optional<Block> converted = ModCommonConfig.convertedBlock(state.getBlock());
        if (converted.isEmpty() || !isShoreline(level, pos)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stone = SkippingStoneItem.withTier(ModItems.SKIPPING_STONE.toStack(), rollTier(level.getBiome(pos), level.getRandom()));
        level.setBlock(pos, converted.get().defaultBlockState(), Block.UPDATE_ALL);
        level.playSound(null, pos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        player.getInventory().placeItemBackInInventory(stone, Prediction.SERVER_ONLY);
        return InteractionResult.SUCCESS_SERVER;
    }

    public static boolean isShoreline(Level level, BlockPos pos) {
        if (level.getFluidState(pos.above()).is(FluidTags.WATER)) {
            return false;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getFluidState(pos.relative(direction)).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    public static int rollTier(Holder<Biome> biome, RandomSource random) {
        return WeightedIndex.pick(weightsFor(biome), random.nextDouble());
    }

    private static List<Integer> weightsFor(Holder<Biome> biome) {
        List<Integer> weights = ModCommonConfig.DEFAULT_TIER_WEIGHTS.get();
        for (BiomeTierWeights entry : ModCommonConfig.BIOME_TIER_WEIGHTS.get()) {
            if (biome.is(TagKey.create(Registries.BIOME, entry.biomeTag()))) {
                weights = entry.weights();
                break;
            }
        }
        int tierCount = ModCommonConfig.tierCount();
        return weights.size() > tierCount ? weights.subList(0, tierCount) : weights;
    }
}
