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

package net.momirealms.customfishing.api.mechanic.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.config.function.*;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;
import net.momirealms.customfishing.api.mechanic.effect.LootBaseEffect;
import net.momirealms.customfishing.api.mechanic.entity.EntityConfig;
import net.momirealms.customfishing.api.mechanic.event.EventCarrier;
import net.momirealms.customfishing.api.mechanic.hook.HookConfig;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.api.mechanic.totem.TotemConfig;
import net.momirealms.customfishing.common.config.ConfigLoader;
import net.momirealms.customfishing.common.config.node.Node;
import net.momirealms.customfishing.common.item.Item;
import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import net.momirealms.customfishing.common.util.Pair;
import net.momirealms.customfishing.common.util.TriConsumer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class ConfigManager implements ConfigLoader, Reloadable {

    private static ConfigManager instance;
    protected final BukkitCustomFishingPlugin plugin;
    protected final HashMap<String, Node<ConfigParserFunction>> formatFunctions = new HashMap<>();
    protected int placeholderLimit;
    protected boolean redisRanking;
    protected String serverGroup;
    protected String[] itemDetectOrder = new String[0];
    protected String[] blockDetectOrder = new String[0];
    protected int dataSaveInterval;
    protected boolean logDataSaving;
    protected boolean lockData;
    protected boolean metrics;
    protected boolean checkUpdate;
    protected boolean debug;
    protected boolean overrideVanillaWaitTime;
    protected int waterMinTime;
    protected int waterMaxTime;
    protected boolean enableLavaFishing;
    protected int lavaMinTime;
    protected int lavaMaxTime;
    protected boolean enableVoidFishing;
    protected int voidMinTime;
    protected int voidMaxTime;
    protected int multipleLootSpawnDelay;
    protected boolean restrictedSizeRange;
    protected List<String> durabilityLore;
    protected boolean allowMultipleTotemType;
    protected boolean allowSameTotemType;
    protected EventPriority eventPriority;
    protected Requirement<Player>[] mechanicRequirements;
    protected Requirement<Player>[] skipGameRequirements;
    protected Requirement<Player>[] autoFishingRequirements;
    protected boolean enableBag;
    protected boolean baitAnimation;
    protected List<TriConsumer<Effect, Context<Player>, Integer>> globalEffects;

    protected ConfigManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static boolean debug() {
        return instance.debug;
    }

    public static int placeholderLimit() {
        return instance.placeholderLimit;
    }

    public static boolean redisRanking() {
        return instance.redisRanking;
    }

    public static String serverGroup() {
        return instance.serverGroup;
    }

    public static String[] itemDetectOrder() {
        return instance.itemDetectOrder;
    }

    public static String[] blockDetectOrder() {
        return instance.blockDetectOrder;
    }

    public static int dataSaveInterval() {
        return instance.dataSaveInterval;
    }

    public static boolean logDataSaving() {
        return instance.logDataSaving;
    }

    public static boolean lockData() {
        return instance.lockData;
    }

    public static boolean metrics() {
        return instance.metrics;
    }

    public static boolean checkUpdate() {
        return instance.checkUpdate;
    }

    public static boolean overrideVanillaWaitTime() {
        return instance.overrideVanillaWaitTime;
    }

    public static int waterMinTime() {
        return instance.waterMinTime;
    }

    public static int waterMaxTime() {
        return instance.waterMaxTime;
    }

    public static boolean enableLavaFishing() {
        return instance.enableLavaFishing;
    }

    public static int lavaMinTime() {
        return instance.lavaMinTime;
    }

    public static int lavaMaxTime() {
        return instance.lavaMaxTime;
    }

    public static boolean enableVoidFishing() {
        return instance.enableVoidFishing;
    }

    public static int voidMinTime() {
        return instance.voidMinTime;
    }

    public static int voidMaxTime() {
        return instance.voidMaxTime;
    }

    public static int multipleLootSpawnDelay() {
        return instance.multipleLootSpawnDelay;
    }

    public static boolean restrictedSizeRange() {
        return instance.restrictedSizeRange;
    }

    public static boolean allowMultipleTotemType() {
        return instance.allowMultipleTotemType;
    }

    public static boolean allowSameTotemType() {
        return instance.allowSameTotemType;
    }

    public static boolean enableBag() {
        return instance.enableBag;
    }

    public static boolean baitAnimation() {
        return instance.baitAnimation;
    }

    public static List<String> durabilityLore() {
        return instance.durabilityLore;
    }

    public static EventPriority eventPriority() {
        return instance.eventPriority;
    }

    public static Requirement<Player>[] mechanicRequirements() {
        return instance.mechanicRequirements;
    }

    public static Requirement<Player>[] autoFishingRequirements() {
        return instance.autoFishingRequirements;
    }

    public static Requirement<Player>[] skipGameRequirements() {
        return instance.skipGameRequirements;
    }

    public static List<TriConsumer<Effect, Context<Player>, Integer>> globalEffects() {
        return instance.globalEffects;
    }

    public void registerHookParser(Function<Object, Consumer<HookConfig.Builder>> function, String... nodes) {
        registerNodeFunction(nodes, new HookParserFunction(function));
    }

    public void registerTotemParser(Function<Object, Consumer<TotemConfig.Builder>> function, String... nodes) {
        registerNodeFunction(nodes, new TotemParserFunction(function));
    }

    public void registerLootParser(Function<Object, Consumer<Loot.Builder>> function, String... nodes) {
        registerNodeFunction(nodes, new LootParserFunction(function));
    }

    public void registerItemParser(Function<Object, BiConsumer<Item<ItemStack>, Context<Player>>> function, int priority, String... nodes) {
        registerNodeFunction(nodes, new ItemParserFunction(priority, function));
    }

    public void registerEffectModifierParser(Function<Object, Consumer<EffectModifier.Builder>> function, String... nodes) {
        registerNodeFunction(nodes, new EffectModifierParserFunction(function));
    }

    public void registerEntityParser(Function<Object, Consumer<EntityConfig.Builder>> function, String... nodes) {
        registerNodeFunction(nodes, new EntityParserFunction(function));
    }

    public void registerEventParser(Function<Object, Consumer<EventCarrier.Builder>> function, String... nodes) {
        registerNodeFunction(nodes, new EventParserFunction(function));
    }

    public void registerBaseEffectParser(Function<Object, Consumer<LootBaseEffect.Builder>> function, String... nodes) {
        registerNodeFunction(nodes, new BaseEffectParserFunction(function));
    }

    public void unregisterNodeFunction(String... nodes) {
        Map<String, Node<ConfigParserFunction>> functionMap = formatFunctions;
        for (int i = 0; i < nodes.length; i++) {
            if (functionMap.containsKey(nodes[i])) {
                Node<ConfigParserFunction> functionNode = functionMap.get(nodes[i]);
                if (i != nodes.length - 1) {
                   if (functionNode.nodeValue() != null) {
                       return;
                   } else {
                       functionMap = functionNode.getChildTree();
                   }
                } else {
                    if (functionNode.nodeValue() != null) {
                        functionMap.remove(nodes[i]);
                    }
                }
            }
        }
    }

    public void registerNodeFunction(String[] nodes, ConfigParserFunction configParserFunction) {
        Map<String, Node<ConfigParserFunction>> functionMap = formatFunctions;
        for (int i = 0; i < nodes.length; i++) {
            if (functionMap.containsKey(nodes[i])) {
                Node<ConfigParserFunction> functionNode = functionMap.get(nodes[i]);
                if (functionNode.nodeValue() != null) {
                    throw new IllegalArgumentException("Format function '" + nodes[i] + "' already exists");
                }
                functionMap = functionNode.getChildTree();
            } else {
                if (i != nodes.length - 1) {
                    Node<ConfigParserFunction> newNode = new Node<>();
                    functionMap.put(nodes[i], newNode);
                    functionMap = newNode.getChildTree();
                } else {
                    functionMap.put(nodes[i], new Node<>(configParserFunction));
                }
            }
        }
    }

    protected Path resolveConfig(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
        filePath = filePath.replace('\\', '/');
        Path configFile = plugin.getConfigDirectory().resolve(filePath);
        // if the config doesn't exist, create it based on the template in the resources dir
        if (!Files.exists(configFile)) {
            try {
                Files.createDirectories(configFile.getParent());
            } catch (IOException e) {
                // ignore
            }
            try (InputStream is = plugin.getResourceStream(filePath)) {
                if (is == null) {
                    throw new IllegalArgumentException("The embedded resource '" + filePath + "' cannot be found");
                }
                Files.copy(is, configFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return configFile;
    }

    @Override
    public YamlDocument loadConfig(String filePath) {
        return loadConfig(filePath, '.');
    }

    @Override
    public YamlDocument loadConfig(String filePath, char routeSeparator) {
        try (InputStream inputStream = new FileInputStream(resolveConfig(filePath).toFile())) {
            return YamlDocument.create(
                    inputStream,
                    plugin.getResourceStream(filePath),
                    GeneralSettings.builder().setRouteSeparator(routeSeparator).build(),
                    LoaderSettings
                            .builder()
                            .setAutoUpdate(true)
                            .build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings
                            .builder()
                            .setVersioning(new BasicVersioning("config-version"))
                            .build()
            );
        } catch (IOException e) {
            plugin.getPluginLogger().severe("Failed to load config " + filePath, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public YamlDocument loadData(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            return YamlDocument.create(inputStream);
        } catch (IOException e) {
            plugin.getPluginLogger().severe("Failed to load config " + file, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public YamlDocument loadData(File file, char routeSeparator) {
        try (InputStream inputStream = new FileInputStream(file)) {
            return YamlDocument.create(inputStream, GeneralSettings.builder().setRouteSeparator(routeSeparator).build());
        } catch (IOException e) {
            plugin.getPluginLogger().severe("Failed to load config " + file, e);
            throw new RuntimeException(e);
        }
    }

    public Map<String, Node<ConfigParserFunction>> getFormatFunctions() {
        return formatFunctions;
    }

    public abstract List<Pair<String, BiFunction<Context<Player>, Double, Double>>> parseWeightOperation(List<String> ops);

    public abstract List<Pair<String, BiFunction<Context<Player>, Double, Double>>> parseGroupWeightOperation(List<String> gops);
}
