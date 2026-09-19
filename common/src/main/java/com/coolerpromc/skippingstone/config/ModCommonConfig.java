package com.coolerpromc.skippingstone.config;

import com.coolerpromc.coolerconfig.config.ConfigBuilder;
import com.coolerpromc.coolerconfig.config.ConfigFormat;
import com.coolerpromc.coolerconfig.config.ConfigSide;
import com.coolerpromc.coolerconfig.config.ConfigSpec;
import com.coolerpromc.coolerconfig.config.ConfigValue;
import com.coolerpromc.skippingstone.Constants;
import com.coolerpromc.skippingstone.config.value.BiomeTierWeights;
import com.coolerpromc.skippingstone.config.value.StoneTierConfig;
import com.coolerpromc.skippingstone.throwing.logic.PowerMeter;
import com.coolerpromc.skippingstone.throwing.logic.ThrowTuning;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ModCommonConfig {
    public static ConfigSpec CONFIG;

    // stone
    public static ConfigValue<List<StoneTierConfig>> TIERS;

    // pickup
    public static ConfigValue<Map<String, String>> BLOCK_CONVERSIONS;
    public static ConfigValue<List<Integer>> DEFAULT_TIER_WEIGHTS;
    public static ConfigValue<List<BiomeTierWeights>> BIOME_TIER_WEIGHTS;

    // meter
    public static ConfigValue<Integer> METER_PERIOD_TICKS;
    public static ConfigValue<Double> GREEN_WIDTH_LOWEST_TIER;
    public static ConfigValue<Double> GREEN_WIDTH_HIGHEST_TIER;
    public static ConfigValue<Double> YELLOW_WIDTH;
    public static ConfigValue<Double> GREEN_POWER_FACTOR;
    public static ConfigValue<Double> YELLOW_POWER_FACTOR;
    public static ConfigValue<Integer> RELEASE_TOLERANCE_TICKS;

    // throw formula
    public static ConfigValue<Double> IDEAL_ANGLE;
    public static ConfigValue<Double> ANGLE_TOLERANCE;
    public static ConfigValue<Double> BASE_DISTANCE;
    public static ConfigValue<Double> DECAY_RATE;
    public static ConfigValue<Double> MIN_SKIP_THRESHOLD;

    // records
    public static ConfigValue<Boolean> SCOREBOARD_OBJECTIVES;

    private static final List<StoneTierConfig> DEFAULT_TIERS = List.of(new StoneTierConfig("chipped", 0.7), new StoneTierConfig("rough", 0.9), new StoneTierConfig("smooth", 1.1), new StoneTierConfig("perfect", 1.35));

    public static void init() {
        ConfigBuilder builder = ConfigSpec.builder(Constants.MODID, ConfigFormat.JSON5).side(ConfigSide.COMMON).comment("Skipping Stone configuration.");

        TIERS = builder.defineCodec("stone.tiers", ExtraCodecs.nonEmptyList(StoneTierConfig.CODEC.listOf()), DEFAULT_TIERS, """
            Stone quality tiers, lowest first. The number of entries is the number of tiers.
            name: translation key suffix, shown as stone_tier.skippingstone.<name>, falling back to the raw name.
            velocityMultiplier: qualityVelocityMultiplier(tier) in the throw formula.""");

        BLOCK_CONVERSIONS = builder.defineCodec("pickup.blockConversions", Codec.unboundedMap(Codec.STRING, Codec.STRING), Map.of("minecraft:gravel", "skippingstone:sifted_gravel", "minecraft:sand", "skippingstone:sifted_sand", "minecraft:red_sand", "skippingstone:sifted_red_sand"), "Blocks that yield a stone when right-clicked with an empty hand at the water's edge (water beside it, not on top of it), mapped to the block they turn into.");
        DEFAULT_TIER_WEIGHTS = builder.defineCodec("pickup.defaultTierWeights", ExtraCodecs.NON_NEGATIVE_INT.listOf(), List.of(50, 32, 14, 4), "Tier roll weights (one per tier, lowest first) used when no biomeTierWeights entry matches.");
        BIOME_TIER_WEIGHTS = builder.defineCodec("pickup.biomeTierWeights", BiomeTierWeights.CODEC.listOf(), List.of(new BiomeTierWeights(Identifier.withDefaultNamespace("is_beach"), List.of(30, 35, 25, 10)), new BiomeTierWeights(Identifier.withDefaultNamespace("is_river"), List.of(35, 35, 22, 8)), new BiomeTierWeights(Identifier.withDefaultNamespace("is_ocean"), List.of(40, 34, 20, 6))), "Per-biome-tag tier roll weights. The first entry whose tag contains the block's biome wins.");

        METER_PERIOD_TICKS = builder.defineInt("meter.periodTicks", 50, 4, 1200, "Ticks for the indicator to sweep across the bar and back.");
        GREEN_WIDTH_LOWEST_TIER = builder.defineDouble("meter.greenWidthLowestTier", 0.30, 0.0, 1.0, "Green zone width for the lowest tier, as a fraction of the bar. Narrows linearly to greenWidthHighestTier.");
        GREEN_WIDTH_HIGHEST_TIER = builder.defineDouble("meter.greenWidthHighestTier", 0.08, 0.0, 1.0, "Green zone width for the highest tier, as a fraction of the bar.");
        YELLOW_WIDTH = builder.defineDouble("meter.yellowWidth", 0.15, 0.0, 0.5, "Width of each yellow zone (one on either side of green), as a fraction of the bar. Shrunk automatically if the bands would not fit.");
        GREEN_POWER_FACTOR = builder.defineDouble("meter.greenPowerFactor", 1.0, 0.0, 10.0, "Power factor for a release in the green zone.");
        YELLOW_POWER_FACTOR = builder.defineDouble("meter.yellowPowerFactor", 0.55, 0.0, 10.0, "Power factor for a release in a yellow zone.");
        RELEASE_TOLERANCE_TICKS = builder.defineInt("meter.releaseToleranceTicks", 4, 0, 100, "How far (in ticks) the client's reported charge time may differ from the server's before the server uses its own.");

        IDEAL_ANGLE = builder.defineDouble("throw.idealAngle", 10.0, -90.0, 90.0, "Release pitch with full angle efficiency. Minecraft pitch: negative looks up, positive looks down. Slightly downward keeps the throw flat; aiming up lobs the stone so it hits the water too steeply.");
        ANGLE_TOLERANCE = builder.defineDouble("throw.angleTolerance", 25.0, 0.1, 180.0, "Degrees away from idealAngle at which angle efficiency reaches 0.");
        BASE_DISTANCE = builder.defineDouble("throw.baseDistance", 6.0, 0.0, 1000.0, "BASE_DISTANCE_CONSTANT: first skip distance, in blocks, for power 1, velocity multiplier 1 and a perfect angle.");
        DECAY_RATE = builder.defineDouble("throw.decayRate", 0.8, 0.01, 0.99, "Each skip travels this fraction of the previous one.");
        MIN_SKIP_THRESHOLD = builder.defineDouble("throw.minSkipThreshold", 0.5, 0.001, 1000.0, "Skips shorter than this many blocks are not counted and end the throw.");

        SCOREBOARD_OBJECTIVES = builder.defineBoolean("records.scoreboardObjectives", true, "Keep scoreboard objectives 'skippingstone.best_skips' and 'skippingstone.best_distance' (whole blocks) updated with every player's best. Show one with e.g. /scoreboard objectives setdisplay sidebar skippingstone.best_skips");

        CONFIG = builder.build();
    }

    public static List<StoneTierConfig> tiers() {
        return TIERS.get();
    }

    public static int tierCount() {
        return tiers().size();
    }

    public static StoneTierConfig tier(int index) {
        List<StoneTierConfig> tiers = tiers();
        return tiers.get(Math.clamp(index, 0, tiers.size() - 1));
    }

    public static Optional<Block> convertedBlock(Block source) {
        String target = BLOCK_CONVERSIONS.get().get(BuiltInRegistries.BLOCK.getKey(source).toString());
        if (target == null) {
            return Optional.empty();
        }
        Identifier id = Identifier.tryParse(target);
        Optional<Block> block = id == null ? Optional.empty() : BuiltInRegistries.BLOCK.getOptional(id);
        if (block.isEmpty()) {
            Constants.LOGGER.warn("pickup.blockConversions maps to unknown block '{}'", target);
        }
        return block;
    }

    public static PowerMeter meterForTier(int tier) {
        double green = PowerMeter.greenWidthForTier(tier, tierCount(), GREEN_WIDTH_LOWEST_TIER.get(), GREEN_WIDTH_HIGHEST_TIER.get());
        double yellow = Math.min(YELLOW_WIDTH.get(), (1 - green) / 2);
        return new PowerMeter(green, yellow);
    }

    public static ThrowTuning throwTuning() {
        return new ThrowTuning(IDEAL_ANGLE.get(), ANGLE_TOLERANCE.get(), tiers().stream().map(StoneTierConfig::velocityMultiplier).toList(), BASE_DISTANCE.get(), DECAY_RATE.get(), MIN_SKIP_THRESHOLD.get(), GREEN_POWER_FACTOR.get(), YELLOW_POWER_FACTOR.get());
    }
}
