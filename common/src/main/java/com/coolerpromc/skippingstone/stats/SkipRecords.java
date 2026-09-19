package com.coolerpromc.skippingstone.stats;

import com.coolerpromc.skippingstone.Constants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Source of truth for every player's personal bests, saved per world in {@code data/skippingstone/skip_records.dat}.
 * Kept in one server-wide store (rather than a player attachment) so offline players' records stay available for
 * leaderboards.
 */
public class SkipRecords extends SavedData {
    /**
     * @param name last known player name, so leaderboards and scoreboards can show players who are offline
     */
    public record PlayerRecord(String name, int bestSkips, double bestDistance) {
        public static final PlayerRecord NONE = new PlayerRecord("", 0, 0);

        public static final Codec<PlayerRecord> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.optionalFieldOf("name", "").forGetter(PlayerRecord::name),
            Codec.INT.fieldOf("bestSkips").forGetter(PlayerRecord::bestSkips),
            Codec.DOUBLE.fieldOf("bestDistance").forGetter(PlayerRecord::bestDistance)
        ).apply(i, PlayerRecord::new));

        /** Best distance in whole blocks, for places that only hold integers (scoreboards). */
        public int bestDistanceBlocks() {
            return (int) Math.floor(this.bestDistance);
        }
    }

    public enum Ranking {
        SKIPS(Comparator.comparingInt(PlayerRecord::bestSkips).thenComparingDouble(PlayerRecord::bestDistance)),
        DISTANCE(Comparator.comparingDouble(PlayerRecord::bestDistance).thenComparingInt(PlayerRecord::bestSkips));

        private final Comparator<PlayerRecord> order;

        Ranking(Comparator<PlayerRecord> order) {
            this.order = order;
        }
    }

    /** Result of submitting a throw: the record before and after, and which parts improved. */
    public record Update(PlayerRecord previous, PlayerRecord current) {
        public boolean newSkipRecord() {
            return current.bestSkips() > previous.bestSkips();
        }

        public boolean newDistanceRecord() {
            return current.bestDistance() > previous.bestDistance();
        }

        public boolean improved() {
            return newSkipRecord() || newDistanceRecord();
        }
    }

    /**
     * Notified whenever a player sets a new personal best. This is the hook for mirroring top scores elsewhere, e.g.
     * onto a vanilla Scoreboard objective for display. The store above stays the source of truth.
     */
    @FunctionalInterface
    public interface RecordListener {
        void onRecordImproved(ServerPlayer player, Update update);
    }

    private static final Codec<SkipRecords> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, PlayerRecord.CODEC)
        .xmap(SkipRecords::new, records -> records.records);

    // The data fixer runs unconditionally on load; command storage has no schema changes that touch our tag
    public static final SavedDataType<SkipRecords> TYPE = new SavedDataType<>(Constants.id("skip_records"), SkipRecords::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private static final List<RecordListener> LISTENERS = new ArrayList<>();

    private final Map<UUID, PlayerRecord> records;

    public SkipRecords() {
        this(Map.of());
    }

    private SkipRecords(Map<UUID, PlayerRecord> records) {
        this.records = new HashMap<>(records);
    }

    public static SkipRecords get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public static void addListener(RecordListener listener) {
        LISTENERS.add(listener);
    }

    public PlayerRecord get(UUID player) {
        return this.records.getOrDefault(player, PlayerRecord.NONE);
    }

    public Map<UUID, PlayerRecord> all() {
        return Map.copyOf(this.records);
    }

    /** Best records first, at most {@code limit} entries. */
    public List<PlayerRecord> top(Ranking ranking, int limit) {
        return this.records.values().stream().sorted(ranking.order.reversed()).limit(limit).toList();
    }

    public Update submit(ServerPlayer player, int skips, double distance) {
        PlayerRecord previous = this.get(player.getUUID());
        PlayerRecord current = new PlayerRecord(player.getScoreboardName(), Math.max(previous.bestSkips(), skips), Math.max(previous.bestDistance(), distance));
        Update update = new Update(previous, current);
        if (update.improved()) {
            this.records.put(player.getUUID(), current);
            this.setDirty();
            LISTENERS.forEach(listener -> listener.onRecordImproved(player, update));
        }
        return update;
    }
}
