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

package net.momirealms.customfishing.manager;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.MiniGameConfig;
import net.momirealms.customfishing.fishing.bar.FishingBar;
import net.momirealms.customfishing.fishing.bar.ModeOneBar;
import net.momirealms.customfishing.fishing.bar.ModeThreeBar;
import net.momirealms.customfishing.fishing.bar.ModeTwoBar;
import net.momirealms.customfishing.object.Function;
import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class BarMechanicManager extends Function {

    private final CustomFishing plugin;
    private final HashMap<String, FishingBar> bars;
    private final HashMap<String, MiniGameConfig> miniGames;

    public BarMechanicManager(CustomFishing plugin) {
        this.plugin = plugin;
        this.bars = new HashMap<>();
        this.miniGames = new HashMap<>();
    }

    @Override
    public void load() {
        loadBars();
        loadGames();
    }

    @Override
    public void unload() {
        bars.clear();
        miniGames.clear();
    }

    private void loadGames() {
        File game_file = new File(plugin.getDataFolder() + File.separator + "minigames");
        if (!game_file.exists()) {
            if (!game_file.mkdir()) return;
            plugin.saveResource("minigames" + File.separator + "default.yml", false);
        }
        File[] files = game_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            for (String key : config.getKeys(false)) {
                ConfigurationSection section = config.getConfigurationSection(key);
                if (section == null) continue;
                List<FishingBar> fishingBarList = new ArrayList<>();
                for (String bar : section.getStringList("bars")) {
                    if (bars.containsKey(bar)) {
                        fishingBarList.add(bars.get(bar));
                    }
                    else {
                        AdventureUtil.consoleMessage("<red>[CustomFishing] Bar " + bar + " doesn't exist");
                    }
                }
                int[] difficulties = section.getIntegerList("difficulty").stream().mapToInt(Integer::intValue).toArray();
                if (difficulties.length == 0) {
                    AdventureUtil.consoleMessage("<red>[CustomFishing] Game " + key + " doesn't have difficulties");
                    continue;
                }
                MiniGameConfig miniGameConfig = new MiniGameConfig(
                        section.getInt("time", 10),
                        fishingBarList.toArray(new FishingBar[0]),
                        difficulties
                );
                miniGames.put(key, miniGameConfig);
            }
        }
        AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + miniGames.size() + " <gray>game(s)");
    }

    private void loadBars() {
        File bar_file = new File(plugin.getDataFolder() + File.separator + "bars");
        if (!bar_file.exists()) {
            if (!bar_file.mkdir()) return;
            plugin.saveResource("bars" + File.separator + "default.yml", false);
        }
        File[] files = bar_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            for (String key : config.getKeys(false)) {
                ConfigurationSection section = config.getConfigurationSection(key);
                if (section == null) continue;
                int type = section.getInt("game-type");
                if (type == 1) {
                    ModeOneBar modeOneBar = new ModeOneBar(section);
                    bars.put(key, modeOneBar);
                }
                else if (type == 2) {
                    ModeTwoBar modeTwoBar = new ModeTwoBar(section);
                    bars.put(key, modeTwoBar);
                }
                else if (type == 3) {
                    ModeThreeBar modeThreeBar = new ModeThreeBar(section);
                    bars.put(key, modeThreeBar);
                }
            }
        }
        AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + bars.size() + " <gray>bar(s)");
    }

    public MiniGameConfig getGameConfig(String game) {
        return miniGames.get(game);
    }

    public MiniGameConfig getRandomGame() {
        Collection<MiniGameConfig> miniGameConfigs = miniGames.values();
        return (MiniGameConfig) miniGameConfigs.toArray()[new Random().nextInt(miniGameConfigs.size())];
    }
}
