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

package net.momirealms.customfishing.mechanic.loot;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.manager.LootManager;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.game.GameConfig;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.loot.LootType;
import net.momirealms.customfishing.api.util.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class LootManagerImpl implements LootManager {

    private final CustomFishingPlugin plugin;
    private final HashMap<String, Loot> lootMap;
    public static CFLoot globalLootProperties;
    private boolean disableStats;
    private boolean disableGames;
    private boolean instantGame;
    private boolean showInFinder;
    private String gameGroup;

    public LootManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.lootMap = new HashMap<>();
    }

    public void load() {
        this.loadGlobalLootProperties();
        this.loadLootsFromPluginFolder();
    }

    public void unload() {
        this.lootMap.clear();
    }

    public void disable() {
        unload();
    }

    @SuppressWarnings("DuplicatedCode")
    public void loadLootsFromPluginFolder() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (String type : List.of("loots", "mobs", "blocks")) {
            File typeFolder = new File(plugin.getDataFolder() + File.separator + "contents" + File.separator + type);
            if (!typeFolder.exists()) {
                if (!typeFolder.mkdirs()) return;
                plugin.saveResource("contents" + File.separator + type + File.separator + "default.yml", false);
            }
            fileDeque.push(typeFolder);
            while (!fileDeque.isEmpty()) {
                File file = fileDeque.pop();
                File[] files = file.listFiles();
                if (files == null) continue;
                for (File subFile : files) {
                    if (subFile.isDirectory()) {
                        fileDeque.push(subFile);
                    } else if (subFile.isFile()) {
                        loadSingleFile(subFile, StringUtils.chop(type));
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public Loot getLoot(String key) {
        return lootMap.get(key);
    }

    private void loadGlobalLootProperties() {
        YamlConfiguration config = plugin.getConfig("config.yml");
        globalLootProperties = getSingleSectionItem(
                Objects.requireNonNull(config.getConfigurationSection("mechanics.global-loot-properties")),
                "GLOBAL",
                "global"
        );
        disableStats = globalLootProperties.disableStats();
        disableGames = globalLootProperties.disableGame();
        instantGame = globalLootProperties.instanceGame();
        showInFinder = globalLootProperties.showInFinder();
        gameGroup = globalLootProperties.gameConfig;
    }

    private void loadSingleFile(File file, String namespace) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (Map.Entry<String, Object> entry : yaml.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection section) {
                var loot = getSingleSectionItem(
                        section,
                        namespace,
                        entry.getKey()
                );
                if (lootMap.containsKey(entry.getKey())) {
                    LogUtils.severe("Duplicated loot found: " + entry.getKey() + ".");
                } else {
                    lootMap.put(entry.getKey(), loot);
                }
            }
        }
    }

    private CFLoot getSingleSectionItem(ConfigurationSection section, String namespace, String key) {
        return new CFLoot.Builder(key, LootType.valueOf(namespace.toUpperCase(Locale.ENGLISH)))
                .disableStats(section.getBoolean("disable-stat", disableStats))
                .disableGames(section.getBoolean("disable-game", disableGames))
                .instantGame(section.getBoolean("instant-game", instantGame))
                .showInFinder(section.getBoolean("show-in-fishfinder", showInFinder))
                .gameConfig(section.getString("game-group", gameGroup))
                .nick(section.getString("nick", section.getString("display.name", key)))
                .addActions(getActionMap(section.getConfigurationSection("action")))
                .addTimesActions(getTimesActionMap(section.getConfigurationSection("action.success-times")))
                .build();
    }

    private HashMap<ActionTrigger, Action[]> getActionMap(ConfigurationSection section) {
        HashMap<ActionTrigger, Action[]> actionMap = new HashMap<>();
        if (section == null) return actionMap;
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection innerSection) {
                actionMap.put(
                        ActionTrigger.valueOf(entry.getKey().toUpperCase(Locale.ENGLISH)),
                        plugin.getActionManager().getActions(innerSection)
                );
            }
        }
        return actionMap;
    }

    private HashMap<Integer, Action[]> getTimesActionMap(ConfigurationSection section) {
        HashMap<Integer, Action[]> actionMap = new HashMap<>();
        if (section == null) return actionMap;
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection innerSection) {
                actionMap.put(Integer.parseInt(entry.getKey()), plugin.getActionManager().getActions(innerSection));
            }
        }
        return actionMap;
    }

    public static class CFLoot implements Loot {

        private final String id;
        private final LootType type;
        private String gameConfig;
        private final HashMap<ActionTrigger, Action[]> actionMap;
        private final HashMap<Integer, Action[]> successTimesActionMap;
        private String nick;
        private boolean showInFinder;
        private boolean disableGame;
        private boolean disableStats;
        private boolean instanceGame;
        private double score;

        public CFLoot(String id, LootType type) {
            this.id = id;
            this.type = type;
            this.actionMap = new HashMap<>();
            this.successTimesActionMap = new HashMap<>();
        }

        public static CFLoot of(String id, LootType type) {
            return new CFLoot(id, type);
        }

        public static class Builder {

            private final CFLoot loot;

            public Builder(String id, LootType type) {
                this.loot = new CFLoot(id, type);
            }

            public Builder nick(String nick) {
                this.loot.nick = nick;
                return this;
            }

            public Builder showInFinder(boolean show) {
                this.loot.showInFinder = show;
                return this;
            }

            public Builder instantGame(boolean instant) {
                this.loot.instanceGame = instant;
                return this;
            }

            public Builder gameConfig(String gameConfig) {
                this.loot.gameConfig = gameConfig;
                return this;
            }

            public Builder disableGames(boolean disable) {
                this.loot.disableGame = disable;
                return this;
            }

            public Builder disableStats(boolean disable) {
                this.loot.disableStats = disable;
                return this;
            }

            public Builder score(double score) {
                this.loot.score = score;
                return this;
            }

            public Builder addActions(ActionTrigger trigger, Action[] actions) {
                this.loot.actionMap.put(trigger, actions);
                return this;
            }

            public Builder addActions(HashMap<ActionTrigger, Action[]> actionMap) {
                this.loot.actionMap.putAll(actionMap);
                return this;
            }

            public Builder addTimesActions(int times, Action[] actions) {
                this.loot.successTimesActionMap.put(times, actions);
                return this;
            }

            public Builder addTimesActions(HashMap<Integer, Action[]> actionMap) {
                this.loot.successTimesActionMap.putAll(actionMap);
                return this;
            }

            public CFLoot build() {
                return loot;
            }
        }

        @Override
        public boolean instanceGame() {
            return this.instanceGame;
        }

        @Override
        public String getID() {
            return this.id;
        }

        @Override
        public LootType getType() {
            return this.type;
        }

        @Override
        public @NotNull String getNick() {
            return this.nick;
        }

        @Override
        public boolean showInFinder() {
            return this.showInFinder;
        }

        @Override
        public double getScore() {
            return this.score;
        }

        @Override
        public boolean disableGame() {
            return this.disableGame;
        }

        @Override
        public boolean disableStats() {
            return this.disableStats;
        }

        @Override
        public GameConfig getGameConfig() {
            return CustomFishingPlugin.get().getGameManager().getGameConfig(this.gameConfig);
        }

        @Override
        public Action[] getActions(ActionTrigger actionTrigger) {
            return actionMap.get(actionTrigger);
        }

        @Override
        public Action[] getSuccessTimesActions(int times) {
            return successTimesActionMap.get(times);
        }

        @Override
        public HashMap<Integer, Action[]> getSuccessTimesActionMap() {
            return successTimesActionMap;
        }
    }

}
