/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.bukkit.game;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.fishing.CustomFishingHook;
import net.momirealms.customfishing.api.mechanic.game.*;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.api.mechanic.misc.value.TextValue;
import net.momirealms.customfishing.api.mechanic.requirement.ConditionalElement;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementManager;
import net.momirealms.customfishing.api.util.OffsetUtils;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.util.*;
import net.momirealms.sparrow.heart.SparrowHeart;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

@SuppressWarnings("DuplicatedCode")
public class BukkitGameManager implements GameManager {

    private final BukkitCustomFishingPlugin plugin;
    private final Map<String, GameFactory> gameFactoryMap = new HashMap<>();
    private final Map<String, Game> gameMap = new HashMap<>();
    private final LinkedHashMap<String, ConditionalElement<List<Pair<String, BiFunction<Context<Player>, Double, Double>>>, Player>> gameConditions = new LinkedHashMap<>();
    private static final String EXPANSION_FOLDER = "expansions/minigame";

    public BukkitGameManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.registerHoldGame();
        this.registerHoldV2Game();
        this.registerClickGame();
        this.registerTensionGame();
        this.registerDanceGame();
        this.registerAccurateClickGame();
        this.registerAccurateClickV2Game();
        this.registerAccurateClickV3Game();
    }

    @Override
    public void load() {
        this.loadExpansions();
        File file = new File(plugin.getDataFolder(), "game-conditions.yml");
        if (!file.exists()) {
            plugin.getBootstrap().saveResource("game-conditions.yml", false);
        }
        YamlDocument lootConditionsConfig = plugin.getConfigManager().loadData(file);
        for (Map.Entry<String, Object> entry : lootConditionsConfig.getStringRouteMappedValues(false).entrySet()) {
            if (entry.getValue() instanceof Section section) {
                gameConditions.put(entry.getKey(), parseGameConditions(section));
            }
        }
    }

    @Override
    public void unload() {
        this.gameMap.clear();
    }

    private ConditionalElement<List<Pair<String, BiFunction<Context<Player>, Double, Double>>>, Player> parseGameConditions(Section section) {
        Section subSection = section.getSection("sub-groups");
        if (subSection == null) {
            return new ConditionalElement<>(
                    plugin.getConfigManager().parseWeightOperation(section.getStringList("list")),
                    Map.of(),
                    plugin.getRequirementManager().parseRequirements(section.getSection("conditions"), false)
            );
        } else {
            HashMap<String, ConditionalElement<List<Pair<String, BiFunction<Context<Player>, Double, Double>>>, Player>> subElements = new HashMap<>();
            for (Map.Entry<String, Object> entry : subSection.getStringRouteMappedValues(false).entrySet()) {
                if (entry.getValue() instanceof Section innerSection) {
                    subElements.put(entry.getKey(), parseGameConditions(innerSection));
                }
            }
            return new ConditionalElement<>(
                    plugin.getConfigManager().parseWeightOperation(section.getStringList("list")),
                    subElements,
                    plugin.getRequirementManager().parseRequirements(section.getSection("conditions"), false)
            );
        }
    }

    @Override
    public boolean registerGameType(String type, GameFactory gameFactory) {
        if (gameFactoryMap.containsKey(type)) return false;
        gameFactoryMap.put(type, gameFactory);
        return true;
    }

    @Override
    public boolean unregisterGameType(String type) {
        return gameFactoryMap.remove(type) != null;
    }

    @Nullable
    @Override
    public GameFactory getGameFactory(String type) {
        return gameFactoryMap.get(type);
    }

    @Override
    public Optional<Game> getGame(String id) {
        return Optional.ofNullable(gameMap.get(id));
    }

    @Override
    public boolean registerGame(Game game) {
        if (gameMap.containsKey(game.id())) return false;
        gameMap.put(game.id(), game);
        return true;
    }

    @Nullable
    @Override
    public Game getNextGame(Effect effect, Context<Player> context) {
        HashMap<String, Double> lootWeightMap = new HashMap<>();
        for (ConditionalElement<List<Pair<String, BiFunction<Context<Player>, Double, Double>>>, Player> conditionalElement : gameConditions.values()) {
            modifyWeightMap(lootWeightMap, context, conditionalElement);
        }
        String gameID = WeightUtils.getRandom(lootWeightMap);
        return Optional.ofNullable(gameID)
                .map(id -> getGame(gameID).orElseThrow(() -> new RuntimeException("Could not find game " + gameID)))
                .orElse(null);
    }

    private void modifyWeightMap(Map<String, Double> weightMap, Context<Player> context, ConditionalElement<List<Pair<String, BiFunction<Context<Player>, Double, Double>>>, Player> conditionalElement) {
        if (conditionalElement == null) return;
        if (RequirementManager.isSatisfied(context, conditionalElement.getRequirements())) {
            for (Pair<String, BiFunction<Context<Player>, Double, Double>> modifierPair : conditionalElement.getElement()) {
                double previous = weightMap.getOrDefault(modifierPair.left(), 0d);
                weightMap.put(modifierPair.left(), modifierPair.right().apply(context, previous));
            }
            for (ConditionalElement<List<Pair<String, BiFunction<Context<Player>, Double, Double>>>, Player> sub : conditionalElement.getSubElements().values()) {
                modifyWeightMap(weightMap, context, sub);
            }
        }
    }

    private GameBasics getGameBasics(Section section) {
        return GameBasics.builder()
                .difficulty(MathValue.auto(section.get("difficulty", "20~80"), false))
                .time(MathValue.auto(section.get("time", 15), false))
                .build();
    }

    private void registerHoldGame() {
        this.registerGameType("hold", (id, section) -> {
            GameBasics basics = getGameBasics(section);
            return new AbstractGame(id, basics) {

                private final int[] timeRequirements = section.getIntList("hold-time-requirements").stream().mapToInt(Integer::intValue).toArray();
                private final String judgementAreaImage = section.getString("subtitle.judgment-area");
                private final String pointerImage = section.getString("subtitle.pointer");
                private final int barEffectiveWidth = section.getInt("arguments.bar-effective-area-width");
                private final int judgementAreaOffset = section.getInt("arguments.judgment-area-offset");
                private final int judgementAreaWidth = section.getInt("arguments.judgment-area-width");
                private final int pointerIconWidth = section.getInt("arguments.pointer-icon-width");
                private final double punishment = section.getDouble("arguments.punishment");
                private final String[] progress = section.getStringList("progress").toArray(new String[0]);
                private final double waterResistance = section.getDouble("arguments.water-resistance", 0.15);
                private final double pullingStrength = section.getDouble("arguments.pulling-strength", 0.45);
                private final double looseningLoss = section.getDouble("arguments.loosening-strength-loss", 0.3);
                private final TextValue<Player> title = TextValue.auto(section.getString("title","{progress}"));
                private final String font = section.getString("subtitle.font");
                private final String barImage = section.getString("subtitle.bar");
                private final String tip = section.getString("tip");
                private final boolean elasticity = section.getBoolean("arguments.elasticity", false);
                private final double elasticityPower = section.getDouble("arguments.elasticity-power", 0.7);

                @Override
                public BiFunction<CustomFishingHook, GameSetting, AbstractGamingPlayer> gamingPlayerProvider() {
                    return (customFishingHook, gameSetting) -> new AbstractGamingPlayer(customFishingHook, gameSetting) {

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
                            this.task = plugin.getScheduler().asyncRepeating(this, 50, 33, TimeUnit.MILLISECONDS);
                        }

                        @Override
                        public void tick() {
                            if (getPlayer().isSneaking()) addV();
                            else reduceV();
                            if (timer < 40 - (settings.difficulty() / 10)) {
                                timer++;
                            } else {
                                timer = 0;
                                if (Math.random() > ((double) 25 / (settings.difficulty() + 100))) {
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
                                judgement_velocity = -1 - 0.8 * Math.random() * (settings.difficulty() / 15);
                            } else {
                                judgement_velocity = 1 + 0.8 * Math.random() * (settings.difficulty() / 15);
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
                                if (elasticity) {
                                    fish_velocity = -fish_velocity * elasticityPower;
                                } else {
                                    fish_velocity = 0;
                                }
                            }
                            if (fish_position + pointerIconWidth > barEffectiveWidth) {
                                fish_position = barEffectiveWidth - pointerIconWidth;
                                if (elasticity) {
                                    fish_velocity = -fish_velocity * elasticityPower;
                                } else {
                                    fish_velocity = 0;
                                }
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

                        private void showUI() {
                            String bar = AdventureHelper.surroundWithMiniMessageFont(barImage, font)
                                    + OffsetUtils.getOffsetChars((int) (judgementAreaOffset + judgement_position))
                                    + AdventureHelper.surroundWithMiniMessageFont(judgementAreaImage, font)
                                    + OffsetUtils.getOffsetChars((int) (barEffectiveWidth - judgement_position - judgementAreaWidth))
                                    + OffsetUtils.getOffsetChars((int) (-barEffectiveWidth - 1 + fish_position))
                                    + AdventureHelper.surroundWithMiniMessageFont(pointerImage, font)
                                    + OffsetUtils.getOffsetChars((int) (barEffectiveWidth - fish_position - pointerIconWidth + 1));
                            customFishingHook.getContext().arg(ContextKeys.PROGRESS, progress[(int) ((hold_time / time_requirement) * progress.length)]);
                            SparrowHeart.getInstance().sendTitle(super.getPlayer(), AdventureHelper.miniMessageToJson(tip != null && !played ? tip : title.render(customFishingHook.getContext())), AdventureHelper.miniMessageToJson(bar), 0, 20, 0);
                        }
                    };
                }
            };
        });
    }

    private void registerHoldV2Game() {
        this.registerGameType("hold_v2", (id, section) -> {
            GameBasics basics = getGameBasics(section);
            return new AbstractGame(id, basics) {

                private final int[] timeRequirements = section.getIntList("hold-time-requirements").stream().mapToInt(Integer::intValue).toArray();
                private final String judgementAreaImage = section.getString("subtitle.judgment-area");
                private final String pointerImage = section.getString("subtitle.pointer");
                private final int barEffectiveWidth = section.getInt("arguments.bar-effective-area-width");
                private final int judgementAreaOffset = section.getInt("arguments.judgment-area-offset");
                private final int judgementAreaWidth = section.getInt("arguments.judgment-area-width");
                private final int pointerIconWidth = section.getInt("arguments.pointer-icon-width");
                private final double punishment = section.getDouble("arguments.punishment");
                private final String[] progress = section.getStringList("progress").toArray(new String[0]);
                private final double waterResistance = section.getDouble("arguments.water-resistance", 0.15);
                private final double pullingStrength = section.getDouble("arguments.pulling-strength", 3d);
                private final double looseningLoss = section.getDouble("arguments.loosening-strength-loss", 0.5);
                private final TextValue<Player> title = TextValue.auto(section.getString("title", "{progress}"));
                private final String font = section.getString("subtitle.font");
                private final String barImage = section.getString("subtitle.bar");
                private final String tip = section.getString("tip");
                private final boolean left = section.getBoolean("left-click", false);
                private final boolean elasticity = section.getBoolean("arguments.elasticity", false);
                private final double elasticityPower = section.getDouble("arguments.elasticity-power", 0.7);

                @Override
                public BiFunction<CustomFishingHook, GameSetting, AbstractGamingPlayer> gamingPlayerProvider() {
                    return (customFishingHook, gameSetting) -> new AbstractGamingPlayer(customFishingHook, gameSetting) {
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
                            this.task = plugin.getScheduler().asyncRepeating(this, 50, 33, TimeUnit.MILLISECONDS);
                        }

                        @Override
                        public void tick() {
                            if (timer < 40 - (settings.difficulty() / 10)) {
                                timer++;
                            } else {
                                timer = 0;
                                if (Math.random() > ((double) 25 / (settings.difficulty() + 100))) {
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
                                judgement_velocity = -1 - 0.8 * Math.random() * ((double) settings.difficulty() / 15);
                            } else {
                                judgement_velocity = 1 + 0.8 * Math.random() * ((double) settings.difficulty() / 15);
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
                                if (elasticity) {
                                    fish_velocity = -fish_velocity * elasticityPower;
                                } else {
                                    fish_velocity = 0;
                                }
                            }
                            if (fish_position + pointerIconWidth > barEffectiveWidth) {
                                fish_position = barEffectiveWidth - pointerIconWidth;
                                if (elasticity) {
                                    fish_velocity = -fish_velocity * elasticityPower;
                                } else {
                                    fish_velocity = 0;
                                }
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
                        public void handleRightClick() {
                            if (left) {
                                setGameResult(false);
                                endGame();
                                return;
                            }
                            played = true;
                            fish_velocity = pullingStrength;
                        }

                        @Override
                        public boolean handleLeftClick() {
                            if (left) {
                                played = true;
                                fish_velocity = pullingStrength;
                            }
                            return false;
                        }

                        private void showUI() {
                            String bar = AdventureHelper.surroundWithMiniMessageFont(barImage, font)
                                    + OffsetUtils.getOffsetChars((int) (judgementAreaOffset + judgement_position))
                                    + AdventureHelper.surroundWithMiniMessageFont(judgementAreaImage, font)
                                    + OffsetUtils.getOffsetChars((int) (barEffectiveWidth - judgement_position - judgementAreaWidth))
                                    + OffsetUtils.getOffsetChars((int) (-barEffectiveWidth - 1 + fish_position))
                                    + AdventureHelper.surroundWithMiniMessageFont(pointerImage, font)
                                    + OffsetUtils.getOffsetChars((int) (barEffectiveWidth - fish_position - pointerIconWidth + 1));
                            hook.getContext().arg(ContextKeys.PROGRESS, progress[(int) ((hold_time / time_requirement) * progress.length)]);
                            SparrowHeart.getInstance().sendTitle(getPlayer(), AdventureHelper.miniMessageToJson(tip != null && !played ? tip : title.render(hook.getContext())), AdventureHelper.miniMessageToJson(bar), 0, 20, 0);
                        }
                    };
                }
            };
        });
    }

    private void registerClickGame() {
        this.registerGameType("click", ((id, section) -> {
            GameBasics basics = getGameBasics(section);
            return new AbstractGame(id, basics) {

                private final TextValue<Player> title = TextValue.auto(section.getString("title","<red>{progress}"));
                private final TextValue<Player> subtitle = TextValue.auto(section.getString("subtitle", "<gray>Click <white>{clicks} <gray>times to win. Time left <white>{time_left}s"));
                private final boolean left = section.getBoolean("left-click", false);

                @Override
                public BiFunction<CustomFishingHook, GameSetting, AbstractGamingPlayer> gamingPlayerProvider() {
                    return (customFishingHook, gameSetting) -> new AbstractGamingPlayer(customFishingHook, gameSetting) {
                        private int clickedTimes;
                        private final int requiredTimes = (int) settings.difficulty();

                        @Override
                        public void arrangeTask() {
                            hook.getContext().arg(ContextKeys.REQUIRED_TIMES, requiredTimes);
                            super.arrangeTask();
                        }

                        @Override
                        protected void tick() {
                            showUI();
                        }

                        @Override
                        public void handleRightClick() {
                            if (!left) {
                                handleClicks();
                            }
                        }

                        @Override
                        public boolean handleLeftClick() {
                            if (left) {
                                handleClicks();
                            }
                            return true;
                        }

                        private void handleClicks() {
                            clickedTimes++;
                            if (clickedTimes >= requiredTimes) {
                                showUI();
                                setGameResult(true);
                                endGame();
                            }
                        }

                        private void showUI() {
                            hook.getContext().arg(ContextKeys.CLICKS_LEFT, requiredTimes - clickedTimes);
                            hook.getContext().arg(ContextKeys.REQUIRED_TIMES, requiredTimes);
                            hook.getContext().arg(ContextKeys.PROGRESS, String.valueOf(clickedTimes));
                            SparrowHeart.getInstance().sendTitle(
                                    getPlayer(),
                                    AdventureHelper.miniMessageToJson(title.render(hook.getContext())),
                                    AdventureHelper.miniMessageToJson(subtitle.render(hook.getContext())),
                                    0, 20, 0);
                        }
                    };
                }
            };
        }));
    }

    private void registerTensionGame() {
        this.registerGameType("tension", ((id, section) -> {
            GameBasics basics = getGameBasics(section);
            return new AbstractGame(id, basics) {

                private final int fishIconWidth = section.getInt("arguments.fish-icon-width");
                private final String fishImage = section.getString("subtitle.fish");
                private final String[] tension = section.getStringList("tension").toArray(new String[0]);
                private final String[] strugglingFishImage = section.getStringList("subtitle.struggling-fish").toArray(new String[0]);
                private final int barEffectiveWidth = section.getInt("arguments.bar-effective-area-width");
                private final int fishOffset = section.getInt("arguments.fish-offset");
                private final int fishStartPosition = section.getInt("arguments.fish-start-position");
                private final int successPosition = section.getInt("arguments.success-position");
                private final double ultimateTension = section.getDouble("arguments.ultimate-tension", 50d);
                private final double normalIncrease = section.getDouble("arguments.normal-pull-tension-increase", 1d);
                private final double strugglingIncrease = section.getDouble("arguments.struggling-tension-increase", 2d);
                private final double tensionLoss = section.getDouble("arguments.loosening-tension-loss", 2d);
                private final TextValue<Player> title = TextValue.auto(section.getString("title","{progress}"));
                private final String font = section.getString("subtitle.font");
                private final String barImage = section.getString("subtitle.bar");
                private final String tip = section.getString("tip");

                @Override
                public BiFunction<CustomFishingHook, GameSetting, AbstractGamingPlayer> gamingPlayerProvider() {
                    return (customFishingHook, gameSetting) -> new AbstractGamingPlayer(customFishingHook, gameSetting) {

                        private int fish_position = fishStartPosition;
                        private double strain;
                        private int struggling_time;
                        private boolean played;

                        @Override
                        public void arrangeTask() {
                            this.task = plugin.getScheduler().asyncRepeating(this, 50, 40, TimeUnit.MILLISECONDS);
                        }

                        @Override
                        protected void tick() {
                            if (struggling_time <= 0) {
                                if (Math.random() < (settings.difficulty() / 4000)) {
                                    struggling_time = (int) (10 + Math.random() * (settings.difficulty() / 4));
                                }
                            } else {
                                struggling_time--;
                            }
                            if (getPlayer().isSneaking()) pull();
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

                        private void pull() {
                            played = true;
                            if (struggling_time > 0) {
                                strain += (strugglingIncrease + (settings.difficulty() / 50));
                                fish_position -= 1;
                            } else {
                                strain += normalIncrease;
                                fish_position -= 2;
                            }
                        }

                        private void loosen() {
                            fish_position++;
                            strain -= tensionLoss;
                        }

                        private void showUI() {
                            String bar = AdventureHelper.surroundWithMiniMessageFont(barImage, font)
                                    + OffsetUtils.getOffsetChars(fishOffset + fish_position)
                                    + AdventureHelper.surroundWithMiniMessageFont((struggling_time > 0 ? strugglingFishImage[struggling_time % strugglingFishImage.length] : fishImage), font)
                                    + OffsetUtils.getOffsetChars(barEffectiveWidth - fish_position - fishIconWidth);
                            strain = Math.max(0, Math.min(strain, ultimateTension));
                            hook.getContext().arg(ContextKeys.PROGRESS, tension[(int) ((strain / ultimateTension) * tension.length)]);
                            SparrowHeart.getInstance().sendTitle(getPlayer(), AdventureHelper.miniMessageToJson(tip != null && !played ? tip : title.render(hook.getContext())), AdventureHelper.miniMessageToJson(bar), 0, 20, 0);
                        }
                    };
                }
            };
        }));
    }

    private void registerDanceGame() {
        this.registerGameType("dance", ((id, section) -> {
            GameBasics basics = getGameBasics(section);
            return new AbstractGame(id, basics) {
                private final TextValue<Player> subtitle = TextValue.auto(section.getString("subtitle", "<gray>Dance to win. Time left: <white>{time_left}s"));
                private final String leftNot = section.getString("title.left-button");
                private final String leftCorrect = section.getString("title.left-button-correct");
                private final String leftWrong = section.getString("title.left-button-wrong");
                private final String leftCurrent = section.getString("title.left-button-current");
                private final String rightNot = section.getString("title.right-button");
                private final String rightCorrect = section.getString("title.right-button-correct");
                private final String rightWrong = section.getString("title.right-button-wrong");
                private final String rightCurrent = section.getString("title.right-button-current");
                private final String upNot = section.getString("title.up-button");
                private final String upCorrect = section.getString("title.up-button-correct");
                private final String upWrong = section.getString("title.up-button-wrong");
                private final String upCurrent = section.getString("title.up-button-current");
                private final String downNot = section.getString("title.down-button");
                private final String downCorrect = section.getString("title.down-button-correct");
                private final String downWrong = section.getString("title.down-button-wrong");
                private final String downCurrent = section.getString("title.down-button-current");
                private final int maxShown = section.getInt("title.display-amount", 7);
                private final String tip = section.getString("tip");
                private final boolean easy = section.getBoolean("easy", false);
                private final String correctSound = section.getString("sound.correct", "minecraft:block.amethyst_block.hit");
                private final String wrongSound = section.getString("sound.wrong", "minecraft:block.anvil.land");

                @Override
                public BiFunction<CustomFishingHook, GameSetting, AbstractGamingPlayer> gamingPlayerProvider() {
                    return (customFishingHook, gameSetting) -> new AbstractGamingPlayer(customFishingHook, gameSetting) {

                        private int clickedTimes;
                        private int requiredTimes;
                        // 0 = left / 1 = right / 2 = up / 3 = down
                        private int[] order;
                        boolean fail = false;

                        @Override
                        public void arrangeTask() {
                            requiredTimes = (int) (settings.difficulty() / 4) + 3;
                            order = new int[requiredTimes];
                            for (int i = 0; i < requiredTimes; i++) {
                                order[i] = ThreadLocalRandom.current().nextInt(0, easy ? 2 : 4);
                            }
                            this.task = plugin.getScheduler().asyncRepeating(this, 50, 50, TimeUnit.MILLISECONDS);
                        }

                        @Override
                        protected void tick() {
                            showUI();
                            if (tip != null) {
                                SparrowHeart.getInstance().sendActionBar(getPlayer(), AdventureHelper.miniMessageToJson(tip));
                            }
                        }

                        @Override
                        public boolean handleLeftClick() {
                            if (order[clickedTimes] != 0) {
                                handleWrongAction();
                                return true;
                            }
                            handleCorrectAction();
                            return true;
                        }

                        @Override
                        public void handleRightClick() {
                            if (order[clickedTimes] != 1) {
                                handleWrongAction();
                                return;
                            }
                            handleCorrectAction();
                        }

                        @Override
                        public boolean handleJump() {
                            if (order[clickedTimes] != 2) {
                                handleWrongAction();
                                return false;
                            }
                            handleCorrectAction();
                            return false;
                        }

                        @Override
                        public boolean handleSneak() {
                            if (order[clickedTimes] != 3) {
                                handleWrongAction();
                                return false;
                            }
                            handleCorrectAction();
                            return false;
                        }

                        private void handleCorrectAction() {
                            plugin.getSenderFactory().getAudience(getPlayer()).playSound(Sound.sound(Key.key(correctSound), Sound.Source.PLAYER, 1,1));
                            clickedTimes++;
                            if (clickedTimes >= requiredTimes) {
                                setGameResult(true);
                                showUI();
                                endGame();
                            }
                        }

                        private void handleWrongAction() {
                            setGameResult(false);
                            fail = true;
                            showUI();
                            plugin.getSenderFactory().getAudience(getPlayer()).playSound(Sound.sound(Key.key(wrongSound), Sound.Source.PLAYER, 1,1));
                            endGame();
                        }

                        private void showUI() {
                            try {
                                if (requiredTimes <= maxShown) {
                                    StringBuilder sb = new StringBuilder();
                                    for (int x = 0; x < requiredTimes; x++) {
                                        if (x < clickedTimes) {
                                            switch (order[x]) {
                                                case 0 -> sb.append(leftCorrect);
                                                case 1 -> sb.append(rightCorrect);
                                                case 2 -> sb.append(upCorrect);
                                                case 3 -> sb.append(downCorrect);
                                            }
                                        } else if (clickedTimes == x) {
                                            switch (order[x]) {
                                                case 0 -> sb.append(fail ? leftWrong : leftCurrent);
                                                case 1 -> sb.append(fail ? rightWrong : rightCurrent);
                                                case 2 -> sb.append(fail ? upWrong : upCurrent);
                                                case 3 -> sb.append(fail ? downWrong : downCurrent);
                                            }
                                        } else {
                                            switch (order[x]) {
                                                case 0 -> sb.append(leftNot);
                                                case 1 -> sb.append(rightNot);
                                                case 2 -> sb.append(upNot);
                                                case 3 -> sb.append(downNot);
                                            }
                                        }
                                    }
                                    SparrowHeart.getInstance().sendTitle(getPlayer(), AdventureHelper.miniMessageToJson(sb.toString()), AdventureHelper.miniMessageToJson(subtitle.render(hook.getContext())), 0, 20, 0);
                                } else {
                                    int half = (maxShown - 1) / 2;
                                    int low = clickedTimes - half;
                                    int high = clickedTimes + half;
                                    if (low < 0) {
                                        high += (-low);
                                        low = 0;
                                    } else if (high >= requiredTimes) {
                                        low -= (high - requiredTimes + 1);
                                        high = requiredTimes - 1;
                                    }
                                    StringBuilder sb = new StringBuilder();
                                    for (int x = low; x < high + 1; x++) {
                                        if (x < clickedTimes) {
                                            switch (order[x]) {
                                                case 0 -> sb.append(leftCorrect);
                                                case 1 -> sb.append(rightCorrect);
                                                case 2 -> sb.append(upCorrect);
                                                case 3 -> sb.append(downCorrect);
                                            }
                                        } else if (clickedTimes == x) {
                                            switch (order[x]) {
                                                case 0 -> sb.append(fail ? leftWrong : leftCurrent);
                                                case 1 -> sb.append(fail ? rightWrong : rightCurrent);
                                                case 2 -> sb.append(fail ? upWrong : upCurrent);
                                                case 3 -> sb.append(fail ? downWrong : downCurrent);
                                            }
                                        } else {
                                            switch (order[x]) {
                                                case 0 -> sb.append(leftNot);
                                                case 1 -> sb.append(rightNot);
                                                case 2 -> sb.append(upNot);
                                                case 3 -> sb.append(downNot);
                                            }
                                        }
                                    }
                                    SparrowHeart.getInstance().sendTitle(getPlayer(), AdventureHelper.miniMessageToJson(sb.toString()), AdventureHelper.miniMessageToJson(subtitle.render(hook.getContext())), 0, 20, 0);
                                }
                            } catch (Exception e) {
                                plugin.getPluginLogger().warn("Failed to show `dance` UI", e);
                            }
                        }
                    };
                }
            };
        }));
    }

    private void registerAccurateClickGame() {
        this.registerGameType("accurate_click", ((id, section) -> {
            GameBasics basics = getGameBasics(section);

            Set<String> chances = Objects.requireNonNull(section.getSection("success-rate-sections")).getRoutesAsStrings(false);
            double[] successRate = new double[chances.size()];
            for(int i = 0; i < chances.size(); i++)
                successRate[i] = section.getDouble("success-rate-sections." + (i + 1));

            return new AbstractGame(id, basics) {

                private final int widthPerSection = section.getInt("arguments.width-per-section", 16);
                private final int totalWidth = chances.size() * widthPerSection - 1;
                private final int pointerOffset = section.getInt("arguments.pointer-offset");
                private final int pointerWidth = section.getInt("arguments.pointer-width");
                private final int maxSpeed = section.getInt("arguments.max-speed", 150);
                private final int minSpeed = section.getInt("arguments.min-speed", 15);
                private final List<String> title = ListUtils.toList(section.get("title"));
                private final String font = section.getString("subtitle.font");
                private final String barImage = section.getString("subtitle.bar");
                private final String pointerImage = section.getString("subtitle.pointer");

                @Override
                public BiFunction<CustomFishingHook, GameSetting, AbstractGamingPlayer> gamingPlayerProvider() {
                    return (customFishingHook, gameSetting) -> new AbstractGamingPlayer(customFishingHook, gameSetting) {

                        private int progress = -1;
                        private boolean face = true;
                        private final TextValue<Player> sendTitle = TextValue.auto(title.get(RandomUtils.generateRandomInt(0, title.size() - 1)));

                        private static final int MIN_VALUE = 1;
                        private static final int MAX_VALUE = 100;

                        private long mapValueToIntervalMicroseconds(int value) {
                            double frequency = minSpeed + ((double) (value - MIN_VALUE) / (MAX_VALUE - MIN_VALUE)) * (maxSpeed - minSpeed);
                            return (long) (1_000_000 / frequency);
                        }

                        @Override
                        public void arrangeTask() {
                            long period = mapValueToIntervalMicroseconds((int) settings.difficulty());
                            this.task = plugin.getScheduler().asyncRepeating(this, period, period, TimeUnit.MICROSECONDS);
                        }

                        @Override
                        protected void tick() {
                            if (face) progress++;
                            else progress--;
                            if (progress > totalWidth) {
                                face = !face;
                                progress = 2 * totalWidth - progress;
                            } else if (progress < 0) {
                                face = !face;
                                progress = -progress;
                            }
                            if (!isValid()) return;
                            showUI();
                        }

                        @Override
                        public boolean isSuccessful() {
                            if (isTimeOut) return false;
                            int last = progress / widthPerSection;
                            return (Math.random() < successRate[last]);
                        }

                        private void showUI() {
                            String bar = AdventureHelper.surroundWithMiniMessageFont(barImage, font)
                                    + OffsetUtils.getOffsetChars(pointerOffset + progress)
                                    + AdventureHelper.surroundWithMiniMessageFont(pointerImage, font)
                                    + OffsetUtils.getOffsetChars(totalWidth - progress - pointerWidth);
                            SparrowHeart.getInstance().sendTitle(getPlayer(), AdventureHelper.miniMessageToJson(sendTitle.render(hook.getContext())), AdventureHelper.miniMessageToJson(bar), 0, 10, 0);
                        }
                    };
                }
            };
        }));
    }

    private void registerAccurateClickV2Game() {
        this.registerGameType("accurate_click_v2", ((id, section) -> {
            GameBasics basics = getGameBasics(section);
            return new AbstractGame(id, basics) {

                private final String barWidth = section.getString("title.total-width", "15~20");
                private final String barSuccess = section.getString("title.success-width","3~4");
                private final String barBody = section.getString("title.body","");
                private final String left = section.getString("title.left","");
                private final String right = section.getString("title.right","");
                private final String barPointer = section.getString("title.pointer", "");
                private final String barTarget = section.getString("title.target","");
                private final String subtitle = section.getString("subtitle", "<gray>Reel in at the most critical moment</gray>");

                @Override
                public BiFunction<CustomFishingHook, GameSetting, AbstractGamingPlayer> gamingPlayerProvider() {

                    int minWidth = Integer.parseInt(barWidth.split("~")[0]);
                    int maxWidth = Integer.parseInt(barWidth.split("~")[1]);
                    int minSuccess = Integer.parseInt(barSuccess.split("~")[0]);
                    int maxSuccess = Integer.parseInt(barSuccess.split("~")[1]);

                    return (customFishingHook, gameSetting) -> new AbstractGamingPlayer(customFishingHook, gameSetting) {

                        private final int totalWidth = RandomUtils.generateRandomInt(minWidth, maxWidth);
                        private final int successWidth = RandomUtils.generateRandomInt(minSuccess, maxSuccess);
                        private final int successPosition = ThreadLocalRandom.current().nextInt((totalWidth - successWidth + 1)) + 1;
                        private int currentIndex = 0;
                        private int timer = 0;
                        private boolean face = true;

                        @Override
                        protected void tick() {
                            timer++;
                            if (timer % ((106 - (int) settings.difficulty()) / 5) == 0) {
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

                        private void showUI() {
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

                            SparrowHeart.getInstance().sendTitle(getPlayer(), AdventureHelper.miniMessageToJson(left + stringBuilder + right), AdventureHelper.miniMessageToJson(subtitle), 0, 20, 0);
                        }

                        @Override
                        public boolean isSuccessful() {
                            if (isTimeOut) return false;
                            return currentIndex + 1 <= successPosition + successWidth - 1 && currentIndex + 1 >= successPosition;
                        }
                    };
                }
            };
        }));
    }

    private void registerAccurateClickV3Game() {
        this.registerGameType("accurate_click_v3", ((id, section) -> {
            GameBasics basics = getGameBasics(section);
            return new AbstractGame(id, basics) {

                private final String font = section.getString("subtitle.font");
                private final String pointerImage = section.getString("subtitle.pointer");
                private final String barImage = section.getString("subtitle.bar");
                private final String judgementAreaImage = section.getString("subtitle.judgment-area");
                private final List<String> titles = ListUtils.toList(section.get("title"));
                private final int barEffectiveWidth = section.getInt("arguments.bar-effective-area-width");
                private final int judgementAreaWidth = section.getInt("arguments.judgment-area-width");
                private final int judgementAreaOffset = section.getInt("arguments.judgment-area-offset");
                private final int pointerIconWidth = section.getInt("arguments.pointer-icon-width");
                private final int pointerOffset = section.getInt("arguments.pointer-offset");
                private final int maxSpeed = section.getInt("arguments.max-speed", 150);
                private final int minSpeed = section.getInt("arguments.min-speed", 15);

                @Override
                public BiFunction<CustomFishingHook, GameSetting, AbstractGamingPlayer> gamingPlayerProvider() {
                    return (customFishingHook, gameSetting) -> new AbstractGamingPlayer(customFishingHook, gameSetting) {

                        private static final int MIN_VALUE = 1;
                        private static final int MAX_VALUE = 100;

                        private int progress = -1;
                        private boolean face = true;
                        private final int judgement_position = RandomUtils.generateRandomInt(0, barEffectiveWidth - judgementAreaWidth);
                        private final TextValue<Player> title = TextValue.auto(titles.get(RandomUtils.generateRandomInt(0, titles.size() - 1)));

                        private long mapValueToIntervalMicroseconds(int value) {
                            double frequency = minSpeed + ((double) (value - MIN_VALUE) / (MAX_VALUE - MIN_VALUE)) * (maxSpeed - minSpeed);
                            return (long) (1_000_000 / frequency);
                        }

                        @Override
                        public void arrangeTask() {
                            long period = mapValueToIntervalMicroseconds((int) settings.difficulty());
                            this.task = plugin.getScheduler().asyncRepeating(this, period, period, TimeUnit.MICROSECONDS);
                        }

                        @Override
                        protected void tick() {
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

                        private void showUI() {
                            String bar = AdventureHelper.surroundWithMiniMessageFont(barImage, font)
                                    + OffsetUtils.getOffsetChars(judgementAreaOffset + judgement_position)
                                    + AdventureHelper.surroundWithMiniMessageFont(judgementAreaImage, font)
                                    + OffsetUtils.getOffsetChars(barEffectiveWidth - judgement_position - judgementAreaWidth)
                                    + OffsetUtils.getOffsetChars(progress + pointerOffset)
                                    + AdventureHelper.surroundWithMiniMessageFont(pointerImage, font)
                                    + OffsetUtils.getOffsetChars(barEffectiveWidth - progress - pointerIconWidth + 1);
                            SparrowHeart.getInstance().sendTitle(getPlayer(), AdventureHelper.miniMessageToJson(title.render(hook.getContext())), AdventureHelper.miniMessageToJson(bar), 0, 20, 0);
                        }

                        @Override
                        public boolean isSuccessful() {
                            if (isTimeOut) return false;
                            return progress < judgement_position + judgementAreaWidth && progress >= judgement_position;
                        }
                    };
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
                    plugin.getPluginLogger().warn("Failed to load expansion: " + expansionJar.getName(), e);
                }
            }
        }
        try {
            for (Class<? extends GameExpansion> expansionClass : classes) {
                GameExpansion expansion = expansionClass.getDeclaredConstructor().newInstance();
                unregisterGameType(expansion.getGameType());
                registerGameType(expansion.getGameType(), expansion.getGameFactory());
                plugin.getPluginLogger().info("Loaded minigame expansion: " + expansion.getGameType() + "[" + expansion.getVersion() + "]" + " by " + expansion.getAuthor() );
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            plugin.getPluginLogger().warn("Error occurred when creating expansion instance.", e);
        }
    }
}
