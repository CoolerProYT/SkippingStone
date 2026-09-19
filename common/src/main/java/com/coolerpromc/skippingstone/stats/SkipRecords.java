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

public class SkipRecords extends SavedData {
    public record PlayerRecord(String name, int bestSkips, double bestDistance) {
        public static final PlayerRecord NONE = new PlayerRecord("", 0, 0);

        public static final Codec<PlayerRecord> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.optionalFieldOf("name", "").forGetter(PlayerRecord::name),
            Codec.INT.fieldOf("bestSkips").forGetter(PlayerRecord::bestSkips),
            Codec.DOUBLE.fieldOf("bestDistance").forGetter(PlayerRecord::bestDistance)
        ).apply(i, PlayerRecord::new));

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

    @FunctionalInterface
    public interface RecordListener {
        void onRecordImproved(ServerPlayer player, Update update);
    }

    private static final Codec<SkipRecords> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, PlayerRecord.CODEC).xmap(SkipRecords::new, records -> records.records);

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
