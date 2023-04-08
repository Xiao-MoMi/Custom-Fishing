package net.momirealms.customfishing.integration.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.manager.StatisticsManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatisticsPapi extends PlaceholderExpansion {

    private final StatisticsManager statisticsManager;

    public StatisticsPapi(CustomFishing plugin) {
        this.statisticsManager = plugin.getStatisticsManager();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "fishingstats";
    }

    @Override
    public @NotNull String getAuthor() {
        return "XiaoMoMi";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.3";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] args = params.split("_", 2);
        switch (args[0]) {
            case "total" -> {
                return String.valueOf(statisticsManager.getTotalFishAmount(player.getUniqueId()));
            }
            case "amount" -> {
                if (args[1].equals("")) return "lack args";
                return String.valueOf(statisticsManager.getFishAmount(player.getUniqueId(), args[1]));
            }
            case "hascaught" -> {
                if (args[1].equals("")) return "lack args";
                return String.valueOf(statisticsManager.hasFished(player.getUniqueId(), args[1]));
            }
            case "category" -> {
                String[] moreArgs = args[1].split("_", 2);
                if (moreArgs[1].equals("")) return "lack args";
                switch (moreArgs[0]) {
                    case "total" -> {
                        return String.valueOf(statisticsManager.getCategoryTotalFishAmount(player.getUniqueId(), moreArgs[1]));
                    }
                    case "progress" -> {
                        String progress = String.format("%.1f", statisticsManager.getCategoryUnlockProgress(player.getUniqueId(), moreArgs[1]));
                        return progress.equals("100.0") ? "100" : progress;
                    }
                }
            }
        }
        return "null";
    }
}
