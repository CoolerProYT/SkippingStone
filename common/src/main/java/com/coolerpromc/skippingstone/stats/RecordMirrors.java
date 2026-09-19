package com.coolerpromc.skippingstone.stats;

import com.coolerpromc.skippingstone.config.ModCommonConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/**
 * Copies {@link SkipRecords} into the places players look: the vanilla Statistics screen and, if enabled, two
 * scoreboard objectives that servers can show with {@code /scoreboard objectives setdisplay}.
 */
public class RecordMirrors {
    public static final String SKIPS_OBJECTIVE = "skippingstone.best_skips";
    public static final String DISTANCE_OBJECTIVE = "skippingstone.best_distance";

    public static void init() {
        SkipRecords.addListener((player, update) -> {
            updateStats(player, update.current());
            if (ModCommonConfig.SCOREBOARD_OBJECTIVES.get()) {
                updateScores(player.level().getServer(), update.current());
            }
        });
    }

    /** Records are the source of truth, so a lost or edited stats file is corrected on the next join. */
    public static void onPlayerJoin(ServerPlayer player) {
        SkipRecords.PlayerRecord record = SkipRecords.get(player.level().getServer()).get(player.getUUID());
        updateStats(player, record);
    }

    /** Fills the objectives with every stored record, including players who are offline. */
    public static void onServerStarted(MinecraftServer server) {
        if (!ModCommonConfig.SCOREBOARD_OBJECTIVES.get()) {
            return;
        }
        SkipRecords.get(server).all().values().forEach(record -> updateScores(server, record));
    }

    private static void updateStats(ServerPlayer player, SkipRecords.PlayerRecord record) {
        player.getStats().setValue(player, Stats.CUSTOM.get(ModStats.BEST_SKIPS.get()), record.bestSkips());
        player.getStats().setValue(player, Stats.CUSTOM.get(ModStats.BEST_SKIP_DISTANCE.get()), (int) Math.round(record.bestDistance() * 100));
    }

    private static void updateScores(MinecraftServer server, SkipRecords.PlayerRecord record) {
        if (record.name().isEmpty()) {
            return;
        }
        ServerScoreboard scoreboard = server.getScoreboard();
        ScoreHolder holder = ScoreHolder.forNameOnly(record.name());
        scoreboard.getOrCreatePlayerScore(holder, objective(scoreboard, SKIPS_OBJECTIVE, "objective.skippingstone.best_skips")).set(record.bestSkips());
        scoreboard.getOrCreatePlayerScore(holder, objective(scoreboard, DISTANCE_OBJECTIVE, "objective.skippingstone.best_distance")).set(record.bestDistanceBlocks());
    }

    private static Objective objective(ServerScoreboard scoreboard, String name, String displayKey) {
        Objective objective = scoreboard.getObjective(name);
        if (objective == null) {
            objective = scoreboard.addObjective(name, ObjectiveCriteria.DUMMY, Component.translatable(displayKey), ObjectiveCriteria.RenderType.INTEGER, true, null);
        }
        return objective;
    }
}
