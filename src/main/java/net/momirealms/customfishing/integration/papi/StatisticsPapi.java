package net.momirealms.customfishing.integration.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.momirealms.customfishing.CustomFishing;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatisticsPapi extends PlaceholderExpansion {

    private CustomFishing plugin;

    public StatisticsPapi(CustomFishing plugin) {
        this.plugin = plugin;
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
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        return "null";
    }
}
