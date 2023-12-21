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
import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.manager.GameManager;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.game.*;
import net.momirealms.customfishing.api.util.FontUtils;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.api.util.OffsetUtils;
import net.momirealms.customfishing.mechanic.requirement.RequirementManagerImpl;
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

@SuppressWarnings("DuplicatedCode")
public class GameManagerImpl implements GameManager {

    private final CustomFishingPlugin plugin;
    private final HashMap<String, GameFactory> gameCreatorMap;
    private final HashMap<String, Pair<BasicGameConfig, GameInstance>> gameInstanceMap;
    private final String EXPANSION_FOLDER = "expansions/minigame";

    public GameManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.gameCreatorMap = new HashMap<>();
        this.gameInstanceMap = new HashMap<>();
        this.registerInbuiltGames();
    }

    private void registerInbuiltGames() {
        this.registerHoldGame();
        this.registerHoldV2Game();
        this.registerTensionGame();
        this.registerClickGame();
        this.registerAccurateClickGame();
        this.registerAccurateClickV2Game();
        this.registerAccurateClickV3Game();
    }

    public void load() {
        this.loadExpansions();
        this.loadGamesFromPluginFolder();
    }

    public void unload() {
        this.gameInstanceMap.clear();
    }

    public void disable() {
        unload();
        this.gameCreatorMap.clear();
    }

    /**
     * Registers a new game type with the specified type identifier.
     *
     * @param type         The type identifier for the game.
     * @param gameFactory  The {@link GameFactory} that creates instances of the game.
     * @return {@code true} if the registration was successful, {@code false} if the type identifier is already registered.
     */
    @Override
    public boolean registerGameType(String type, GameFactory gameFactory) {
        if (gameCreatorMap.containsKey(type))
            return false;
        else
            gameCreatorMap.put(type, gameFactory);
        return true;
    }

    /**
     * Unregisters a game type with the specified type identifier.
     *
     * @param type The type identifier of the game to unregister.
     * @return {@code true} if the game type was successfully unregistered, {@code false} if the type identifier was not found.
     */
    @Override
    public boolean unregisterGameType(String type) {
        return gameCreatorMap.remove(type) != null;
    }

    /**
     * Retrieves the game factory associated with the specified game type.
     *
     * @param type The type identifier of the game.
     * @return The {@code GameFactory} for the specified game type, or {@code null} if not found.
     */
    @Override
    @Nullable
    public GameFactory getGameFactory(String type) {
        return gameCreatorMap.get(type);
    }

    /**
     * Retrieves a game instance and its basic configuration associated with the specified key.
     *
     * @param key The key identifying the game instance.
     * @return An {@code Optional} containing a {@code Pair} of the basic game configuration and the game instance
     *         if found, or an empty {@code Optional} if not found.
     */
    @Override
    public Pair<BasicGameConfig, GameInstance> getGameInstance(String key) {
        return gameInstanceMap.get(key);
    }

    /**
     * Retrieves a map of game names and their associated weights based on the specified conditions.
     *
     * @param condition The condition to evaluate game weights.
     * @return A {@code HashMap} containing game names as keys and their associated weights as values.
     */
    @Override
    public HashMap<String, Double> getGameWithWeight(Condition condition) {
        return ((RequirementManagerImpl) plugin.getRequirementManager()).getGameWithWeight(condition);
    }

    /**
     * Loads minigames from the plugin folder.
     * This method searches for minigame configuration files in the plugin's data folder and loads them.
     */
    public void loadGamesFromPluginFolder() {
        Deque<File> fileDeque = new ArrayDeque<>();
        File typeFolder = new File(plugin.getDataFolder() + File.separator + "contents" + File.separator + "minigame");
        if (!typeFolder.exists()) {
            if (!typeFolder.mkdirs()) return;
            plugin.saveResource("contents" + File.separator + "minigame" + File.separator + "default.yml", false);
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

    /**
     * Loads a minigame configuration from a YAML file.
     * This method parses the YAML file and extracts minigame configurations to be used in the plugin.
     *
     * @param file The YAML file to load.
     */
    private void loadSingleFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map.Entry<String, Object> entry : config.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection section) {
                GameFactory creator = this.getGameFactory(section.getString("game-type"));
                if (creator == null) {
                    LogUtils.warn("Game type:" + section.getString("game-type") + " doesn't exist.");
                    continue;
                }

                BasicGameConfig.Builder basicGameBuilder = new BasicGameConfig.Builder();
                Object time = section.get("time", 15);
                if (time instanceof String str) {
                    String[] split = str.split("~");
                    basicGameBuilder.time(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                } else if (time instanceof Integer integer) {
                    basicGameBuilder.time(integer);
                }
                Object difficulty = section.get("difficulty", "20~80");
                if (difficulty instanceof String str) {
                    String[] split = str.split("~");
                    basicGameBuilder.difficulty(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                } else if (difficulty instanceof Integer integer) {
                    basicGameBuilder.difficulty(integer);
                }
                gameInstanceMap.put(entry.getKey(), Pair.of(basicGameBuilder.build(), creator.setArgs(section)));
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
            var title = ConfigUtils.stringListArgs(section.get("title"));
            var font = section.getString("subtitle.font");
            var barImage = section.getString("subtitle.bar");
            var pointerImage = section.getString("subtitle.pointer");

            return (player, fishHook, settings) -> new AbstractGamingPlayer(player, fishHook, settings) {

                private int progress = -1;
                private boolean face = true;
                private final String sendTitle = title.get(ThreadLocalRandom.current().nextInt(title.size()));

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
                public void onTick() {
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
                    AdventureManagerImpl.getInstance().sendTitle(player, sendTitle, bar,0,10,0);
                }

                @Override
                public boolean isSuccessful() {
                    if (isTimeOut) return false;
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
            var pointerImage = section.getString("subtitle.pointer");
            var barEffectiveWidth = section.getInt("arguments.bar-effective-area-width");
            var judgementAreaOffset = section.getInt("arguments.judgment-area-offset");
            var judgementAreaWidth = section.getInt("arguments.judgment-area-width");
            var pointerIconWidth = section.getInt("arguments.pointer-icon-width");
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
                public void onTick() {
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
                    if (fish_position >= judgement_position && fish_position + pointerIconWidth <= judgement_position + judgementAreaWidth) {
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
                    if (fish_position + pointerIconWidth > barEffectiveWidth) {
                        fish_position = barEffectiveWidth - pointerIconWidth;
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
                            + FontUtils.surroundWithFont(pointerImage, font)
                            + OffsetUtils.getOffsetChars((int) (barEffectiveWidth - fish_position - pointerIconWidth + 1));
                    AdventureManagerImpl.getInstance().sendTitle(
                            player,
                            tip != null && !played ? tip : title.replace("{progress}", progress[(int) ((hold_time / time_requirement) * progress.length)]),
                            bar,
                            0,
                            10,
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
                public void onTick() {
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
                            10,
                            0
                    );
                }
            };
        }));
    }

    private void registerClickGame() {
        this.registerGameType("click", (section -> {

            var title = section.getString("title","<red>{click}");
            var subtitle = section.getString("subtitle", "<gray>Click <white>{clicks} <gray>times to win. Time left <white>{time}s");

            return (player, fishHook, settings) -> new AbstractGamingPlayer(player, fishHook, settings) {

                private int clickedTimes;
                private final int requiredTimes = settings.getDifficulty();

                @Override
                public void arrangeTask() {
                    this.task = CustomFishingPlugin.get().getScheduler().runTaskAsyncTimer(this, 50, 50, TimeUnit.MILLISECONDS);
                }

                @Override
                public void onTick() {
                    showUI();
                }

                @Override
                public boolean onRightClick() {
                    clickedTimes++;
                    if (clickedTimes >= requiredTimes) {
                        setGameResult(true);
                        endGame();
                    }
                    return true;
                }

                public void showUI() {
                    AdventureManagerImpl.getInstance().sendTitle(
                            player,
                            title.replace("{click}", String.valueOf(clickedTimes)),
                            subtitle.replace("{clicks}", String.valueOf(requiredTimes)).replace("{time}", String.format("%.1f", ((double) deadline - System.currentTimeMillis())/1000)),
                            0,
                            10,
                            0
                    );
                }
            };
        }));
    }

    private void registerAccurateClickV2Game() {

        this.registerGameType("accurate_click_v2", (section -> {

            var barWidth = ConfigUtils.getIntegerPair(section.getString("title.total-width", "15~20"));
            var barSuccess = ConfigUtils.getIntegerPair(section.getString("title.success-width","3~4"));
            var barBody = section.getString("title.body","");
            var barPointer = section.getString("title.pointer", "");
            var barTarget = section.getString("title.target","");

            var subtitle = section.getString("subtitle", "<gray>Reel in at the most critical moment");

            return (player, fishHook, settings) -> new AbstractGamingPlayer(player, fishHook, settings) {

                private final int totalWidth = ThreadLocalRandom.current().nextInt(barWidth.right() - barWidth.left() + 1) + barWidth.left();
                private final int successWidth = ThreadLocalRandom.current().nextInt(barSuccess.right() - barSuccess.left() + 1) + barSuccess.left();
                private final int successPosition = ThreadLocalRandom.current().nextInt((totalWidth - successWidth + 1)) + 1;
                private int currentIndex = 0;
                private int timer = 0;
                private boolean face = true;

                @Override
                public void arrangeTask() {
                    this.task = CustomFishingPlugin.get().getScheduler().runTaskAsyncTimer(this, 50, 50, TimeUnit.MILLISECONDS);
                }

                @Override
                public void onTick() {
                    timer++;
                    if (timer % (21 - settings.getDifficulty() / 5) == 0) {
                        movePointer();
                    }
                    showUI();
                }

                private void movePointer() {
                    if (face) {
                        currentIndex++;
                        if (currentIndex >= totalWidth - 1) {
                            face = false;
                        }
                    } else {
                        currentIndex--;
                        if (currentIndex <= 0) {
                            face = true;
                        }
                    }
                }

                public void showUI() {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 1; i <= totalWidth; i++) {
                        if (i == currentIndex + 1) {
                            stringBuilder.append(barPointer);
                            continue;
                        }
                        if (i >= successPosition && i <= successPosition + successWidth - 1) {
                            stringBuilder.append(barTarget);
                            continue;
                        }
                        stringBuilder.append(barBody);
                    }

                    AdventureManagerImpl.getInstance().sendTitle(
                            player,
                            stringBuilder.toString(),
                            subtitle,
                            0,
                            10,
                            0
                    );
                }

                @Override
                public boolean isSuccessful() {
                    return currentIndex + 1 <= successPosition + successWidth - 1 && currentIndex + 1 >= successPosition;
                }
            };
        }));
    }

    private void registerAccurateClickV3Game() {

        this.registerGameType("accurate_click_v3", (section -> {

            var font = section.getString("subtitle.font");
            var pointerImage = section.getString("subtitle.pointer");
            var barImage = section.getString("subtitle.bar");
            var judgementAreaImage = section.getString("subtitle.judgment-area");
            var titles = ConfigUtils.stringListArgs(section.get("title"));

            var barEffectiveWidth = section.getInt("arguments.bar-effective-area-width");
            var judgementAreaWidth = section.getInt("arguments.judgment-area-width");
            var judgementAreaOffset = section.getInt("arguments.judgment-area-offset");
            var pointerIconWidth = section.getInt("arguments.pointer-icon-width");
            var pointerOffset = section.getInt("arguments.pointer-offset");

            return (player, fishHook, settings) -> new AbstractGamingPlayer(player, fishHook, settings) {

                private int progress = -1;
                private boolean face = true;
                private final int judgement_position = ThreadLocalRandom.current().nextInt(barEffectiveWidth - judgementAreaWidth + 1);
                private final String title = titles.get(ThreadLocalRandom.current().nextInt(titles.size()));

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
                public void onTick() {
                    if (face) {
                        progress++;
                        if (progress >= barEffectiveWidth - 1) {
                            face = false;
                        }
                    } else {
                        progress--;
                        if (progress <= 0) {
                            face = true;
                        }
                    }
                    showUI();
                }

                public void showUI() {
                    String bar = FontUtils.surroundWithFont(barImage, font)
                            + OffsetUtils.getOffsetChars(judgementAreaOffset + judgement_position)
                            + FontUtils.surroundWithFont(judgementAreaImage, font)
                            + OffsetUtils.getOffsetChars(barEffectiveWidth - judgement_position - judgementAreaWidth)
                            + OffsetUtils.getOffsetChars(progress + pointerOffset)
                            + FontUtils.surroundWithFont(pointerImage, font)
                            + OffsetUtils.getOffsetChars(barEffectiveWidth - progress - pointerIconWidth + 1);
                    AdventureManagerImpl.getInstance().sendTitle(
                            player,
                            title,
                            bar,
                            0,
                            10,
                            0
                    );
                }

                @Override
                public boolean isSuccessful() {
                    return progress < judgement_position + judgementAreaWidth && progress >= judgement_position;
                }
            };
        }));
    }

    private void registerHoldV2Game() {
        this.registerGameType("hold_v2", (section -> {

            var timeRequirements = section.getIntegerList("hold-time-requirements").stream().mapToInt(Integer::intValue).toArray();
            var judgementAreaImage = section.getString("subtitle.judgment-area");
            var pointerImage = section.getString("subtitle.pointer");
            var barEffectiveWidth = section.getInt("arguments.bar-effective-area-width");
            var judgementAreaOffset = section.getInt("arguments.judgment-area-offset");
            var judgementAreaWidth = section.getInt("arguments.judgment-area-width");
            var pointerIconWidth = section.getInt("arguments.pointer-icon-width");
            var punishment = section.getDouble("arguments.punishment");
            var progress = section.getStringList("progress").toArray(new String[0]);
            var waterResistance = section.getDouble("arguments.water-resistance", 0.15);
            var pullingStrength = section.getDouble("arguments.pulling-strength", 3);
            var looseningLoss = section.getDouble("arguments.loosening-strength-loss", 0.5);

            var title = section.getString("title", "{progress}");
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
                public void onTick() {
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
                    if (fish_position >= judgement_position && fish_position + pointerIconWidth <= judgement_position + judgementAreaWidth) {
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
                    fish_velocity -= looseningLoss;
                    if (fish_velocity < -10 * looseningLoss) {
                        fish_velocity = -10 * looseningLoss;
                    }
                }

                private void calibrate() {
                    if (fish_position < 0) {
                        fish_position = 0;
                        fish_velocity = 0;
                    }
                    if (fish_position + pointerIconWidth > barEffectiveWidth) {
                        fish_position = barEffectiveWidth - pointerIconWidth;
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

                @Override
                public boolean onRightClick() {
                    played = true;
                    fish_velocity = pullingStrength;
                    return true;
                }

                public void showUI() {
                    String bar = FontUtils.surroundWithFont(barImage, font)
                            + OffsetUtils.getOffsetChars((int) (judgementAreaOffset + judgement_position))
                            + FontUtils.surroundWithFont(judgementAreaImage, font)
                            + OffsetUtils.getOffsetChars((int) (barEffectiveWidth - judgement_position - judgementAreaWidth))
                            + OffsetUtils.getOffsetChars((int) (-barEffectiveWidth - 1 + fish_position))
                            + FontUtils.surroundWithFont(pointerImage, font)
                            + OffsetUtils.getOffsetChars((int) (barEffectiveWidth - fish_position - pointerIconWidth + 1));
                    AdventureManagerImpl.getInstance().sendTitle(
                            player,
                            tip != null && !played ? tip : title.replace("{progress}", progress[(int) ((hold_time / time_requirement) * progress.length)]),
                            bar,
                            0,
                            10,
                            0
                    );
                }
            };
        }));
    }

    /**
     * Loads minigame expansions from the expansion folder.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
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
