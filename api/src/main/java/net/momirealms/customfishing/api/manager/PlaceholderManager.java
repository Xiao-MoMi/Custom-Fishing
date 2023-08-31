package net.momirealms.customfishing.api.manager;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface PlaceholderManager {

    String setPlaceholders(Player player, String text);

    String setPlaceholders(OfflinePlayer player, String text);

    List<String> detectPlaceholders(String text);

    String getSingleValue(@Nullable Player player, String placeholder, Map<String, String> placeholders);

    String parse(@Nullable OfflinePlayer player, String text, Map<String, String> placeholders);

    List<String> parse(@Nullable OfflinePlayer player, List<String> list, Map<String, String> replacements);
}
