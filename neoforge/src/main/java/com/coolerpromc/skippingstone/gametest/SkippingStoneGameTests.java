package com.coolerpromc.skippingstone.gametest;

import com.coolerpromc.skippingstone.Constants;
import com.coolerpromc.skippingstone.config.ModCommonConfig;
import com.coolerpromc.skippingstone.entity.custom.SkippingStoneEntity;
import com.coolerpromc.skippingstone.item.ModItems;
import com.coolerpromc.skippingstone.item.custom.SkippingStoneItem;
import com.coolerpromc.skippingstone.pickup.StonePickupHandler;
import com.coolerpromc.skippingstone.stats.ModStats;
import com.coolerpromc.skippingstone.stats.RecordMirrors;
import com.coolerpromc.skippingstone.stats.SkipRecords;
import com.coolerpromc.skippingstone.throwing.logic.MeterZone;
import com.coolerpromc.skippingstone.throwing.logic.StoneThrowCalculator;
import com.coolerpromc.skippingstone.throwing.logic.ThrowResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * In-world checks that a thrown stone really plays out its calculated skips and that the record firework only fires
 * when a player beats a record they already had. Only loaded when game tests are enabled for this namespace (the dev
 * runs set {@code neoforge.enabledGameTestNamespaces}); run with {@code runGameTestServer}.
 */
