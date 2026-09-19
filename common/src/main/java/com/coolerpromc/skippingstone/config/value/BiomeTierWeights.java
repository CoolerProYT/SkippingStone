package com.coolerpromc.skippingstone.config.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

import java.util.List;

/**
 * Tier roll weights for blocks inside biomes of a given tag.
 *
 * @param biomeTag biome tag id without the leading {@code #}, e.g. {@code minecraft:is_river}
 * @param weights  one weight per tier, in tier order; missing trailing entries count as 0
 */
public record BiomeTierWeights(Identifier biomeTag, List<Integer> weights) {
    public static final Codec<BiomeTierWeights> CODEC = RecordCodecBuilder.create(i -> i.group(
        Identifier.CODEC.fieldOf("biomeTag").forGetter(BiomeTierWeights::biomeTag),
        ExtraCodecs.NON_NEGATIVE_INT.listOf().fieldOf("weights").forGetter(BiomeTierWeights::weights)
    ).apply(i, BiomeTierWeights::new));
}
