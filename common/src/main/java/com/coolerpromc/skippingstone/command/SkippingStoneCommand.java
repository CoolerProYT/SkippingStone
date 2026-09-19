package com.coolerpromc.skippingstone.command;

import com.coolerpromc.skippingstone.stats.SkipRecords;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.util.Collection;
import java.util.List;

public class SkippingStoneCommand {
    private static final int LEADERBOARD_SIZE = 10;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("skippingstone")
            .then(Commands.literal("records")
                .executes(c -> showOwn(c.getSource()))
                .then(Commands.literal("top")
                    .executes(c -> showTop(c.getSource(), SkipRecords.Ranking.SKIPS))
                    .then(Commands.literal("skips").executes(c -> showTop(c.getSource(), SkipRecords.Ranking.SKIPS)))
                    .then(Commands.literal("distance").executes(c -> showTop(c.getSource(), SkipRecords.Ranking.DISTANCE))))
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                    .executes(c -> showPlayers(c.getSource(), GameProfileArgument.getGameProfiles(c, "player"))))));
    }

    private static int showOwn(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        SkipRecords.PlayerRecord record = SkipRecords.get(source.getServer()).get(player.getUUID());
        if (record == SkipRecords.PlayerRecord.NONE) {
            source.sendSuccess(() -> Component.translatable("commands.skippingstone.records.self.none"), false);
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("commands.skippingstone.records.self", record.bestSkips(), blocks(record)), false);
        return record.bestSkips();
    }

    private static int showPlayers(CommandSourceStack source, Collection<NameAndId> players) {
        SkipRecords records = SkipRecords.get(source.getServer());
        for (NameAndId player : players) {
            SkipRecords.PlayerRecord record = records.get(player.id());
            if (record == SkipRecords.PlayerRecord.NONE) {
                source.sendSuccess(() -> Component.translatable("commands.skippingstone.records.other.none", player.name()), false);
            } else {
                source.sendSuccess(() -> Component.translatable("commands.skippingstone.records.other", player.name(), record.bestSkips(), blocks(record)), false);
            }
        }
        return players.size();
    }

    private static int showTop(CommandSourceStack source, SkipRecords.Ranking ranking) {
        List<SkipRecords.PlayerRecord> top = SkipRecords.get(source.getServer()).top(ranking, LEADERBOARD_SIZE);
        if (top.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.skippingstone.records.top.empty"), false);
            return 0;
        }

        String rankedBy = ranking == SkipRecords.Ranking.SKIPS ? "commands.skippingstone.records.top.by_skips" : "commands.skippingstone.records.top.by_distance";
        source.sendSuccess(() -> Component.translatable("commands.skippingstone.records.top.header", Component.translatable(rankedBy)).withStyle(ChatFormatting.GOLD), false);
        for (int i = 0; i < top.size(); i++) {
            SkipRecords.PlayerRecord record = top.get(i);
            int rank = i + 1;
            source.sendSuccess(() -> Component.translatable("commands.skippingstone.records.top.entry", rank, record.name(), record.bestSkips(), blocks(record)), false);
        }
        return top.size();
    }

    private static String blocks(SkipRecords.PlayerRecord record) {
        return String.format("%.1f", record.bestDistance());
    }
}
