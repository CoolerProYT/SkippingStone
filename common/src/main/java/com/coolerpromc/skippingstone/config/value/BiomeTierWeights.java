package com.coolerpromc.skippingstone.config.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

import java.util.List;

public record BiomeTierWeights(Identifier biomeTag, List<Integer> weights) {
    public static final Codec<BiomeTierWeights> CODEC = RecordCodecBuilder.create(i -> i.group(
        Identifier.CODEC.fieldOf("biomeTag").forGetter(BiomeTierWeights::biomeTag),
        ExtraCodecs.NON_NEGATIVE_INT.listOf().fieldOf("weights").forGetter(BiomeTierWeights::weights)
    ).apply(i, BiomeTierWeights::new));
}
