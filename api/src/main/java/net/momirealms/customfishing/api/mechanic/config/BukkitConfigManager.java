package net.momirealms.customfishing.api.mechanic.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.misc.function.FormatFunction;
import net.momirealms.customfishing.api.mechanic.misc.function.ItemPropertyFunction;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.api.mechanic.misc.value.TextValue;
import net.momirealms.customfishing.common.config.ConfigManager;
import net.momirealms.customfishing.common.config.node.Node;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.item.Item;
import net.momirealms.customfishing.common.plugin.CustomFishingPlugin;
import net.momirealms.customfishing.common.util.ListUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class BukkitConfigManager implements ConfigManager {

    private final CustomFishingPlugin plugin;

    private final HashMap<String, Node<FormatFunction>> formatFunctions = new HashMap<>();

    public BukkitConfigManager(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.registerBuiltInItemProperties();
    }

    private void registerBuiltInItemProperties() {
        this.registerItemFunction(arg -> {
            MathValue<Player> mathValue = MathValue.auto(arg);
            return (item, context) -> item.customModelData((int) mathValue.evaluate(context));
        }, 4000, "custom-model-data");
        this.registerItemFunction(arg -> {
            TextValue<Player> textValue = TextValue.auto((String) arg);
            return (item, context) -> {
                item.displayName(AdventureHelper.miniMessageToJson(textValue.render(context)));
            };
        }, 3000, "display", "name");
        this.registerItemFunction(arg -> {
            List<String> list = ListUtils.toList(arg);
            List<TextValue<Player>> lore = new ArrayList<>();
            for (String text : list) {
                lore.add(TextValue.auto(text));
            }
            return (item, context) -> {
                item.lore(lore.stream()
                        .map(it -> AdventureHelper.miniMessageToJson(it.render(context)))
                        .toList());
            };
        }, 2000, "display", "lore");
        this.registerItemFunction(arg -> {
            boolean enable = (boolean) arg;
            return (item, context) -> {
                if (!enable) return;
                item.setTag(context.arg(ContextKeys.ID), "CustomFishing", "id");
                item.setTag(context.arg(ContextKeys.TYPE), "CustomFishing", "type");
            };
        }, 1000, "tag");
    }

    private void registerItemFunction(Function<Object, BiConsumer<Item<ItemStack>, Context<Player>>> function, int priority, String... nodes) {
        registerNodeFunction(nodes, new ItemPropertyFunction(priority, function));
    }

    public void registerNodeFunction(String[] nodes, FormatFunction formatFunction) {
        Map<String, Node<FormatFunction>> functionMap = formatFunctions;
        for (int i = 0; i < nodes.length; i++) {
            if (functionMap.containsKey(nodes[i])) {
                Node<FormatFunction> functionNode = functionMap.get(nodes[i]);
                if (functionNode.nodeValue() != null) {
                    throw new IllegalArgumentException("Format function '" + nodes[i] + "' already exists");
                }
                functionMap = functionNode.getChildTree();
            } else {
                if (i != nodes.length - 1) {
                    Node<FormatFunction> newNode = new Node<>();
                    functionMap.put(nodes[i], newNode);
                    functionMap = newNode.getChildTree();
                } else {
                    functionMap.put(nodes[i], new Node<>(formatFunction));
                }
            }
        }
    }

    protected Path resolveConfig(String filePath) {
        if (filePath == null || filePath.equals("")) {
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
            return null;
        }
    }
}
