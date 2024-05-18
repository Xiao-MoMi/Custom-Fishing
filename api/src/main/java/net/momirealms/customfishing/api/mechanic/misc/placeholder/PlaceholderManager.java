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

package net.momirealms.customfishing.api.mechanic.misc.placeholder;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public interface PlaceholderManager {

    Pattern PATTERN = Pattern.compile("\\{[^{}]+}");

    /**
     * Registers a custom placeholder with its corresponding original string.
     *
     * @param placeholder the placeholder to register.
     * @param original    the original string corresponding to the placeholder.
     * @return true if the placeholder was successfully registered, false if it already exists.
     */
    boolean registerCustomPlaceholder(String placeholder, String original);

    /**
     * Resolves all placeholders within a given text.
     *
     * @param text the text to resolve placeholders in.
     * @return a list of found placeholders.
     */
    List<String> resolvePlaceholders(String text);

    /**
     * Parses a single placeholder for a specified player, optionally using a map of replacements.
     *
     * @param player       the player for whom the placeholder should be parsed.
     * @param placeholder  the placeholder to parse.
     * @param replacements a map of replacement strings for placeholders.
     * @return the parsed placeholder string.
     */
    String parseSingle(@Nullable OfflinePlayer player, String placeholder, Map<String, String> replacements);

    /**
     * Parses all placeholders in the given text for a specified player, optionally using a map of replacements.
     *
     * @param player       the player for whom the placeholders should be parsed.
     * @param text         the text containing placeholders.
     * @param replacements a map of replacement strings for placeholders.
     * @return the text with parsed placeholders.
     */
    String parse(@Nullable OfflinePlayer player, String text, Map<String, String> replacements);

    /**
     * Parses all placeholders in a list of strings for a specified player, optionally using a map of replacements.
     *
     * @param player       the player for whom the placeholders should be parsed.
     * @param list         the list of strings containing placeholders.
     * @param replacements a map of replacement strings for placeholders.
     * @return the list of strings with parsed placeholders.
     */
    List<String> parse(@Nullable OfflinePlayer player, List<String> list, Map<String, String> replacements);
}
