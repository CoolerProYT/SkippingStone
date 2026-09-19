package com.coolerpromc.skippingstone.config.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

/**
 * One stone quality tier. Tiers are ordered: index 0 is the lowest (widest green zone), the last is the highest.
 *
 * @param name               key used for the display name ({@code stone_tier.skippingstone.<name>})
 * @param velocityMultiplier {@code qualityVelocityMultiplier(tier)} in the throw formula
 */
public record StoneTierConfig(String name, double velocityMultiplier) {
    public static final Codec<StoneTierConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
        ExtraCodecs.NON_EMPTY_STRING.fieldOf("name").forGetter(StoneTierConfig::name),
        Codec.doubleRange(0, 100).fieldOf("velocityMultiplier").forGetter(StoneTierConfig::velocityMultiplier)
    ).apply(i, StoneTierConfig::new));

    public Component displayName() {
        return Component.translatableWithFallback("stone_tier.skippingstone." + name, name);
    }
}
