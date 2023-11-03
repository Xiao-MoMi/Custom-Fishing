/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.api.manager;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface PlaceholderManager {

    /**
     * Set placeholders in a text string for a player.
     *
     * @param player The player for whom the placeholders should be set.
     * @param text   The text string containing placeholders.
     * @return The text string with placeholders replaced if PlaceholderAPI is available; otherwise, the original text.
     */
    String setPlaceholders(Player player, String text);

    /**
     * Set placeholders in a text string for an offline player.
     *
     * @param player The offline player for whom the placeholders should be set.
     * @param text   The text string containing placeholders.
     * @return The text string with placeholders replaced if PlaceholderAPI is available; otherwise, the original text.
     */
    String setPlaceholders(OfflinePlayer player, String text);

    /**
     * Detect and extract placeholders from a text string.
     *
     * @param text The text string to search for placeholders.
     * @return A list of detected placeholders in the text.
     */
    List<String> detectPlaceholders(String text);

    /**
     * Get the value associated with a single placeholder.
     *
     * @param player      The player for whom the placeholders are being resolved (nullable).
     * @param placeholder The placeholder to look up.
     * @param placeholders A map of placeholders to their corresponding values.
     * @return The value associated with the placeholder, or the original placeholder if not found.
     */
    String getSingleValue(@Nullable Player player, String placeholder, Map<String, String> placeholders);

    /**
     * Parse a text string by replacing placeholders with their corresponding values.
     *
     * @param player      The offline player for whom the placeholders are being resolved (nullable).
     * @param text        The text string containing placeholders.
     * @param placeholders A map of placeholders to their corresponding values.
     * @return The text string with placeholders replaced by their values.
     */
    String parse(@Nullable OfflinePlayer player, String text, Map<String, String> placeholders);

    /**
     * Parse a list of text strings by replacing placeholders with their corresponding values.
     *
     * @param player       The player for whom the placeholders are being resolved (can be null for offline players).
     * @param list         The list of text strings containing placeholders.
     * @param replacements A map of custom replacements for placeholders.
     * @return The list of text strings with placeholders replaced by their values.
     */
    List<String> parse(@Nullable OfflinePlayer player, List<String> list, Map<String, String> replacements);
}
