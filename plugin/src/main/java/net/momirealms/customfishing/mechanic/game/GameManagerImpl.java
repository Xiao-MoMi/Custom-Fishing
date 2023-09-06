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

package net.momirealms.customfishing.mechanic.game;

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.manager.GameManager;
import net.momirealms.customfishing.api.mechanic.game.*;
import net.momirealms.customfishing.api.util.FontUtils;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.api.util.OffsetUtils;
import net.momirealms.customfishing.util.ClassUtils;
import net.momirealms.customfishing.util.ConfigUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class GameManagerImpl implements GameManager {

    private final CustomFishingPlugin plugin;
    private final HashMap<String, GameFactory> gameCreatorMap;
    private final HashMap<String, GameInstance> gameMap;
    private final HashMap<String, GameConfig> gameConfigMap;
    private final String EXPANSION_FOLDER = "expansions/minigames";

    public GameManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.gameCreatorMap = new HashMap<>();
        this.gameMap = new HashMap<>();
        this.gameConfigMap = new HashMap<>();
        this.registerInbuiltGames();
    }

    private void registerInbuiltGames() {
        this.registerAccurateClickGame();
        this.registerHoldGame();
        this.registerTensionGame();
    }

    public void load() {
        this.loadExpansions();
        this.loadGamesFromPluginFolder();
        this.loadGameConfigs();
    }

    public void unload() {
        this.gameMap.clear();
        this.gameConfigMap.clear();
    }

    public void disable() {
        unload();
        this.gameCreatorMap.clear();
    }

    @Override
    public boolean registerGameType(String type, GameFactory gameFactory) {
        if (gameCreatorMap.containsKey(type))
            return false;
        else
            gameCreatorMap.put(type, gameFactory);
        return true;
    }

    @Override
    public boolean unregisterGameType(String type) {
        return gameCreatorMap.remove(type) != null;
    }

    @Override
    @Nullable
    public GameFactory getGameCreator(String type) {
        return gameCreatorMap.get(type);
    }

    @Override
    @Nullable
    public GameInstance getGame(String key) {
        return gameMap.get(key);
    }

    @Override
    @Nullable
    public GameConfig getGameConfig(String key) {
        return gameConfigMap.get(key);
    }

    @Override
    public GameInstance getRandomGame() {
        Collection<GameInstance> collection = gameMap.values();
        return (GameInstance) collection.toArray()[ThreadLocalRandom.current().nextInt(collection.size())];
    }

    @Override
    public GameConfig getRandomGameConfig() {
        Collection<GameConfig> collection = gameConfigMap.values();
        return (GameConfig) collection.toArray()[ThreadLocalRandom.current().nextInt(collection.size())];
    }

    public void loadGameConfigs() {
        YamlConfiguration config = plugin.getConfig("game-groups.yml");
        for (Map.Entry<String, Object> entry : config.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection section) {
                if (section.contains("groups")) {
                    gameConfigMap.put(entry.getKey(), new GameGroups(ConfigUtils.getWeights(section.getStringList("groups"))));
                } else if (section.contains("games")) {
                    var pair1 = ConfigUtils.splitStringIntegerArgs(section.getString("difficulty", "1~100"));
                    var pair2 = ConfigUtils.splitStringIntegerArgs(section.getString("time", "10~20"));
                    gameConfigMap.put(entry.getKey(),
                            new GameGroup(ConfigUtils.getWeights(section.getStringList("games")))
                                    .difficulty(pair1.left(), pair1.right())
                                    .time(pair2.left(), pair2.right())
                    );
                }
            }
        }
    }

    public void loadGamesFromPluginFolder() {
        Deque<File> fileDeque = new ArrayDeque<>();
        File typeFolder = new File(plugin.getDataFolder() + File.separator + "contents" + File.separator + "minigames");
        if (!typeFolder.exists()) {
            if (!typeFolder.mkdirs()) return;
            plugin.saveResource("contents" + File.separator + "minigames" + File.separator + "default.yml", false);
        }
        fileDeque.push(typeFolder);
        while (!fileDeque.isEmpty()) {
            File file = fileDeque.pop();
            File[] files = file.listFiles();
            if (files == null) continue;
            for (File subFile : files) {
                if (subFile.isDirectory()) {
                    fileDeque.push(subFile);
                } else if (subFile.isFile() && subFile.getName().endsWith(".yml")) {
                    loadSingleFile(subFile);
                }
            }
        }
    }

    private void loadSingleFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map.Entry<String, Object> entry : config.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection section) {
                GameFactory creator = this.getGameCreator(section.getString("game-type"));
                if (creator != null) {
                    gameMap.put(entry.getKey(), creator.setArgs(section));
                }
            }
        }
    }

    private void registerAccurateClickGame() {
        this.registerGameType("accurate_click", (section -> {

            Set<String> chances = Objects.requireNonNull(section.getConfigurationSection("success-rate-sections")).getKeys(false);
            var widthPerSection = section.getInt("arguments.width-per-section", 16);
            var successRate = new double[chances.size()];
            for(int i = 0; i < chances.size(); i++)
                successRate[i] = section.getDouble("success-rate-sections." + (i + 1));
            var totalWidth = chances.size() * widthPerSection - 1;
            var pointerOffset = section.getInt("arguments.pointer-offset");
            var pointerWidth = section.getInt("arguments.pointer-width");
            var title = section.getString("title");
            var font = section.getString("subtitle.font");
            var barImage = section.getString("subtitle.bar");
            var pointerImage = section.getString("subtitle.pointer");

            return (player, fishHook, settings) -> new AbstractGamingPlayer(player, fishHook, settings) {

                private int progress;
                private boolean face;

                @Override
                public void arrangeTask() {
                    var period = ((double) 10*(200-settings.getDifficulty()))/((double) (1+4*settings.getDifficulty()));
                    this.task = CustomFishingPlugin.get().getScheduler().runTaskAsyncTimer(
                            this,
                            50,
                            (long) period,
                            TimeUnit.MILLISECONDS
                    );
                }

                @Override
                public void run() {
                    super.run();
                    if (face) progress++;
                    else progress--;
                    if (progress > totalWidth) {
                        face = !face;
                        progress = 2 * totalWidth - progress;
                    } else if (progress < 0) {
                        face = !face;
                        progress = -progress;
                    }
                    showUI();
                }

                public void showUI() {
                    String bar = FontUtils.surroundWithFont(barImage, font)
                               + OffsetUtils.getOffsetChars(pointerOffset + progress)
                               + FontUtils.surroundWithFont(pointerImage, font)
                               + OffsetUtils.getOffsetChars(totalWidth - progress - pointerWidth);
                    AdventureManagerImpl.getInstance().sendTitle(player, title, bar,0,500,0);
                }

                @Override
                public boolean isSuccessful() {
                    int last = progress / widthPerSection;
                    return (Math.random() < successRate[last]);
                }
            };
        }));
    }

    private void registerHoldGame() {
        this.registerGameType("hold", (section -> {

            var timeRequirements = section.getIntegerList("hold-time-requirements").stream().mapToInt(Integer::intValue).toArray();
            var judgementAreaImage = section.getString("subtitle.judgment-area");
            var fishImage = section.getString("subtitle.fish");
            var barEffectiveWidth = section.getInt("arguments.bar-effective-area-width");
            var judgementAreaOffset = section.getInt("arguments.judgment-area-offset");
            var judgementAreaWidth = section.getInt("arguments.judgment-area-width");
            var fishIconWidth = section.getInt("arguments.fish-icon-width");
            var punishment = section.getDouble("arguments.punishment");
            var progress = section.getStringList("progress").toArray(new String[0]);
            var waterResistance = section.getDouble("arguments.water-resistance", 0.15);
            var pullingStrength = section.getDouble("arguments.pulling-strength", 0.45);
            var looseningLoss = section.getDouble("arguments.loosening-strength-loss", 0.3);

            var title = section.getString("title","{progress}");
            var font = section.getString("subtitle.font");
            var barImage = section.getString("subtitle.bar");
            var tip = section.getString("tip");

            return (player, fishHook, settings) -> new AbstractGamingPlayer(player, fishHook, settings) {
                private double hold_time;
                private double judgement_position;
                private double fish_position;
                private double judgement_velocity;
                private double fish_velocity;
                private int timer;
                private final int time_requirement = timeRequirements[ThreadLocalRandom.current().nextInt(timeRequirements.length)] * 1000;
                private boolean played;

                @Override
                public void arrangeTask() {
                    this.judgement_position = (double) (barEffectiveWidth - judgementAreaWidth) / 2;
                    this.task = CustomFishingPlugin.get().getScheduler().runTaskAsyncTimer(
                            this,
                            50,
                            33,
                            TimeUnit.MILLISECONDS
                    );
                }

                @Override
                public void run() {
                    super.run();
                    if (player.isSneaking()) addV();
                    else reduceV();
                    if (timer < 40 - (settings.getDifficulty() / 10)) {
                        timer++;
                    } else {
                        timer = 0;
                        if (Math.random() > ((double) 25 / (settings.getDifficulty() + 100))) {
                            burst();
                        }
                    }
                    judgement_position += judgement_velocity;
                    fish_position += fish_velocity;
                    fraction();
                    calibrate();
                    if (fish_position >= judgement_position - 2 && fish_position + fishIconWidth <= judgement_position + judgementAreaWidth + 2) {
                        hold_time += 33;
                    } else {
                        hold_time -= punishment * 33;
                    }
                    if (hold_time >= time_requirement) {
                        setGameResult(true);
                        endGame();
                        return;
                    }
                    hold_time = Math.max(0, Math.min(hold_time, time_requirement));
                    showUI();
                }

                private void burst() {
                    if (Math.random() < (judgement_position / barEffectiveWidth)) {
                        judgement_velocity = -1 - 0.8 * Math.random() * ((double) settings.getDifficulty() / 15);
                    } else {
                        judgement_velocity = 1 + 0.8 * Math.random() * ((double) settings.getDifficulty() / 15);
                    }
                }

                private void fraction() {
                    if (judgement_velocity > 0) {
                        judgement_velocity -= waterResistance;
                        if (judgement_velocity < 0) judgement_velocity = 0;
                    } else {
                        judgement_velocity += waterResistance;
                        if (judgement_velocity > 0) judgement_velocity = 0;
                    }
                }

                private void reduceV() {
                    fish_velocity -= looseningLoss;
                }

                private void addV() {
                    played = true;
                    fish_velocity += pullingStrength;
                }

                private void calibrate() {
                    if (fish_position < 0) {
                        fish_position = 0;
                        fish_velocity = 0;
                    }
                    if (fish_position + fishIconWidth > barEffectiveWidth) {
                        fish_position = barEffectiveWidth - fishIconWidth;
                        fish_velocity = 0;
                    }
                    if (judgement_position < 0) {
                        judgement_position = 0;
                        judgement_velocity = 0;
                    }
                    if (judgement_position + judgementAreaWidth > barEffectiveWidth) {
                        judgement_position = barEffectiveWidth - judgementAreaWidth;
                        judgement_velocity = 0;
                    }
                }

                public void showUI() {
                    String bar = FontUtils.surroundWithFont(barImage, font)
                            + OffsetUtils.getOffsetChars((int) (judgementAreaOffset + judgement_position))
                            + FontUtils.surroundWithFont(judgementAreaImage, font)
                            + OffsetUtils.getOffsetChars((int) (barEffectiveWidth - judgement_position - judgementAreaWidth))
                            + OffsetUtils.getOffsetChars((int) (-barEffectiveWidth - 1 + fish_position))
                            + FontUtils.surroundWithFont(fishImage, font)
                            + OffsetUtils.getOffsetChars((int) (barEffectiveWidth - fish_position - fishIconWidth + 1))
                            ;
                    AdventureManagerImpl.getInstance().sendTitle(
                            player,
                            tip != null && !played ? tip : title.replace("{progress}", progress[(int) ((hold_time / time_requirement) * progress.length)])
                            ,
                            bar,
                            0,
                            500,
                            0
                    );
                }
            };
        }));
    }

    private void registerTensionGame() {
        this.registerGameType("tension", (section -> {

            var fishIconWidth = section.getInt("arguments.fish-icon-width");
            var fishImage = section.getString("subtitle.fish");
            var tension = section.getStringList("tension").toArray(new String[0]);
            var strugglingFishImage = section.getStringList("subtitle.struggling-fish").toArray(new String[0]);
            var barEffectiveWidth = section.getInt("arguments.bar-effective-area-width");
            var fishOffset = section.getInt("arguments.fish-offset");
            var fishStartPosition = section.getInt("arguments.fish-start-position");
            var successPosition = section.getInt("arguments.success-position");
            var ultimateTension = section.getDouble("arguments.ultimate-tension", 50);
            var normalIncrease = section.getDouble("arguments.normal-pull-tension-increase", 1);
            var strugglingIncrease = section.getDouble("arguments.struggling-tension-increase", 2);
            var tensionLoss = section.getDouble("arguments.loosening-tension-loss", 2);

            var title = section.getString("title","{progress}");
            var font = section.getString("subtitle.font");
            var barImage = section.getString("subtitle.bar");
            var tip = section.getString("tip");

            return (player, fishHook, settings) -> new AbstractGamingPlayer(player, fishHook, settings) {

                private int fish_position = fishStartPosition;
                private double strain;
                private int struggling_time;
                private boolean played;

                @Override
                public void arrangeTask() {
                    this.task = CustomFishingPlugin.get().getScheduler().runTaskAsyncTimer(this, 50, 40, TimeUnit.MILLISECONDS);
                }

                @Override
                public void run() {
                    super.run();
                    if (struggling_time <= 0) {
                        if (Math.random() < ((double) settings.getDifficulty() / 4000)) {
                            struggling_time = (int) (10 + Math.random() * (settings.getDifficulty() / 4));
                        }
                    } else {
                        struggling_time--;
                    }
                    if (player.isSneaking()) pull();
                    else loosen();
                    if (fish_position < successPosition - fishIconWidth - 1) {
                        setGameResult(true);
                        endGame();
                        return;
                    }
                    if (fish_position + fishIconWidth > barEffectiveWidth || strain >= ultimateTension) {
                        setGameResult(false);
                        endGame();
                        return;
                    }
                    showUI();
                }

                public void pull() {
                    played = true;
                    if (struggling_time > 0) {
                        strain += (strugglingIncrease + ((double) settings.getDifficulty() / 50));
                        fish_position -= 1;
                    } else {
                        strain += normalIncrease;
                        fish_position -= 2;
                    }
                }

                public void loosen() {
                    fish_position++;
                    strain -= tensionLoss;
                }

                public void showUI() {
                    String bar = FontUtils.surroundWithFont(barImage, font)
                            + OffsetUtils.getOffsetChars(fishOffset + fish_position)
                            + FontUtils.surroundWithFont((struggling_time > 0 ? strugglingFishImage[struggling_time % strugglingFishImage.length] : fishImage), font)
                            + OffsetUtils.getOffsetChars(barEffectiveWidth - fish_position - fishIconWidth);
                    strain = Math.max(0, Math.min(strain, ultimateTension));
                    AdventureManagerImpl.getInstance().sendTitle(
                            player,
                            tip != null && !played ? tip : title.replace("{tension}", tension[(int) ((strain / ultimateTension) * tension.length)]),
                            bar,
                            0,
                            500,
                            0
                    );
                }
            };
        }));
    }

    private void loadExpansions() {
        File expansionFolder = new File(plugin.getDataFolder(), EXPANSION_FOLDER);
        if (!expansionFolder.exists())
            expansionFolder.mkdirs();

        List<Class<? extends GameExpansion>> classes = new ArrayList<>();
        File[] expansionJars = expansionFolder.listFiles();
        if (expansionJars == null) return;
        for (File expansionJar : expansionJars) {
            if (expansionJar.getName().endsWith(".jar")) {
                try {
                    Class<? extends GameExpansion> expansionClass = ClassUtils.findClass(expansionJar, GameExpansion.class);
                    classes.add(expansionClass);
                } catch (IOException | ClassNotFoundException e) {
                    LogUtils.warn("Failed to load expansion: " + expansionJar.getName(), e);
                }
            }
        }
        try {
            for (Class<? extends GameExpansion> expansionClass : classes) {
                GameExpansion expansion = expansionClass.getDeclaredConstructor().newInstance();
                unregisterGameType(expansion.getGameType());
                registerGameType(expansion.getGameType(), expansion.getGameFactory());
                LogUtils.info("Loaded minigame expansion: " + expansion.getGameType() + "[" + expansion.getVersion() + "]" + " by " + expansion.getAuthor() );
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            LogUtils.warn("Error occurred when creating expansion instance.", e);
        }
    }
}
