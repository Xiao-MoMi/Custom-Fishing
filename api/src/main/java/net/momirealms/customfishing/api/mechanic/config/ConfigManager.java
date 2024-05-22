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
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.common.config.ConfigLoader;
import net.momirealms.customfishing.common.config.node.Node;
import net.momirealms.customfishing.common.item.Item;
import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class ConfigManager implements ConfigLoader, Reloadable {

    protected final BukkitCustomFishingPlugin plugin;

    protected final HashMap<String, Node<ConfigParserFunction>> formatFunctions = new HashMap<>();

    public ConfigManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    public static int placeholderLimit() {
        return 0;
    }

    public static boolean redisRanking() {
        return false;
    }

    public static String serverGroup() {
        return null;
    }

    public static String[] itemDetectOrder() {
        return new String[0];
    }

    public static String[] blockDetectOrder() {
        return new String[0];
    }

    public static boolean enableFishingBag() {
        return true;
    }

    public static int dataSaveInterval() {
        return 360;
    }

    public static boolean logDataSaving() {
        return true;
    }

    public static boolean lockData() {
        return false;
    }

    public void registerLootParser(Function<Object, Consumer<Loot.Builder>> function, String... nodes) {
        registerNodeFunction(nodes, new LootParserFunction(function));
    }

    public void registerBaseEffectParser(Function<Object, Consumer<LootBaseEffect.Builder>> function, String... nodes) {
        registerNodeFunction(nodes, new BaseEffectParserFunction(function));
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
        try {
            return YamlDocument.create(
                    resolveConfig(filePath).toFile(),
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
        try {
            return YamlDocument.create(file);
        } catch (IOException e) {
            plugin.getPluginLogger().severe("Failed to load config " + file, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public YamlDocument loadData(File file, char routeSeparator) {
        try {
            return YamlDocument.create(file, GeneralSettings.builder().setRouteSeparator(routeSeparator).build());
        } catch (IOException e) {
            plugin.getPluginLogger().severe("Failed to load config " + file, e);
            throw new RuntimeException(e);
        }
    }

    public Map<String, Node<ConfigParserFunction>> getFormatFunctions() {
        return formatFunctions;
    }
}
