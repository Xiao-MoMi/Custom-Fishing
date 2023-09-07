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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    private final ConcurrentHashMap<UUID, CachedPlaceholder> cachedPlaceholders;

    public PlaceholderManagerImpl(CustomFishingPlugin plugin) {
        instance = this;
        this.plugin = plugin;
        this.hasPapi = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        this.pattern = Pattern.compile("\\{[^{}]+}");
        this.customPlaceholderMap = new HashMap<>();
        this.cachedPlaceholders = new ConcurrentHashMap<>();
        if (this.hasPapi) {
            competitionPapi = new CompetitionPapi(plugin);
            statisticsPapi = new StatisticsPapi(plugin);
        }
    }

    public void load() {
        if (competitionPapi != null) competitionPapi.load();
        if (statisticsPapi != null) statisticsPapi.load();
        loadCustomPlaceholders();
    }

    public void unload() {
        if (competitionPapi != null) competitionPapi.unload();
        if (statisticsPapi != null) statisticsPapi.unload();
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

    @Override
    public String setPlaceholders(Player player, String text) {
        return hasPapi ? ParseUtils.setPlaceholders(player, text) : text;
    }

    @Override
    public String setPlaceholders(OfflinePlayer player, String text) {
        return hasPapi ? ParseUtils.setPlaceholders(player, text) : text;
    }

    @Override
    public List<String> detectPlaceholders(String text) {
        List<String> placeholders = new ArrayList<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) placeholders.add(matcher.group());
        return placeholders;
    }

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
                    replacer = setPlaceholders(player, custom);
                }
            }
            if (replacer != null) {
                text = text.replace(papi, replacer);
            }
        }
        return text;
    }

    @Override
    public String parseCacheable(Player player, String text) {
        var list = detectPlaceholders(text);
        CachedPlaceholder cachedPlaceholder = cachedPlaceholders.computeIfAbsent(player.getUniqueId(), k -> new CachedPlaceholder());
        for (String papi : list) {
            String custom = customPlaceholderMap.get(papi);
            if (custom != null) {
                text = text.replace(papi, cachedPlaceholder.get(player, custom));
            }
        }
        return text;
    }

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

    public static class CachedPlaceholder {

        private long lastUpdated;
        private final HashMap<String, String> cached;

        public CachedPlaceholder() {
            this.lastUpdated = 0;
            this.cached = new HashMap<>();
        }

        public synchronized String get(Player player, String custom) {
            long current = System.currentTimeMillis();
            if (current - lastUpdated > 3000) {
                lastUpdated = current;
                String parsed = ParseUtils.setPlaceholders(player, custom);
                cached.put(custom, parsed);
                return parsed;
            } else {
                String cachedStr = cached.get(custom);
                if (cachedStr != null)
                    return cachedStr;
                String parsed = ParseUtils.setPlaceholders(player, custom);
                cached.put(custom, parsed);
                return parsed;
            }
        }
    }
}
