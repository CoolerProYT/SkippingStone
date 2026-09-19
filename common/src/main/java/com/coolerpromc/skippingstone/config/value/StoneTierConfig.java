package com.coolerpromc.skippingstone.config.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

public record StoneTierConfig(String name, double velocityMultiplier) {
    public static final Codec<StoneTierConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
        ExtraCodecs.NON_EMPTY_STRING.fieldOf("name").forGetter(StoneTierConfig::name),
        Codec.doubleRange(0, 100).fieldOf("velocityMultiplier").forGetter(StoneTierConfig::velocityMultiplier)
    ).apply(i, StoneTierConfig::new));

    public Component displayName() {
        return Component.translatableWithFallback("stone_tier.skippingstone." + name, name);
    }
}
