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

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class BukkitPlaceholderManager implements PlaceholderManager {

    private final BukkitCustomFishingPlugin plugin;
    private final boolean hasPapi;
    private final HashMap<String, String> customPlaceholderMap;
    private static BukkitPlaceholderManager instance;

    public BukkitPlaceholderManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.hasPapi = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        this.customPlaceholderMap = new HashMap<>();
        instance = this;
    }

    public static BukkitPlaceholderManager getInstance() {
        return instance;
    }

    @Override
    public boolean registerCustomPlaceholder(String placeholder, String original) {
        if (this.customPlaceholderMap.containsKey(placeholder)) return false;
        this.customPlaceholderMap.put(placeholder, original);
        return true;
    }

    @Override
    public List<String> resolvePlaceholders(String text) {
        List<String> placeholders = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(text);
        while (matcher.find()) placeholders.add(matcher.group());
        return placeholders;
    }

    private String setPlaceholders(OfflinePlayer player, String text) {
        return hasPapi ? PlaceholderAPIUtils.parse(player, text) : text;
    }

    @Override
    public String parseSingle(@Nullable OfflinePlayer player, String placeholder, Map<String, String> replacements) {
        String result = null;
        if (replacements != null)
            result = replacements.get(placeholder);
        if (result != null)
            return result;
        String custom = customPlaceholderMap.get(placeholder);
        if (custom == null)
            return placeholder;
        return setPlaceholders(player, custom);
    }

    @Override
    public String parse(@Nullable OfflinePlayer player, String text, Map<String, String> replacements) {
        var list = resolvePlaceholders(text);
        for (String papi : list) {
            String replacer = null;
            if (replacements != null) {
                replacer = replacements.get(papi);
            }
            if (replacer == null) {
                String custom = customPlaceholderMap.get(papi);
                if (custom != null)
                    replacer = setPlaceholders(player, parse(player, custom, replacements));
            }
            if (replacer != null)
                text = text.replace(papi, replacer);
        }
        return text;
    }

    @Override
    public List<String> parse(@Nullable OfflinePlayer player, List<String> list, Map<String, String> replacements) {
        return list.stream()
                .map(s -> parse(player, s, replacements))
                .collect(Collectors.toList());
    }
}