public class SkippingStoneGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, Constants.MODID);
    private static final Map<String, Consumer<GameTestHelper>> TESTS = new LinkedHashMap<>();

    private static final int POOL_LENGTH = 56;
    private static final int POOL_WIDTH = 5;
    private static final double DISTANCE_TOLERANCE = 0.75;
    private static final int PERFECT_TIER = 3;

    static {
        TESTS.put("perfect_green_throw_skips_as_calculated", helper -> {
            ThrowResult result = result(MeterZone.GREEN);
            SkippingStoneEntity stone = throwStone(helper, result, null);
            helper.succeedWhen(() -> {
                assertSunk(helper, stone);
                helper.assertValueEqual(stone.getSkipsPerformed(), result.skipCount(), Component.literal("skip count"));
                double drift = Math.abs(stone.getDistanceTravelled() - result.totalDistance());
                helper.assertTrue(drift <= DISTANCE_TOLERANCE, Component.literal("distance travelled " + stone.getDistanceTravelled() + " vs calculated " + result.totalDistance()));
            });
        });
        TESTS.put("red_throw_sinks", helper -> {
            SkippingStoneEntity stone = throwStone(helper, result(MeterZone.RED), null);
            helper.succeedWhen(() -> {
                assertSunk(helper, stone);
                helper.assertValueEqual(stone.getSkipsPerformed(), 0, Component.literal("skip count"));
            });
        });
        TESTS.put("first_throw_sets_record_without_firework", helper -> {
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            ThrowResult result = result(MeterZone.GREEN);
            SkippingStoneEntity stone = throwStone(helper, result, player);
            helper.succeedWhen(() -> {
                assertSunk(helper, stone);
                helper.assertFalse(stone.hasLaunchedRecordFirework(), Component.literal("firework launched without an earlier record"));
                SkipRecords.PlayerRecord saved = SkipRecords.get(helper.getLevel().getServer()).get(player.getUUID());
                helper.assertValueEqual(saved.bestSkips(), result.skipCount(), Component.literal("saved best skips"));
            });
        });
        TESTS.put("beating_record_launches_firework", helper -> {
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            SkipRecords.get(helper.getLevel().getServer()).submit(player, 3, 5.0);
            SkippingStoneEntity stone = throwStone(helper, result(MeterZone.GREEN), player);
            helper.succeedWhen(() -> {
                assertSunk(helper, stone);
                helper.assertTrue(stone.hasLaunchedRecordFirework(), Component.literal("no firework for a new record"));
            });
        });
        TESTS.put("unbeaten_record_launches_no_firework", helper -> {
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            SkipRecords.get(helper.getLevel().getServer()).submit(player, 500, 5000.0);
            SkippingStoneEntity stone = throwStone(helper, result(MeterZone.GREEN), player);
            helper.succeedWhen(() -> {
                assertSunk(helper, stone);
                helper.assertFalse(stone.hasLaunchedRecordFirework(), Component.literal("firework without beating the record"));
            });
        });
        TESTS.put("records_show_in_stats_scoreboard_and_command", helper -> {
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            var server = helper.getLevel().getServer();
            SkipRecords.get(server).submit(player, 11, 23.456);

            var bestSkips = Stats.CUSTOM.get(ModStats.BEST_SKIPS.get());
            var bestDistance = Stats.CUSTOM.get(ModStats.BEST_SKIP_DISTANCE.get());
            helper.assertValueEqual(player.getStats().getValue(bestSkips), 11, Component.literal("best skips stat"));
            helper.assertValueEqual(player.getStats().getValue(bestDistance), 2346, Component.literal("best distance stat (cm)"));
            helper.assertTrue(!bestDistance.format(2346).equals("2346"), Component.literal("distance stat is not using the distance formatter: " + bestDistance.format(2346)));

            var scoreboard = server.getScoreboard();
            for (String name : new String[]{RecordMirrors.SKIPS_OBJECTIVE, RecordMirrors.DISTANCE_OBJECTIVE}) {
                helper.assertTrue(scoreboard.getObjective(name) != null, Component.literal("missing objective " + name));
            }
            ScoreHolder holder = ScoreHolder.forNameOnly(player.getScoreboardName());
            Objective skipsObjective = scoreboard.getObjective(RecordMirrors.SKIPS_OBJECTIVE);
            Objective distanceObjective = scoreboard.getObjective(RecordMirrors.DISTANCE_OBJECTIVE);
            helper.assertValueEqual(scoreboard.getOrCreatePlayerScore(holder, skipsObjective).get(), 11, Component.literal("skips score"));
            helper.assertValueEqual(scoreboard.getOrCreatePlayerScore(holder, distanceObjective).get(), 23, Component.literal("distance score"));

            var dispatcher = server.getCommands().getDispatcher();
            var source = player.createCommandSourceStack();
            try {
                helper.assertValueEqual(dispatcher.execute("skippingstone records", source), 11, Component.literal("/skippingstone records result"));
                helper.assertValueEqual(dispatcher.execute("skippingstone records " + player.getScoreboardName(), source), 1, Component.literal("/skippingstone records <player> result"));
                helper.assertTrue(dispatcher.execute("skippingstone records top distance", source) >= 1, Component.literal("leaderboard is empty"));
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
                helper.fail(Component.literal("command failed: " + e.getMessage()));
            }
            helper.succeed();
        });
        TESTS.put("stones_only_come_from_shoreline_sand_and_gravel", helper -> {
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            // gravel with water beside it: yields a stone
            assertPickup(helper, player, new BlockPos(1, 1, 1), Blocks.GRAVEL, true, false, true);
            assertPickup(helper, player, new BlockPos(5, 1, 1), Blocks.SAND, true, false, true);
            // submerged: water on top, even with water beside it
            assertPickup(helper, player, new BlockPos(9, 1, 1), Blocks.GRAVEL, true, true, false);
            // dry: no water beside it
            assertPickup(helper, player, new BlockPos(13, 1, 1), Blocks.SAND, false, false, false);
            // dirt no longer counts
            assertPickup(helper, player, new BlockPos(17, 1, 1), Blocks.DIRT, true, false, false);
            helper.succeed();
        });
        TESTS.forEach((name, test) -> TEST_FUNCTIONS.register(name, () -> test));
    }

    public static void register(IEventBus modBus) {
        TEST_FUNCTIONS.register(modBus);
        modBus.addListener(SkippingStoneGameTests::onRegisterGameTests);
    }

    private static void onRegisterGameTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(Constants.id("default"));
        for (String name : TESTS.keySet()) {
            ResourceKey<Consumer<GameTestHelper>> function = ResourceKey.create(Registries.TEST_FUNCTION, Constants.id(name));
            event.registerTest(Constants.id(name), new FunctionGameTestInstance(function, new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 400, 0, true)));
        }
    }

    private static void assertPickup(GameTestHelper helper, ServerPlayer player, BlockPos pos, Block block, boolean waterBeside, boolean waterOnTop, boolean expectStone) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                helper.setBlock(pos.offset(x, -1, z), Blocks.STONE);
                helper.setBlock(pos.offset(x, 0, z), Blocks.STONE);
                helper.setBlock(pos.offset(x, 1, z), Blocks.AIR);
            }
        }
        helper.setBlock(pos, block);
        if (waterBeside) {
            helper.setBlock(pos.east(), Blocks.WATER);
        }
        if (waterOnTop) {
            helper.setBlock(pos.above(), Blocks.WATER);
        }

        player.getInventory().clearContent();
        BlockPos absolute = helper.absolutePos(pos);
        InteractionResult result = StonePickupHandler.onUseBlock(player, helper.getLevel(), InteractionHand.MAIN_HAND,
            new BlockHitResult(absolute.getCenter(), Direction.UP, absolute, false));
        boolean gotStone = player.getInventory().contains(stack -> stack.is(ModItems.SKIPPING_STONE.get()));
        String label = block + (waterBeside ? " +water beside" : "") + (waterOnTop ? " +water on top" : "");
        helper.assertValueEqual(gotStone, expectStone, Component.literal("stone from " + label));
        helper.assertValueEqual(result != InteractionResult.PASS, expectStone, Component.literal("interaction handled for " + label));
        helper.assertValueEqual(helper.getBlockState(pos).getBlock() != block, expectStone, Component.literal("block converted for " + label));
    }

    private static ThrowResult result(MeterZone zone) {
        return new StoneThrowCalculator(ModCommonConfig.throwTuning()).calculate(zone, PERFECT_TIER, ModCommonConfig.IDEAL_ANGLE.get());
    }

    private static void assertSunk(GameTestHelper helper, SkippingStoneEntity stone) {
        helper.assertTrue(stone.isSinking(), Component.literal("stone has not sunk yet (removed=" + stone.isRemoved() + ", skips=" + stone.getSkipsPerformed()
            + ", pos=" + helper.relativeVec(stone.position()) + ", inWater=" + stone.isInWater() + ", age=" + stone.tickCount + ")"));
    }

    /** Builds a long pool along the test's +x axis and throws a stone down it. */
    private static SkippingStoneEntity throwStone(GameTestHelper helper, ThrowResult result, @Nullable ServerPlayer owner) {
        // The empty test structure only keeps its own chunk ticking; the pool reaches well beyond it
        BlockPos poolStart = helper.absolutePos(BlockPos.ZERO);
        BlockPos poolEnd = helper.absolutePos(new BlockPos(POOL_LENGTH, 0, POOL_WIDTH));
        for (int cx = Math.min(poolStart.getX(), poolEnd.getX()) >> 4; cx <= Math.max(poolStart.getX(), poolEnd.getX()) >> 4; cx++) {
            for (int cz = Math.min(poolStart.getZ(), poolEnd.getZ()) >> 4; cz <= Math.max(poolStart.getZ(), poolEnd.getZ()) >> 4; cz++) {
                helper.getLevel().setChunkForced(cx, cz, true);
            }
        }

        for (int x = 0; x < POOL_LENGTH; x++) {
            for (int z = 0; z < POOL_WIDTH; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
                helper.setBlock(new BlockPos(x, 1, z), Blocks.WATER);
                helper.setBlock(new BlockPos(x, 2, z), Blocks.AIR);
                helper.setBlock(new BlockPos(x, 3, z), Blocks.AIR);
            }
        }

        Vec3 start = helper.absoluteVec(new Vec3(1.5, 3.5, POOL_WIDTH / 2.0 + 0.5));
        var stack = SkippingStoneItem.withTier(ModItems.SKIPPING_STONE.toStack(), PERFECT_TIER);
        SkippingStoneEntity stone = owner == null
            ? new SkippingStoneEntity(helper.getLevel(), start.x, start.y, start.z, stack, result)
            : new SkippingStoneEntity(helper.getLevel(), owner, stack, result);
        stone.setPos(start);
        // Thrown along the test's +x axis, whatever rotation the test was placed with
        Vec3 along = helper.absoluteVec(new Vec3(1, 0, 0)).subtract(helper.absoluteVec(Vec3.ZERO)).normalize();
        stone.setDeltaMovement(along.x * 0.6, -0.15, along.z * 0.6);
        helper.assertTrue(helper.getLevel().addFreshEntity(stone), Component.literal("stone could not be spawned"));
        return stone;
    }
}
