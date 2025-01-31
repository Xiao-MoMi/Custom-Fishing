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

package net.momirealms.customfishing.bukkit.config;

import com.saicone.rtag.RtagItem;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.common.ScalarStyle;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.ConstructorException;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Tag;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.utils.format.NodeRole;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.MechanicType;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionManager;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.block.BlockDataModifier;
import net.momirealms.customfishing.api.mechanic.block.BlockDataModifierFactory;
import net.momirealms.customfishing.api.mechanic.block.BlockStateModifier;
import net.momirealms.customfishing.api.mechanic.block.BlockStateModifierFactory;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.config.ConfigType;
import net.momirealms.customfishing.api.mechanic.config.function.ConfigParserFunction;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.effect.EffectProperties;
import net.momirealms.customfishing.api.mechanic.event.EventManager;
import net.momirealms.customfishing.api.mechanic.item.ItemEditor;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.loot.operation.*;
import net.momirealms.customfishing.api.mechanic.misc.placeholder.BukkitPlaceholderManager;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.api.mechanic.misc.value.TextValue;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementManager;
import net.momirealms.customfishing.api.mechanic.statistic.StatisticsKeys;
import net.momirealms.customfishing.api.mechanic.totem.TotemModel;
import net.momirealms.customfishing.api.mechanic.totem.TotemParticle;
import net.momirealms.customfishing.api.mechanic.totem.block.TotemBlock;
import net.momirealms.customfishing.api.mechanic.totem.block.property.AxisImpl;
import net.momirealms.customfishing.api.mechanic.totem.block.property.FaceImpl;
import net.momirealms.customfishing.api.mechanic.totem.block.property.HalfImpl;
import net.momirealms.customfishing.api.mechanic.totem.block.property.TotemBlockProperty;
import net.momirealms.customfishing.api.mechanic.totem.block.type.TypeCondition;
import net.momirealms.customfishing.api.util.OffsetUtils;
import net.momirealms.customfishing.bukkit.item.damage.CustomDurabilityItem;
import net.momirealms.customfishing.bukkit.totem.particle.DustParticleSetting;
import net.momirealms.customfishing.bukkit.totem.particle.ParticleSetting;
import net.momirealms.customfishing.bukkit.util.ItemStackUtils;
import net.momirealms.customfishing.bukkit.util.ParticleUtils;
import net.momirealms.customfishing.common.config.node.Node;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.helper.ExpressionHelper;
import net.momirealms.customfishing.common.helper.VersionHelper;
import net.momirealms.customfishing.common.item.AbstractItem;
import net.momirealms.customfishing.common.item.Item;
import net.momirealms.customfishing.common.locale.TranslationManager;
import net.momirealms.customfishing.common.plugin.CustomFishingProperties;
import net.momirealms.customfishing.common.util.*;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class BukkitConfigManager extends ConfigManager {

    private static YamlDocument MAIN_CONFIG;
    private static Particle dustParticle;
    public static YamlDocument getMainConfig() {
        return MAIN_CONFIG;
    }

    private Function<String, Boolean> lootValidator = (id) -> {
        return plugin.getLootManager().getLoot(id).isPresent();
    };

    public BukkitConfigManager(BukkitCustomFishingPlugin plugin) {
        super(plugin);
        this.registerBuiltInItemProperties();
        this.registerBuiltInBaseEffectParser();
        this.registerBuiltInLootParser();
        this.registerBuiltInEntityParser();
        this.registerBuiltInEventParser();
        this.registerBuiltInEffectModifierParser();
        this.registerBuiltInTotemParser();
        this.registerBuiltInHookParser();
        this.registerBuiltInBlockParser();
        dustParticle = VersionHelper.isVersionNewerThan1_20_5() ? Particle.valueOf("DUST") : Particle.valueOf("REDSTONE");
    }

    @Override
    public void load() {
        String configVersion = CustomFishingProperties.getValue("config");
        try (InputStream inputStream = new FileInputStream(resolveConfig("config.yml").toFile())) {
            MAIN_CONFIG = YamlDocument.create(
                    inputStream,
                    plugin.getResourceStream("config.yml"),
                    GeneralSettings.builder()
                            .setRouteSeparator('.')
                            .setUseDefaults(false)
                            .build(),
                    LoaderSettings
                            .builder()
                            .setAutoUpdate(true)
                            .build(),
                    DumperSettings.builder()
                            .setScalarFormatter((tag, value, role, def) -> {
                                if (role == NodeRole.KEY) {
                                    return ScalarStyle.PLAIN;
                                } else {
                                    return tag == Tag.STR ? ScalarStyle.DOUBLE_QUOTED : ScalarStyle.PLAIN;
                                }
                            })
                            .build(),
                    UpdaterSettings
                            .builder()
                            .setVersioning(new BasicVersioning("config-version"))
                            .addIgnoredRoute(configVersion, "mechanics.mechanic-requirements", '.')
                            .addIgnoredRoute(configVersion, "mechanics.skip-game-requirements", '.')
                            .addIgnoredRoute(configVersion, "mechanics.auto-fishing-requirements", '.')
                            .addIgnoredRoute(configVersion, "mechanics.global-events", '.')
                            .addIgnoredRoute(configVersion, "mechanics.global-effects", '.')
                            .addIgnoredRoute(configVersion, "mechanics.fishing-bag.collect-requirements", '.')
                            .addIgnoredRoute(configVersion, "mechanics.fishing-bag.collect-actions", '.')
                            .addIgnoredRoute(configVersion, "mechanics.fishing-bag.full-actions", '.')
                            .addIgnoredRoute(configVersion, "mechanics.market.item-price", '.')
                            .addIgnoredRoute(configVersion, "mechanics.market.sell-all-icons", '.')
                            .addIgnoredRoute(configVersion, "mechanics.market.sell-icons", '.')
                            .addIgnoredRoute(configVersion, "mechanics.market.decorative-icons", '.')
                            .addIgnoredRoute(configVersion, "mechanics.market.titles", '.')
                            .addIgnoredRoute(configVersion, "other-settings.placeholder-register", '.')
                            .build()
            );
            MAIN_CONFIG.save(resolveConfig("config.yml").toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.loadSettings();
        this.loadConfigs();
        this.loadGlobalEffects();
    }

    private void loadGlobalEffects() {
        YamlDocument config = getMainConfig();
        globalEffects = new ArrayList<>();
        Section globalEffectSection = config.getSection("mechanics.global-effects");
        if (globalEffectSection != null) {
            for (Map.Entry<String, Object> entry : globalEffectSection.getStringRouteMappedValues(false).entrySet()) {
                if (entry.getValue() instanceof Section innerSection) {
                    globalEffects.add(parseEffect(innerSection, id -> plugin.getLootManager().getGroupMembers(id)));
                }
            }
        }
    }

    private void loadSettings() {
        YamlDocument config = getMainConfig();

        metrics = config.getBoolean("metrics", true);
        checkUpdate = config.getBoolean("update-checker", true);
        debug = config.getBoolean("debug", false);

        overrideVanillaWaitTime = config.getBoolean("mechanics.fishing-wait-time.override-vanilla", false);
        waterMinTime = config.getInt("mechanics.fishing-wait-time.min-wait-time", 100);
        waterMaxTime = config.getInt("mechanics.fishing-wait-time.max-wait-time", 600);
        finalWaterMinTime = config.getInt("mechanics.fishing-wait-time.final-min-wait-time", 50);
        finalWaterMaxTime = config.getInt("mechanics.fishing-wait-time.final-max-wait-time", 1200);

        enableLavaFishing = config.getBoolean("mechanics.lava-fishing.enable", false);
        lavaMinTime = config.getInt("mechanics.lava-fishing.min-wait-time", 100);
        lavaMaxTime = config.getInt("mechanics.lava-fishing.max-wait-time", 600);
        finalLavaMinTime = config.getInt("mechanics.lava-fishing.final-min-wait-time", 50);
        finalLavaMaxTime = config.getInt("mechanics.lava-fishing.final-max-wait-time", 1200);

        enableVoidFishing = config.getBoolean("mechanics.void-fishing.enable", false);
        voidMinTime = config.getInt("mechanics.void-fishing.min-wait-time", 100);
        voidMaxTime = config.getInt("mechanics.void-fishing.max-wait-time", 600);
        finalVoidMinTime = config.getInt("mechanics.void-fishing.final-min-wait-time", 50);
        finalVoidMaxTime = config.getInt("mechanics.void-fishing.final-max-wait-time", 1200);

        restrictedSizeRange = config.getBoolean("mechanics.size.restricted-size-range", true);

        placeholderLimit = config.getInt("mechanics.competition.placeholder-limit", 3);
        serverGroup = config.getString("mechanics.competition.server-group", "default");
        redisRanking = config.getBoolean("mechanics.competition.redis-ranking", false);

        AdventureHelper.legacySupport = config.getBoolean("other-settings.legacy-color-code-support", true);
        dataSaveInterval = config.getInt("other-settings.data-saving-interval", 600);
        logDataSaving = config.getBoolean("other-settings.log-data-saving", true);
        lockData = config.getBoolean("other-settings.lock-data", true);

        durabilityLore = new ArrayList<>(config.getStringList("other-settings.custom-durability-format").stream().map(it -> "<!i>" + it).toList());

        itemDetectOrder = config.getStringList("other-settings.item-detection-order").toArray(new String[0]);
        blockDetectOrder = config.getStringList("other-settings.block-detection-order").toArray(new String[0]);

        allowMultipleTotemType = config.getBoolean("mechanics.totem.allow-multiple-type", true);
        allowSameTotemType = config.getBoolean("mechanics.totem.allow-same-type", false);

        eventPriority = EventPriority.valueOf(config.getString("other-settings.event-priority", "NORMAL").toUpperCase(Locale.ENGLISH));

        antiAutoFishingMod = config.getBoolean("other-settings.anti-auto-fishing-mod", false);

        mechanicRequirements = plugin.getRequirementManager().parseRequirements(config.getSection("mechanics.mechanic-requirements"), true);
        skipGameRequirements = plugin.getRequirementManager().parseRequirements(config.getSection("mechanics.skip-game-requirements"), true);
        autoFishingRequirements = plugin.getRequirementManager().parseRequirements(config.getSection("mechanics.auto-fishing-requirements"), true);

        enableBag = config.getBoolean("mechanics.fishing-bag.enable", true);

        baitAnimation = config.getBoolean("mechanics.bait-animation", true);

        multipleLootSpawnDelay = config.getInt("mechanics.multiple-loot-spawn-delay", 4);

        Loot.DefaultProperties.DEFAULT_DISABLE_GAME = config.getBoolean("mechanics.global-loot-property.disable-game", false);
        Loot.DefaultProperties.DEFAULT_DISABLE_STATS = config.getBoolean("mechanics.global-loot-property.disable-stat", false);
        Loot.DefaultProperties.DEFAULT_INSTANT_GAME = config.getBoolean("mechanics.global-loot-property.instant-game", false);
        Loot.DefaultProperties.DEFAULT_SHOW_IN_FINDER = config.getBoolean("mechanics.global-loot-property.show-in-fishfinder", true);

        Section placeholderSection = config.getSection("other-settings.placeholder-register");
        if (placeholderSection != null) {
            for (Map.Entry<String, Object> entry : placeholderSection.getStringRouteMappedValues(false).entrySet()) {
                if (entry.getValue() instanceof String original) {
                    plugin.getPlaceholderManager().registerCustomPlaceholder(entry.getKey(), original);
                }
            }
        }

        OffsetUtils.load(config.getSection("other-settings.offset-characters"));

        EventManager.GLOBAL_ACTIONS.clear();
        EventManager.GLOBAL_TIMES_ACTION.clear();
        Section globalEvents = config.getSection("mechanics.global-events");
        if (globalEvents != null) {
            for (Map.Entry<String, Object> entry : globalEvents.getStringRouteMappedValues(false).entrySet()) {
                MechanicType type = MechanicType.index().value(entry.getKey());
                if (entry.getValue() instanceof Section inner) {
                    Map<ActionTrigger, Action<Player>[]> actionMap = new HashMap<>();
                    Map<ActionTrigger, TreeMap<Integer, Action<Player>[]>> actionTimesMap = new HashMap<>();
                    for (Map.Entry<String, Object> innerEntry : inner.getStringRouteMappedValues(false).entrySet()) {
                        if (innerEntry.getValue() instanceof Section actionSection) {
                            String trigger = innerEntry.getKey().toUpperCase(Locale.ENGLISH);
                            if (trigger.equals("SUCCESS_TIMES") || trigger.equals("SUCCESS-TIMES")) {
                                actionTimesMap.put(ActionTrigger.SUCCESS, plugin.getActionManager().parseTimesActions(actionSection));
                            } else {
                                actionMap.put(ActionTrigger.valueOf(trigger), plugin.getActionManager().parseActions(actionSection));
                            }
                        }
                    }
                    EventManager.GLOBAL_ACTIONS.put(type, actionMap);
                    EventManager.GLOBAL_TIMES_ACTION.put(type, actionTimesMap);
                }
            }
        }

        TranslationManager.forceLocale(TranslationManager.parseLocale(config.getString("force-locale", "")));
    }

    private void loadConfigs() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (ConfigType type : ConfigType.values()) {
            File typeFolder = new File(plugin.getDataFolder(), "contents" + File.separator + type.path());
            if (!typeFolder.exists()) {
                if (!typeFolder.mkdirs()) return;
                plugin.getBootstrap().saveResource("contents" + File.separator + type.path() + File.separator + "default.yml", false);
            }
            Map<String, Node<ConfigParserFunction>> nodes = type.parser();
            fileDeque.push(typeFolder);
            while (!fileDeque.isEmpty()) {
                File file = fileDeque.pop();
                File[] files = file.listFiles();
                if (files == null) continue;
                for (File subFile : files) {
                    if (subFile.isDirectory()) {
                        fileDeque.push(subFile);
                    } else if (subFile.isFile() && subFile.getName().endsWith(".yml")) {
                        try {
                            YamlDocument document = plugin.getConfigManager().loadData(subFile);
                            for (Map.Entry<String, Object> entry : document.getStringRouteMappedValues(false).entrySet()) {
                                try {
                                    if (entry.getValue() instanceof Section section) {
                                        type.parse(entry.getKey(), section, nodes);
                                    }
                                } catch (Exception e) {
                                    plugin.getPluginLogger().warn("Invalid config " + subFile.getPath() + " - Failed to parse section " + entry.getKey(), e);
                                }
                            }
                        } catch (ConstructorException e) {
                            plugin.getPluginLogger().warn("Could not load config file: " + subFile.getAbsolutePath() + ". Is it a corrupted file?");
                        }
                    }
                }
            }
        }
    }

    private Map<Key, Short> getEnchantments(Section section) {
        Map<Key, Short> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : section.getStringRouteMappedValues(false).entrySet()) {
            int level = Math.min(255, Math.max(1, (int) entry.getValue()));
            if (Registry.ENCHANTMENT.get(Objects.requireNonNull(NamespacedKey.fromString(entry.getKey()))) != null) {
                map.put(Key.fromString(entry.getKey()), (short) level);
            }
        }
        return map;
    }

    private List<Tuple<Double, String, Short>> getPossibleEnchantments(Section section) {
        List<Tuple<Double, String, Short>> list = new ArrayList<>();
        for (Map.Entry<String, Object> entry : section.getStringRouteMappedValues(false).entrySet()) {
            if (entry.getValue() instanceof Section inner) {
                Tuple<Double, String, Short> tuple = Tuple.of(
                        inner.getDouble("chance"),
                        inner.getString("enchant"),
                        Short.valueOf(String.valueOf(inner.getInt("level")))
                );
                list.add(tuple);
            }
        }
        return list;
    }

    private Pair<Key, Short> getEnchantmentPair(String enchantmentWithLevel) {
        String[] split = enchantmentWithLevel.split(":", 3);
        return Pair.of(Key.of(split[0], split[1]), Short.parseShort(split[2]));
    }

    @SuppressWarnings("unchecked")
    private void registerBuiltInItemProperties() {
        Function<Object, BiConsumer<Item<ItemStack>, Context<Player>>> f1 = arg -> {
            Section section = (Section) arg;
            boolean stored = Objects.equals(section.getNameAsString(), "stored-enchantment-pool");
            Section amountSection = section.getSection("amount");
            Section enchantSection = section.getSection("pool");
            List<Pair<Integer, MathValue<Player>>> amountList = new ArrayList<>();
            for (Map.Entry<String, Object> entry : amountSection.getStringRouteMappedValues(false).entrySet()) {
                amountList.add(Pair.of(Integer.parseInt(entry.getKey()), MathValue.auto(entry.getValue())));
            }
            List<Pair<Pair<Key, Short>, MathValue<Player>>> enchantPoolPair = new ArrayList<>();
            for (Map.Entry<String, Object> entry : enchantSection.getStringRouteMappedValues(false).entrySet()) {
                enchantPoolPair.add(Pair.of(getEnchantmentPair(entry.getKey()), MathValue.auto(entry.getValue())));
            }
            if (amountList.isEmpty() || enchantPoolPair.isEmpty()) {
                throw new RuntimeException("Both `pool` and `amount` should not be empty");
            }
            return (item, context) -> {
                List<Pair<Integer, Double>> parsedAmountPair = new ArrayList<>(amountList.size());
                for (Pair<Integer, MathValue<Player>> rawValue : amountList) {
                    parsedAmountPair.add(Pair.of(rawValue.left(), rawValue.right().evaluate(context)));
                }
                int amount = WeightUtils.getRandom(parsedAmountPair);
                if (amount <= 0) return;
                HashSet<Enchantment> addedEnchantments = new HashSet<>();
                List<Pair<Pair<Key, Short>, Double>> cloned = new ArrayList<>(enchantPoolPair.size());
                for (Pair<Pair<Key, Short>, MathValue<Player>> rawValue : enchantPoolPair) {
                    cloned.add(Pair.of(rawValue.left(), rawValue.right().evaluate(context)));
                }
                int i = 0;
                outer:
                while (i < amount && !cloned.isEmpty()) {
                    Pair<Key, Short> enchantPair = WeightUtils.getRandom(cloned);
                    Enchantment enchantment = Registry.ENCHANTMENT.get(Objects.requireNonNull(NamespacedKey.fromString(enchantPair.left().toString())));
                    if (enchantment == null) {
                        plugin.getPluginLogger().warn("Enchantment: " + enchantPair.left() + " doesn't exist.");
                        return;
                    }
                    if (!stored) {
                        for (Enchantment added : addedEnchantments) {
                            if (enchantment.conflictsWith(added)) {
                                cloned.removeIf(pair -> pair.left().left().equals(enchantPair.left()));
                                continue outer;
                            }
                        }
                    }
                    if (stored) {
                        item.addStoredEnchantment(enchantPair.left(), enchantPair.right());
                    } else {
                        item.addEnchantment(enchantPair.left(), enchantPair.right());
                    }
                    addedEnchantments.add(enchantment);
                    cloned.removeIf(pair -> pair.left().left().equals(enchantPair.left()));
                    i++;
                }
            };
        };
        this.registerItemParser(f1, 4800, "stored-enchantment-pool");
        this.registerItemParser(f1, 4700, "enchantment-pool");
        Function<Object, BiConsumer<Item<ItemStack>, Context<Player>>> f2 = arg -> {
            Section section = (Section) arg;
            boolean stored = Objects.equals(section.getNameAsString(), "random-stored-enchantments");
            List<Tuple<Double, String, Short>> enchantments = getPossibleEnchantments(section);
            return (item, context) -> {
                HashSet<String> ids = new HashSet<>();
                for (Tuple<Double, String, Short> pair : enchantments) {
                    if (Math.random() < pair.left() && !ids.contains(pair.mid())) {
                        if (stored) {
                            item.addStoredEnchantment(Key.fromString(pair.mid()), pair.right());
                        } else {
                            item.addEnchantment(Key.fromString(pair.mid()), pair.right());
                        }
                        ids.add(pair.mid());
                    }
                }
            };
        };
        this.registerItemParser(f2, 4850, "random-stored-enchantments");
        this.registerItemParser(f2, 4750, "random-enchantments");
        this.registerItemParser(arg -> {
            Section section = (Section) arg;
            Map<Key, Short> map = getEnchantments(section);
            return (item, context) -> item.storedEnchantments(map);
        }, 4600, "stored-enchantments");
        this.registerItemParser(arg -> {
            Section section = (Section) arg;
            Map<Key, Short> map = getEnchantments(section);
            return (item, context) -> item.enchantments(map);
        }, 4500, "enchantments");
        this.registerItemParser(arg -> {
            String base64 = (String) arg;
            return (item, context) -> item.skull(base64);
        }, 5200, "head64");
        this.registerItemParser(arg -> {
            List<String> args = ListUtils.toList(arg);
            return (item, context) -> item.itemFlags(args);
        }, 5100, "item-flags");
        this.registerItemParser(arg -> {
            MathValue<Player> mathValue = MathValue.auto(arg);
            return (item, context) -> item.customModelData((int) mathValue.evaluate(context));
        }, 5000, "custom-model-data");
        this.registerItemParser(arg -> {
            TextValue<Player> textValue = TextValue.auto("<!i><white>" + arg);
            return (item, context) -> {
                item.displayName(AdventureHelper.miniMessageToJson(textValue.render(context)));
            };
        }, 4000, "display", "name");
        this.registerItemParser(arg -> {
            List<String> list = ListUtils.toList(arg);
            List<TextValue<Player>> lore = new ArrayList<>();
            for (String text : list) {
                lore.add(TextValue.auto("<!i><white>" + text));
            }
            return (item, context) -> {
                item.lore(lore.stream()
                        .map(it -> AdventureHelper.miniMessageToJson(it.render(context)))
                        .toList());
            };
        }, 3_000, "display", "lore");
        this.registerItemParser(arg -> {
            boolean enable = (boolean) arg;
            return (item, context) -> {
                if (!enable) return;
                item.setTag(context.arg(ContextKeys.ID), "CustomFishing", "id");
            };
        }, 2_000, "tag");
        this.registerItemParser(arg -> {
            boolean enable = (boolean) arg;
            return (item, context) -> {
                item.unbreakable(enable);
            };
        }, 2_211, "unbreakable");
        this.registerItemParser(arg -> {
            boolean enable = (boolean) arg;
            return (item, context) -> {
                if (enable) return;
                item.setTag(UUID.randomUUID(), "CustomFishing", "uuid");
            };
        }, 2_222, "stackable");
        this.registerItemParser(arg -> {
            boolean enable = (boolean) arg;
            return (item, context) -> item.setTag(enable ? 1 : 0, "CustomFishing", "placeable");
        }, 2_335, "placeable");
        this.registerItemParser(arg -> {
            String sizePair = (String) arg;
            String[] split = sizePair.split("~", 2);
            MathValue<Player> min = MathValue.auto(split[0]);
            MathValue<Player> max = split.length == 2 ? MathValue.auto(split[1]) : MathValue.auto(split[0]);
            return (item, context) -> {
                double minSize = min.evaluate(context);
                double maxSize = max.evaluate(context);
                float size = (float) RandomUtils.generateRandomDouble(minSize, maxSize);
                Double sm = context.arg(ContextKeys.SIZE_MULTIPLIER);
                if (sm == null) sm = 1.0;
                Double sa = context.arg(ContextKeys.SIZE_ADDER);
                if (sa == null) sa = 0.0;
                size = (float) (sm * size + sa);
                if (restrictedSizeRange()) {
                    if (size > maxSize) {
                        size = (float) maxSize;
                    }
                    if (size < minSize) {
                        size = (float) minSize;
                    }
                }
                item.setTag(size, "CustomFishing", "size");
                context.arg(ContextKeys.SIZE, size);
                context.arg(ContextKeys.MIN_SIZE, minSize);
                context.arg(ContextKeys.MAX_SIZE, maxSize);
                context.arg(ContextKeys.SIZE_FORMATTED, String.format("%.2f", size));
            };
        }, 1_000, "size");
        this.registerItemParser(arg -> {
            Section section = (Section) arg;
            MathValue<Player> base = MathValue.auto(section.get("base", "0"));
            MathValue<Player> bonus = MathValue.auto(section.get("bonus", "0"));
            return (item, context) -> {
                double basePrice = base.evaluate(context);
                context.arg(ContextKeys.BASE, basePrice);
                double bonusPrice = bonus.evaluate(context);
                context.arg(ContextKeys.BONUS, bonusPrice);
                String formula = plugin.getMarketManager().getFormula();
                TextValue<Player> playerTextValue = TextValue.auto(formula);
                String rendered = playerTextValue.render(context);
                List<String> unparsed = plugin.getPlaceholderManager().resolvePlaceholders(rendered);
                for (String unparsedValue : unparsed) {
                    rendered = rendered.replace(unparsedValue, "0");
                }
                double price = ExpressionHelper.evaluate(rendered);
                item.setTag(price, "Price");
                context.arg(ContextKeys.PRICE, price);
                context.arg(ContextKeys.PRICE_FORMATTED, String.format("%.2f", price));
            };
        }, 1_500, "price");
        this.registerItemParser(arg -> {
            boolean random = (boolean) arg;
            return (item, context) -> {
                if (!random) return;
                if (item.hasTag("CustomFishing", "max_dur")) {
                    CustomDurabilityItem durabilityItem = new CustomDurabilityItem(item);
                    durabilityItem.damage(RandomUtils.generateRandomInt(0, durabilityItem.maxDamage() - 1));
                } else {
                    item.damage(RandomUtils.generateRandomInt(0, item.maxDamage().get() - 1));
                }
            };
        }, 3200, "random-durability");
        this.registerItemParser(arg -> {
            MathValue<Player> mathValue = MathValue.auto(arg);
            return (item, context) -> {
                int max = (int) mathValue.evaluate(context);
                item.setTag(max, "CustomFishing", "max_dur");
                item.setTag(max, "CustomFishing", "cur_dur");
                CustomDurabilityItem customDurabilityItem = new CustomDurabilityItem(item);
                customDurabilityItem.damage(0);
            };
        }, 3100, "max-durability");
        this.registerItemParser(arg -> {
            Section section = (Section) arg;
            ArrayList<ItemEditor> editors = new ArrayList<>();
            ItemStackUtils.sectionToTagEditor(section, editors);
            return (item, context) -> {
                for (ItemEditor editor : editors) {
                    editor.apply(((AbstractItem<RtagItem, ItemStack>) item).getRTagItem(), context);
                }
            };
        }, 10_050, "nbt");
        if (VersionHelper.isVersionNewerThan1_20_5()) {
            this.registerItemParser(arg -> {
                Section section = (Section) arg;
                ArrayList<ItemEditor> editors = new ArrayList<>();
                ItemStackUtils.sectionToComponentEditor(section, editors);
                return (item, context) -> {
                    for (ItemEditor editor : editors) {
                        editor.apply(((AbstractItem<RtagItem, ItemStack>) item).getRTagItem(), context);
                    }
                };
            }, 10_075, "components");
        }
    }

    private void registerBuiltInEffectModifierParser() {
        this.registerEffectModifierParser(object -> {
            Section section = (Section) object;
            return builder -> builder.requirements(List.of(plugin.getRequirementManager().parseRequirements(section, true)));
        }, "requirements");
        this.registerEffectModifierParser(object -> {
            Section section = (Section) object;
            ArrayList<TriConsumer<Effect, Context<Player>, Integer>> property = new ArrayList<>();
            for (Map.Entry<String, Object> entry : section.getStringRouteMappedValues(false).entrySet()) {
                if (entry.getValue() instanceof Section innerSection) {
                    property.add(parseEffect(innerSection, id -> plugin.getLootManager().getGroupMembers(id)));
                }
            }
            return builder -> {
                builder.modifiers(property);
            };
        }, "effects");
    }

    public TriConsumer<Effect, Context<Player>, Integer> parseEffect(Section section, Function<String, List<String>> groupProvider) {
        if (!section.contains("type")) {
            throw new RuntimeException(section.getRouteAsString());
        }
        Action<Player>[] actions = plugin.getActionManager().parseActions(section.getSection("actions"));
        String type = section.getString("type");
        if (type == null) {
            throw new RuntimeException(section.getRouteAsString());
        }
        switch (type) {
            case "lava-fishing" -> {
                return (((effect, context, phase) -> {
                    if (phase == 0) {
                        effect.properties().put(EffectProperties.LAVA_FISHING, true);
                        ActionManager.trigger(context, actions);
                    }
                }));
            }
            case "void-fishing" -> {
                return (((effect, context, phase) -> {
                    if (phase == 0) {
                        effect.properties().put(EffectProperties.VOID_FISHING, true);
                        ActionManager.trigger(context, actions);
                    }
                }));
            }
            case "weight-mod" -> {
                var op = parseWeightOperation(section.getStringList("value"), lootValidator, groupProvider);
                return (((effect, context, phase) -> {
                    if (phase == 1) {
                        effect.weightOperations(op);
                        ActionManager.trigger(context, actions);
                    }
                }));
            }
            case "weight-mod-ignore-conditions" -> {
                var op = parseWeightOperation(section.getStringList("value"), lootValidator, groupProvider);
                return (((effect, context, phase) -> {
                    if (phase == 1) {
                        effect.weightOperationsIgnored(op);
                        ActionManager.trigger(context, actions);
                    }
                }));
            }
            case "group-mod", "group_mod" -> {
                var op = parseGroupWeightOperation(section.getStringList("value"), true, groupProvider);
                return (((effect, context, phase) -> {
                    if (phase == 1) {
                        effect.weightOperations(op);
                        ActionManager.trigger(context, actions);
                    }
                }));
            }
            case "group-mod-ignore-conditions", "group_mod_ignore_conditions" -> {
                var op = parseGroupWeightOperation(section.getStringList("value"), false, groupProvider);
                return (((effect, context, phase) -> {
                    if (phase == 1) {
                        effect.weightOperationsIgnored(op);
                        ActionManager.trigger(context, actions);
                    }
                }));
            }
            case "wait-time", "wait_time" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context, phase) -> {
                    if (phase == 2) {
                        effect.waitTimeAdder(effect.waitTimeAdder() + value.evaluate(context));
                        ActionManager.trigger(context, actions);
                    }
                }));
            }
            case "hook-time", "hook_time", "wait-time-multiplier", "wait_time_multiplier" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context, phase) -> {
                    if (phase == 2) {
                        effect.waitTimeMultiplier(effect.waitTimeMultiplier() - 1 + value.evaluate(context));
                        ActionManager.trigger(context, actions);
                    }
                }));
            }
            case "difficulty" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context, phase) -> {
                    if (phase == 2) {
                        effect.difficultyAdder(effect.difficultyAdder() + value.evaluate(context));
                        ActionManager.trigger(context, actions);
                    }
                }));
            }
            case "difficulty-multiplier", "difficulty_multiplier", "difficulty-bonus" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context, phase) -> {
                    if (phase == 2) {
                        effect.difficultyMultiplier(effect.difficultyMultiplier() - 1 + value.evaluate(context));
                        ActionManager.trigger(context, actions);
                    }
                }));
            }
            case "size" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context, phase) -> {
                    if (phase == 2) {
                        effect.sizeAdder(effect.sizeAdder() + value.evaluate(context));
                        ActionManager.trigger(context, actions);
                    }
                }));
            }
            case "size-multiplier", "size-bonus" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context, phase) -> {
                    if (phase == 2) {
                        effect.sizeMultiplier(effect.sizeMultiplier() - 1 + value.evaluate(context));
                        ActionManager.trigger(context, actions);
                    }
                }));
            }
            case "game-time" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context, phase) -> {
                    if (phase == 2) {
                        effect.gameTimeAdder(effect.gameTimeAdder() + value.evaluate(context));
                        ActionManager.trigger(context, actions);
                    }
                }));
            }
            case "game-time-multiplier", "game-time-bonus" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context, phase) -> {
                    if (phase == 2) {
                        effect.gameTimeMultiplier(effect.gameTimeMultiplier() - 1 + value.evaluate(context));
                        ActionManager.trigger(context, actions);
                    }
                }));
            }
            case "score" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context, phase) -> {
                    if (phase == 2) {
                        effect.scoreAdder(effect.scoreAdder() + value.evaluate(context));
                        ActionManager.trigger(context, actions);
                    }
                }));
            }
            case "score-multiplier", "score-bonus" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context, phase) -> {
                    if (phase == 2) {
                        effect.scoreMultiplier(effect.scoreMultiplier() - 1 + value.evaluate(context));
                        ActionManager.trigger(context, actions);
                    }
                }));
            }
            case "multiple-loot" -> {
                MathValue<Player> value = MathValue.auto(section.get("value"));
                return (((effect, context, phase) -> {
                    if (phase == 2) {
                        effect.multipleLootChance(effect.multipleLootChance() + value.evaluate(context));
                        ActionManager.trigger(context, actions);
                    }
                }));
            }
            case "conditional" -> {
                Requirement<Player>[] requirements = plugin.getRequirementManager().parseRequirements(section.getSection("conditions"), true);
                Section effectSection = section.getSection("effects");
                ArrayList<TriConsumer<Effect, Context<Player>, Integer>> effects = new ArrayList<>();
                if (effectSection != null)
                    for (Map.Entry<String, Object> entry : effectSection.getStringRouteMappedValues(false).entrySet())
                        if (entry.getValue() instanceof Section inner)
                            effects.add(parseEffect(inner, groupProvider));
                return (((effect, context, phase) -> {
                    if (!RequirementManager.isSatisfied(context, requirements)) return;
                    for (TriConsumer<Effect, Context<Player>, Integer> consumer : effects) {
                        consumer.accept(effect, context, phase);
                    }
                }));
            }
            default -> {
                return (((effect, context, phase) -> {}));
            }
        }
    }

    private WeightOperation parseSharedGroupWeight(String op, int memberCount, boolean forAvailable, Function<String, List<String>> groupProvider) {
        switch (op.charAt(0)) {
            case '-' -> {
                MathValue<Player> arg = MathValue.auto(op.substring(1));
                return new ReduceWeightOperation(arg, memberCount, forAvailable);
            }
            case '+' -> {
                MathValue<Player> arg = MathValue.auto(op.substring(1));
                return new AddWeightOperation(arg, memberCount, forAvailable);
            }
            case '=' -> {
                String expression = op.substring(1);
                MathValue<Player> arg = MathValue.auto(expression);
                List<String> placeholders = BukkitPlaceholderManager.getInstance().resolvePlaceholders(expression);
                List<String> otherEntries = new ArrayList<>();
                List<Pair<String, String[]>> otherGroups = new ArrayList<>();
                for (String placeholder : placeholders) {
                    if (placeholder.startsWith("{entry_")) {
                        otherEntries.add(placeholder.substring("{entry_".length(), placeholder.length() - 1));
                    } else if (placeholder.startsWith("{group")) {
                        String groupExpression = placeholder.substring("{group_".length(), placeholder.length() - 1);
                        List<String> members = getGroupMembers(groupExpression, groupProvider);
                        if (members.isEmpty()) {
                            plugin.getPluginLogger().warn("Failed to load expression: " + expression + ". Invalid group: " + groupExpression);
                            continue;
                        }
                        otherGroups.add(Pair.of(groupExpression, members.toArray(new String[0])));
                    }
                }
                return new CustomWeightOperation(arg, expression.contains("{1}"), otherEntries, otherGroups, memberCount, forAvailable);
            }
            default -> throw new IllegalArgumentException("Invalid shared weight operation: " + op);
        }
    }

    private WeightOperation parseWeightOperation(String op, boolean forAvailable, Function<String, List<String>> groupProvider) {
        switch (op.charAt(0)) {
            case '/' -> {
                MathValue<Player> arg = MathValue.auto(op.substring(1));
                return new DivideWeightOperation(arg, forAvailable);
            }
            case '*' -> {
                MathValue<Player> arg = MathValue.auto(op.substring(1));
                return new MultiplyWeightOperation(arg, forAvailable);
            }
            case '-' -> {
                MathValue<Player> arg = MathValue.auto(op.substring(1));
                return new ReduceWeightOperation(arg, 1, forAvailable);
            }
            case '%' -> {
                MathValue<Player> arg = MathValue.auto(op.substring(1));
                return new ModuloWeightOperation(arg, forAvailable);
            }
            case '+' -> {
                MathValue<Player> arg = MathValue.auto(op.substring(1));
                return new AddWeightOperation(arg, 1, forAvailable);
            }
            case '=' -> {
                String expression = op.substring(1);
                MathValue<Player> arg = MathValue.auto(expression);
                List<String> placeholders = BukkitPlaceholderManager.getInstance().resolvePlaceholders(expression);
                List<String> otherEntries = new ArrayList<>();
                List<Pair<String, String[]>> otherGroups = new ArrayList<>();
                for (String placeholder : placeholders) {
                    if (placeholder.startsWith("{entry_")) {
                        otherEntries.add(placeholder.substring("{entry_".length(), placeholder.length() - 1));
                    } else if (placeholder.startsWith("{group")) {
                        String groupExpression = placeholder.substring("{group_".length(), placeholder.length() - 1);
                        List<String> members = getGroupMembers(groupExpression, groupProvider);
                        if (members.isEmpty()) {
                            plugin.getPluginLogger().warn("Failed to load expression: " + expression + ". Invalid group: " + groupExpression);
                            continue;
                        }
                        otherGroups.add(Pair.of(groupExpression, members.toArray(new String[0])));
                    }
                }
                return new CustomWeightOperation(arg, expression.contains("{1}"), otherEntries, otherGroups, 1, forAvailable);
            }
            default -> throw new IllegalArgumentException("Invalid weight operation: " + op);
        }
    }

    @Override
    public List<Pair<String, WeightOperation>> parseWeightOperation(List<String> ops, Function<String, Boolean> validator, Function<String, List<String>> groupProvider) {
        List<Pair<String, WeightOperation>> result = new ArrayList<>();
        for (String op : ops) {
            String[] split = op.split(":", 3);
            if (split.length < 2) {
                plugin.getPluginLogger().warn("Illegal weight operation: " + op);
                continue;
            }
            if (split.length == 2) {
                String id = split[0];
                if (!validator.apply(id)) {
                    plugin.getPluginLogger().warn("Illegal weight operation: " + op + ". Id " + id + " is not valid");
                    continue;
                }
                result.add(Pair.of(id, parseWeightOperation(split[1], false, groupProvider)));
            } else {
                String type = split[0];
                String id = split[1];
                switch (type) {
                    case "group_for_each" -> {
                        List<String> members = getGroupMembers(id, groupProvider);
                        if (members.isEmpty()) {
                            plugin.getPluginLogger().warn("Failed to load expression: " + op + ". Invalid group: " + id);
                            continue;
                        }
                        WeightOperation operation = parseWeightOperation(split[2], false, groupProvider);
                        for (String member : members) {
                            result.add(Pair.of(member, operation));
                        }
                    }
                    case "group_available_for_each" -> {
                        List<String> members = getGroupMembers(id, groupProvider);
                        if (members.isEmpty()) {
                            plugin.getPluginLogger().warn("Failed to load expression: " + op + ". Invalid group: " + id);
                            continue;
                        }
                        WeightOperation operation = parseWeightOperation(split[2], true, groupProvider);
                        for (String member : members) {
                            result.add(Pair.of(member, operation));
                        }
                    }
                    case "group_total" -> {
                        List<String> members = getGroupMembers(id, groupProvider);
                        if (members.isEmpty()) {
                            plugin.getPluginLogger().warn("Failed to load expression: " + op + ". Invalid group: " + id);
                            continue;
                        }
                        WeightOperation operation = parseSharedGroupWeight(split[2], members.size(), false, groupProvider);
                        for (String member : members) {
                            result.add(Pair.of(member, operation));
                        }
                    }
                    case "group_available_total" -> {
                        List<String> members = getGroupMembers(id, groupProvider);
                        if (members.isEmpty()) {
                            plugin.getPluginLogger().warn("Failed to load expression: " + op + ". Invalid group: " + id);
                            continue;
                        }
                        WeightOperation operation = parseSharedGroupWeight(split[2], members.size(), true, groupProvider);
                        for (String member : members) {
                            result.add(Pair.of(member, operation));
                        }
                    }
                    case "loot_available" -> {
                        if (!validator.apply(id)) {
                            plugin.getPluginLogger().warn("Illegal weight operation: " + op + ". Id " + id + " is not valid");
                            continue;
                        }
                        result.add(Pair.of(id, parseWeightOperation(split[2], true, groupProvider)));
                    }
                    default -> {
                        if (!validator.apply(id)) {
                            plugin.getPluginLogger().warn("Illegal weight operation: " + op + ". Id " + id + " is not valid");
                            continue;
                        }
                        result.add(Pair.of(id, parseWeightOperation(split[2], false, groupProvider)));
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<Pair<String, WeightOperation>> parseGroupWeightOperation(List<String> gops, boolean forAvailable, Function<String, List<String>> groupProvider) {
        List<Pair<String, WeightOperation>> result = new ArrayList<>();
        for (String gop : gops) {
            String[] split = gop.split(":", 2);
            if (split.length < 2) {
                plugin.getPluginLogger().warn("Illegal weight operation: " + gop);
                continue;
            }
            WeightOperation operation = parseWeightOperation(split[1], forAvailable, groupProvider);
            String groupExpression = split[0];
            for (String member : getGroupMembers(groupExpression, groupProvider)) {
                result.add(Pair.of(member, operation));
            }
        }
        return result;
    }

    private List<String> getGroupMembers(String groupExpression, Function<String, List<String>> groupProvider) {
        if (groupExpression.contains("&")) {
            String[] groups = groupExpression.split("&");
            List<Set<String>> groupSets = new ArrayList<>();
            for (String group : groups) {
                groupSets.add(new HashSet<>(groupProvider.apply(group)));
            }
            Set<String> intersection = groupSets.get(0);
            for (int i = 1; i < groupSets.size(); i++) {
                intersection.retainAll(groupSets.get(i));
            }
            return new ArrayList<>(intersection);
        } else if (groupExpression.contains("|")) {
            Set<String> members = new HashSet<>();
            String[] groups = groupExpression.split("\\|");
            for (String group : groups) {
                members.addAll(groupProvider.apply(group));
            }
            return new ArrayList<>(members);
        } else {
            return groupProvider.apply(groupExpression);
        }
    }

    private void registerBuiltInHookParser() {
        this.registerHookParser(object -> {
            List<String> lore = ListUtils.toList(object);
            return builder -> builder.lore(lore.stream().map(it -> "<!i>" + it).toList());
        }, "lore-on-rod");
    }

    private void registerBuiltInTotemParser() {
        this.registerTotemParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.radius(mathValue);
        }, "radius");
        this.registerTotemParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.duration(mathValue);
        }, "duration");
        this.registerTotemParser(object -> {
            Section section = (Section) object;
            TotemParticle[] particles = getParticleSettings(section);
            return builder -> builder.particleSettings(particles);
        }, "particles");
        this.registerTotemParser(object -> {
            Section section = (Section) object;
            TotemModel[] models = getTotemModels(section);
            return builder -> builder.totemModels(models);
        }, "pattern");
    }

    private void registerBuiltInBaseEffectParser() {
        this.registerBaseEffectParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.difficultyAdder(mathValue);
        }, "base-effects", "difficulty-adder");
        this.registerBaseEffectParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.difficultyMultiplier(mathValue);
        }, "base-effects", "difficulty-multiplier");
        this.registerBaseEffectParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.gameTimeAdder(mathValue);
        }, "base-effects", "game-time-adder");
        this.registerBaseEffectParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.gameTimeMultiplier(mathValue);
        }, "base-effects", "game-time-multiplier");
        this.registerBaseEffectParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.waitTimeAdder(mathValue);
        }, "base-effects", "wait-time-adder");
        this.registerBaseEffectParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.waitTimeMultiplier(mathValue);
        }, "base-effects", "wait-time-multiplier");
    }

    private void registerBuiltInBlockParser() {
        this.registerBlockParser(object -> {
            String block = (String) object;
            return builder -> builder.blockID(block);
        }, "block");
        this.registerBlockParser(object -> {
            Section section = (Section) object;
            List<BlockDataModifier> dataModifiers = new ArrayList<>();
            List<BlockStateModifier> stateModifiers = new ArrayList<>();
            for (Map.Entry<String, Object> innerEntry : section.getStringRouteMappedValues(false).entrySet()) {
                BlockDataModifierFactory dataModifierFactory = plugin.getBlockManager().getBlockDataModifierFactory(innerEntry.getKey());
                if (dataModifierFactory != null) {
                    dataModifiers.add(dataModifierFactory.process(innerEntry.getValue()));
                    continue;
                }
                BlockStateModifierFactory stateModifierFactory = plugin.getBlockManager().getBlockStateModifierFactory(innerEntry.getKey());
                if (stateModifierFactory != null) {
                    stateModifiers.add(stateModifierFactory.process(innerEntry.getValue()));
                }
            }
            return builder -> {
                builder.dataModifierList(dataModifiers);
                builder.stateModifierList(stateModifiers);
            };
        }, "properties");
        this.registerBlockParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.horizontalVector(mathValue);
        }, "velocity", "horizontal");
        this.registerBlockParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.verticalVector(mathValue);
        }, "velocity", "vertical");
    }

    private void registerBuiltInEntityParser() {
        this.registerEntityParser(object -> {
            String entity = (String) object;
            return builder -> builder.entityID(entity);
        }, "entity");
        this.registerEntityParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.horizontalVector(mathValue);
        }, "velocity", "horizontal");
        this.registerEntityParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.verticalVector(mathValue);
        }, "velocity", "vertical");
        this.registerEntityParser(object -> {
            Section section = (Section) object;
            return builder -> builder.propertyMap(section.getStringRouteMappedValues(false));
        }, "properties");
    }

    private void registerBuiltInEventParser() {
        this.registerEventParser(object -> {
            boolean disable = (boolean) object;
            return builder -> builder.disableGlobalActions(disable);
        }, "disable-global-event");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.LURE, actions);
        }, "events", "lure");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.ESCAPE, actions);
        }, "events", "escape");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.SUCCESS, actions);
        }, "events", "success");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.ACTIVATE, actions);
        }, "events", "activate");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.FAILURE, actions);
        }, "events", "failure");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.HOOK, actions);
        }, "events", "hook");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.CONSUME, actions);
        }, "events", "consume");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.CAST, actions);
        }, "events", "cast");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.BITE, actions);
        }, "events", "bite");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.LAND, actions);
        }, "events", "land");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.TIMER, actions);
        }, "events", "timer");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.INTERACT, actions);
        }, "events", "interact");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.REEL, actions);
        }, "events", "reel");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.NEW_SIZE_RECORD, actions);
        }, "events", "new_size_record");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            Action<Player>[] actions = plugin.getActionManager().parseActions(section);
            return builder -> builder.action(ActionTrigger.NEW_SIZE_RECORD, actions);
        }, "events", "new-size-record");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            TreeMap<Integer, Action<Player>[]> actions = plugin.getActionManager().parseTimesActions(section);
            return builder -> builder.actionTimes(ActionTrigger.SUCCESS, actions);
        }, "events", "success_times");
        this.registerEventParser(object -> {
            Section section = (Section) object;
            TreeMap<Integer, Action<Player>[]> actions = plugin.getActionManager().parseTimesActions(section);
            return builder -> builder.actionTimes(ActionTrigger.SUCCESS, actions);
        }, "events", "success-times");
    }

    private void registerBuiltInLootParser() {
        this.registerLootParser(object -> {
            Section section = (Section) object;
            Map<String, TextValue<Player>> data = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : section.getStringRouteMappedValues(false).entrySet()) {
                if (entry.getValue() instanceof String str) {
                    data.put(entry.getKey(), TextValue.auto(str));
                } else {
                    data.put(entry.getKey(), TextValue.auto(entry.getValue().toString()));
                }
            }
            return builder -> {
                builder.customData(data);
            };
        }, "custom-data");
        this.registerLootParser(object -> {
            if (object instanceof Boolean b) {
                return builder -> builder.toInventory(MathValue.plain(b ? 1 : 0));
            } else {
                return builder -> builder.toInventory(MathValue.auto(object));
            }
        }, "to-inventory");
        this.registerLootParser(object -> {
            boolean value = (boolean) object;
            return builder -> builder.preventGrabbing(value);
        }, "prevent-grabbing");
        this.registerLootParser(object -> {
            String string = (String) object;
            return builder -> builder.nick(string);
        }, "nick");
        this.registerLootParser(object -> {
            boolean value = (boolean) object;
            return builder -> builder.showInFinder(value);
        }, "show-in-fishfinder");
        this.registerLootParser(object -> {
            boolean value = (boolean) object;
            return builder -> builder.disableStatistics(value);
        }, "disable-stat");
        this.registerLootParser(object -> {
            boolean value = (boolean) object;
            return builder -> builder.disableGame(value);
        }, "disable-game");
        this.registerLootParser(object -> {
            boolean value = (boolean) object;
            return builder -> builder.instantGame(value);
        }, "instant-game");
        this.registerLootParser(object -> {
            MathValue<Player> mathValue = MathValue.auto(object);
            return builder -> builder.score(mathValue);
        }, "score");
        this.registerLootParser(object -> {
            List<String> args = ListUtils.toList(object);
            return builder -> builder.groups(args.toArray(new String[0]));
        }, "group");
        this.registerLootParser(object -> {
            Section section = (Section) object;
            StatisticsKeys keys = new StatisticsKeys(
                    section.getString("amount"),
                    section.getString("size")
            );
            return builder -> builder.statisticsKeys(keys);
        }, "statistics");
    }

    @Override
    public void saveResource(String filePath) {
        if (!new File(plugin.getDataFolder(), filePath).exists()) {
            plugin.getBootstrap().saveResource(filePath, false);
        }
    }

    private ParticleSetting[] getParticleSettings(Section section) {
        List<ParticleSetting> particleSettings = new ArrayList<>();
        if (section != null)
            for (Map.Entry<String, Object> entry : section.getStringRouteMappedValues(false).entrySet()) {
                if (entry.getValue() instanceof Section innerSection) {
                    particleSettings.add(getParticleSetting(innerSection));
                }
            }
        return particleSettings.toArray(new ParticleSetting[0]);
    }

    private ParticleSetting getParticleSetting(Section section) {
        Particle particle = ParticleUtils.getParticle(section.getString("type","REDSTONE").toUpperCase(Locale.ENGLISH));
        String formulaHorizontal = section.getString("polar-coordinates-formula.horizontal");
        String formulaVertical = section.getString("polar-coordinates-formula.vertical");
        List<Pair<Double, Double>> ranges = section.getStringList("theta.range")
                .stream().map(it -> {
                    String[] split = it.split("~");
                    return Pair.of(Double.parseDouble(split[0]) * Math.PI / 180, Double.parseDouble(split[1]) * Math.PI / 180);
                }).toList();
        double interval = section.getDouble("theta.draw-interval", 3d);
        int delay = section.getInt("task.delay", 0);
        int period = section.getInt("task.period", 0);
        if (particle == dustParticle) {
            String color = section.getString("options.color","0,0,0");
            String[] colorSplit = color.split(",");
            return new DustParticleSetting(
                    formulaHorizontal, formulaVertical, particle, interval, ranges, delay, period,
                    new Particle.DustOptions(Color.fromRGB(Integer.parseInt(colorSplit[0]), Integer.parseInt(colorSplit[1]), Integer.parseInt(colorSplit[2])), section.getDouble("options.scale", 1.0).floatValue())
            );
        } else if (particle == Particle.DUST_COLOR_TRANSITION) {
            String color = section.getString("options.from","0,0,0");
            String[] colorSplit = color.split(",");
            String toColor = section.getString("options.to","255,255,255");
            String[] toColorSplit = toColor.split(",");
            return new DustParticleSetting(
                    formulaHorizontal, formulaVertical, particle, interval, ranges, delay, period,
                    new Particle.DustTransition(
                            Color.fromRGB(Integer.parseInt(colorSplit[0]), Integer.parseInt(colorSplit[1]), Integer.parseInt(colorSplit[2])),
                            Color.fromRGB(Integer.parseInt(toColorSplit[0]), Integer.parseInt(toColorSplit[1]), Integer.parseInt(toColorSplit[2])),
                            section.getDouble("options.scale", 1.0).floatValue()
                    )
            );
        } else {
            return new ParticleSetting(formulaHorizontal, formulaVertical, particle, interval, ranges, delay, period);
        }
    }

    private TotemModel[] getTotemModels(Section section) {
        TotemModel originalModel = parseModel(section);
        List<TotemModel> modelList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            originalModel = originalModel.deepClone().rotate90();
            modelList.add(originalModel);
            if (i % 2 == 0) {
                modelList.add(originalModel.mirrorVertically());
            } else {
                modelList.add(originalModel.mirrorHorizontally());
            }
        }
        return modelList.toArray(new TotemModel[0]);
    }

    @SuppressWarnings("unchecked")
    private TotemModel parseModel(Section section) {
        Section layerSection = section.getSection("layer");
        List<TotemBlock[][][]> totemBlocksList = new ArrayList<>();
        if (layerSection != null) {
            var set = layerSection.getStringRouteMappedValues(false).entrySet();
            TotemBlock[][][][] totemBlocks = new TotemBlock[set.size()][][][];
            for (Map.Entry<String, Object> entry : set) {
                if (entry.getValue() instanceof List<?> list) {
                    totemBlocks[Integer.parseInt(entry.getKey())-1] = parseLayer((List<String>) list);
                }
            }
            totemBlocksList.addAll(List.of(totemBlocks));
        }

        String[] core = section.getString("core","1,1,1").split(",");
        int x = Integer.parseInt(core[2]) - 1;
        int z = Integer.parseInt(core[1]) - 1;
        int y = Integer.parseInt(core[0]) - 1;
        return new TotemModel(
                x,y,z,
                totemBlocksList.toArray(new TotemBlock[0][][][])
        );
    }

    private TotemBlock[][][] parseLayer(List<String> lines) {
        List<TotemBlock[][]> totemBlocksList = new ArrayList<>();
        for (String line : lines) {
            totemBlocksList.add(parseSingleLine(line));
        }
        return totemBlocksList.toArray(new TotemBlock[0][][]);
    }

    private TotemBlock[][] parseSingleLine(String line) {
        List<TotemBlock[]> totemBlocksList = new ArrayList<>();
        String[] splits = line.split("\\s+");
        for (String split : splits) {
            totemBlocksList.add(parseSingleElement(split));
        }
        return totemBlocksList.toArray(new TotemBlock[0][]);
    }

    private TotemBlock[] parseSingleElement(String element) {
        String[] orBlocks = element.split("\\|\\|");
        List<TotemBlock> totemBlockList = new ArrayList<>();
        for (String block : orBlocks) {
            int index = block.indexOf("{");
            List<TotemBlockProperty> propertyList = new ArrayList<>();
            if (index == -1) {
                index = block.length();
            } else {
                String propertyStr = block.substring(index+1, block.length()-1);
                String[] properties = propertyStr.split(";");
                for (String property : properties) {
                    String[] split = property.split("=");
                    if (split.length < 2) continue;
                    String key = split[0];
                    String value = split[1];
                    switch (key) {
                        // Block face
                        case "face" -> {
                            BlockFace blockFace = BlockFace.valueOf(value.toUpperCase(Locale.ENGLISH));
                            propertyList.add(new FaceImpl(blockFace));
                        }
                        // Block axis
                        case "axis" -> {
                            Axis axis = Axis.valueOf(value.toUpperCase(Locale.ENGLISH));
                            propertyList.add(new AxisImpl(axis));
                        }
                        // Slab, Stair half
                        case "half" -> {
                            Bisected.Half half = Bisected.Half.valueOf(value.toUpperCase(Locale.ENGLISH));
                            propertyList.add(new HalfImpl(half));
                        }
                    }
                }
            }
            String type = block.substring(0, index);
            TotemBlock totemBlock = new TotemBlock(
                    TypeCondition.getTypeCondition(type),
                    propertyList.toArray(new TotemBlockProperty[0])
            );
            totemBlockList.add(totemBlock);
        }
        return totemBlockList.toArray(new TotemBlock[0]);
    }
}
