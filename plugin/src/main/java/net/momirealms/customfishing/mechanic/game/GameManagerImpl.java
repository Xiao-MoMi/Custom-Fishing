package net.momirealms.customfishing.mechanic.game;

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.manager.GameManager;
import net.momirealms.customfishing.api.mechanic.game.*;
import net.momirealms.customfishing.api.util.FontUtils;
import net.momirealms.customfishing.api.util.OffsetUtils;
import net.momirealms.customfishing.util.ConfigUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class GameManagerImpl implements GameManager {

    private final CustomFishingPlugin plugin;
    private final HashMap<String, GameCreator> gameCreatorMap;
    private final HashMap<String, Game> gameMap;
    private final HashMap<String, GameConfig> gameConfigMap;

    public GameManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.gameCreatorMap = new HashMap<>();
        this.gameMap = new HashMap<>();
        this.gameConfigMap = new HashMap<>();
        this.registerInbuiltGames();
    }

    private void registerInbuiltGames() {
        this.registerAccurateClickGame();
    }

    public void load() {
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
    public boolean registerGameType(String type, GameCreator gameCreator) {
        if (gameCreatorMap.containsKey(type))
            return false;
        else
            gameCreatorMap.put(type, gameCreator);
        return true;
    }

    @Override
    public boolean unregisterGameType(String type) {
        return gameCreatorMap.remove(type) != null;
    }

    @Override
    @Nullable
    public GameCreator getGameCreator(String type) {
        return gameCreatorMap.get(type);
    }

    @Override
    @Nullable
    public Game getGame(String key) {
        return gameMap.get(key);
    }

    @Override
    @Nullable
    public GameConfig getGameConfig(String key) {
        return gameConfigMap.get(key);
    }

    @Override
    public Game getRandomGame() {
        Collection<Game> collection = gameMap.values();
        return (Game) collection.toArray()[ThreadLocalRandom.current().nextInt(collection.size())];
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
                } else if (subFile.isFile()) {
                    loadSingleFile(subFile);
                }
            }
        }
    }

    private void loadSingleFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map.Entry<String, Object> entry : config.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection section) {
                GameCreator creator = this.getGameCreator(section.getString("game-type"));
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

            return (player, settings, manager) -> new AbstractGamingPlayer(player, settings, manager) {

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
                public boolean isSucceeded() {
                    int last = progress / widthPerSection;
                    return (Math.random() < successRate[last]);
                }
            };
        }));
    }
}
