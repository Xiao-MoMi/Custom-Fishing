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

package net.momirealms.customfishing.compatibility.papi;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.manager.PlaceholderManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PlaceholderManagerImpl implements PlaceholderManager {

    private static PlaceholderManagerImpl instance;
    private final CustomFishingPlugin plugin;
    private final boolean hasPapi;
    private final Pattern pattern;
    private final HashMap<String, String> customPlaceholderMap;
    private CompetitionPapi competitionPapi;
    private StatisticsPapi statisticsPapi;
    private CFPapi cfPapi;

    public PlaceholderManagerImpl(CustomFishingPlugin plugin) {
        instance = this;
        this.plugin = plugin;
        this.hasPapi = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        this.pattern = Pattern.compile("\\{[^{}]+}");
        this.customPlaceholderMap = new HashMap<>();
        if (this.hasPapi) {
            competitionPapi = new CompetitionPapi(plugin);
            statisticsPapi = new StatisticsPapi(plugin);
            cfPapi = new CFPapi(plugin);
        }
    }

    public void load() {
        if (competitionPapi != null) competitionPapi.load();
        if (statisticsPapi != null) statisticsPapi.load();
        if (cfPapi != null) cfPapi.load();
        loadCustomPlaceholders();
    }

    public void unload() {
        if (competitionPapi != null) competitionPapi.unload();
        if (statisticsPapi != null) statisticsPapi.unload();
        if (cfPapi != null) cfPapi.unload();
    }

    public void disable() {
        this.customPlaceholderMap.clear();
    }

    public void loadCustomPlaceholders() {
        YamlConfiguration config = plugin.getConfig("config.yml");
        ConfigurationSection section = config.getConfigurationSection("other-settings.placeholder-register");
        if (section != null) {
            for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
                this.customPlaceholderMap.put(entry.getKey(), (String) entry.getValue());
            }
        }
    }

    /**
     * Set placeholders in a text string for a player.
     *
     * @param player The player for whom the placeholders should be set.
     * @param text   The text string containing placeholders.
     * @return The text string with placeholders replaced if PlaceholderAPI is available; otherwise, the original text.
     */
    @Override
    public String setPlaceholders(Player player, String text) {
        return hasPapi ? ParseUtils.setPlaceholders(player, text) : text;
    }

    /**
     * Set placeholders in a text string for an offline player.
     *
     * @param player The offline player for whom the placeholders should be set.
     * @param text   The text string containing placeholders.
     * @return The text string with placeholders replaced if PlaceholderAPI is available; otherwise, the original text.
     */
    @Override
    public String setPlaceholders(OfflinePlayer player, String text) {
        return hasPapi ? ParseUtils.setPlaceholders(player, text) : text;
    }

    /**
     * Detect and extract placeholders from a text string.
     *
     * @param text The text string to search for placeholders.
     * @return A list of detected placeholders in the text.
     */
    @Override
    public List<String> detectPlaceholders(String text) {
        List<String> placeholders = new ArrayList<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) placeholders.add(matcher.group());
        return placeholders;
    }

    /**
     * Get the value associated with a single placeholder.
     *
     * @param player      The player for whom the placeholders are being resolved (nullable).
     * @param placeholder The placeholder to look up.
     * @param placeholders A map of placeholders to their corresponding values.
     * @return The value associated with the placeholder, or the original placeholder if not found.
     */
    @Override
    public String getSingleValue(@Nullable Player player, String placeholder, Map<String, String> placeholders) {
        String result = null;
        if (placeholders != null)
             result = placeholders.get(placeholder);
        if (result != null)
            return result;
        String custom = customPlaceholderMap.get(placeholder);
        if (custom == null)
            return placeholder;
        return setPlaceholders(player, custom);
    }

    /**
     * Parse a text string by replacing placeholders with their corresponding values.
     *
     * @param player      The offline player for whom the placeholders are being resolved (nullable).
     * @param text        The text string containing placeholders.
     * @param placeholders A map of placeholders to their corresponding values.
     * @return The text string with placeholders replaced by their values.
     */
    @Override
    public String parse(@Nullable OfflinePlayer player, String text, Map<String, String> placeholders) {
        var list = detectPlaceholders(text);
        for (String papi : list) {
            String replacer = null;
            if (placeholders != null) {
                replacer = placeholders.get(papi);
            }
            if (replacer == null) {
                String custom = customPlaceholderMap.get(papi);
                if (custom != null) {
                    replacer = setPlaceholders(player, parse(player, custom, placeholders));
                }
            }
            if (replacer != null) {
                text = text.replace(papi, replacer);
            }
        }
        return text;
    }

    /**
     * Parse a list of text strings by replacing placeholders with their corresponding values.
     *
     * @param player       The player for whom the placeholders are being resolved (can be null for offline players).
     * @param list         The list of text strings containing placeholders.
     * @param replacements A map of custom replacements for placeholders.
     * @return The list of text strings with placeholders replaced by their values.
     */
    @Override
    public List<String> parse(@Nullable OfflinePlayer player, List<String> list, Map<String, String> replacements) {
        return list.stream()
                .map(s -> parse(player, s, replacements))
                .collect(Collectors.toList());
    }


    public static PlaceholderManagerImpl getInstance() {
        return instance;
    }

    public boolean hasPapi() {
        return hasPapi;
    }
}
