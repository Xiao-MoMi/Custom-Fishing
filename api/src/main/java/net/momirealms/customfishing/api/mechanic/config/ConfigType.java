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

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.MechanicType;
import net.momirealms.customfishing.api.mechanic.config.function.ConfigParserFunction;
import net.momirealms.customfishing.common.config.node.Node;
import net.momirealms.customfishing.common.util.TriConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Configuration types for various mechanics.
 */
public class ConfigType {

    public static final ConfigType ITEM = of(
            "item",
            () -> {
                HashMap<String, Node<ConfigParserFunction>> parsers = new HashMap<>();
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getLootFormatFunctions());
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getItemFormatFunctions());
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getEventFormatFunctions());
                return parsers;
            },
            (id, section, functions) -> {
                MechanicType.register(id, MechanicType.LOOT);
                ItemConfigParser config = new ItemConfigParser(id, section, functions);
                BukkitCustomFishingPlugin.getInstance().getItemManager().registerItem(config.getItem());
                BukkitCustomFishingPlugin.getInstance().getLootManager().registerLoot(config.getLoot());
                BukkitCustomFishingPlugin.getInstance().getEventManager().registerEventCarrier(config.getEventCarrier());
            }
    );

    public static final ConfigType ENTITY = of(
            "entity",
            () -> {
                HashMap<String, Node<ConfigParserFunction>> parsers = new HashMap<>();
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getLootFormatFunctions());
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getEntityFormatFunctions());
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getEventFormatFunctions());
                return parsers;
            },
            (id, section, functions) -> {
                MechanicType.register(id, MechanicType.LOOT);
                EntityConfigParser config = new EntityConfigParser(id, section, functions);
                BukkitCustomFishingPlugin.getInstance().getEntityManager().registerEntity(config.getEntity());
                BukkitCustomFishingPlugin.getInstance().getLootManager().registerLoot(config.getLoot());
                BukkitCustomFishingPlugin.getInstance().getEventManager().registerEventCarrier(config.getEventCarrier());
            }
    );

    public static final ConfigType BLOCK = of(
            "block",
            () -> {
                HashMap<String, Node<ConfigParserFunction>> parsers = new HashMap<>();
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getLootFormatFunctions());
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getBlockFormatFunctions());
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getEventFormatFunctions());
                return parsers;
            },
            (id, section, functions) -> {
                MechanicType.register(id, MechanicType.LOOT);
                BlockConfigParser config = new BlockConfigParser(id, section, functions);
                BukkitCustomFishingPlugin.getInstance().getBlockManager().registerBlock(config.getBlock());
                BukkitCustomFishingPlugin.getInstance().getLootManager().registerLoot(config.getLoot());
                BukkitCustomFishingPlugin.getInstance().getEventManager().registerEventCarrier(config.getEventCarrier());
            }
    );

    public static final ConfigType ROD = of(
            "rod",
            () -> {
                HashMap<String, Node<ConfigParserFunction>> parsers = new HashMap<>();
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getItemFormatFunctions());
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getEffectModifierFormatFunctions());
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getEventFormatFunctions());
                return parsers;
            },
            (id, section, functions) -> {
                MechanicType.register(id, MechanicType.ROD);
                RodConfigParser config = new RodConfigParser(id, section, functions);
                BukkitCustomFishingPlugin.getInstance().getItemManager().registerItem(config.getItem());
                //BukkitCustomFishingPlugin.getInstance().getLootManager().registerLoot(config.getLoot());
                BukkitCustomFishingPlugin.getInstance().getEffectManager().registerEffectModifier(config.getEffectModifier(), MechanicType.ROD);
                BukkitCustomFishingPlugin.getInstance().getEventManager().registerEventCarrier(config.getEventCarrier());
            }
    );

    public static final ConfigType BAIT = of(
            "bait",
            () -> {
                HashMap<String, Node<ConfigParserFunction>> parsers = new HashMap<>();
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getItemFormatFunctions());
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getEffectModifierFormatFunctions());
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getEventFormatFunctions());
                return parsers;
            },
            (id, section, functions) -> {
                MechanicType.register(id, MechanicType.BAIT);
                BaitConfigParser config = new BaitConfigParser(id, section, functions);
                BukkitCustomFishingPlugin.getInstance().getItemManager().registerItem(config.getItem());
                //BukkitCustomFishingPlugin.getInstance().getLootManager().registerLoot(config.getLoot());
                BukkitCustomFishingPlugin.getInstance().getEffectManager().registerEffectModifier(config.getEffectModifier(), MechanicType.BAIT);
                BukkitCustomFishingPlugin.getInstance().getEventManager().registerEventCarrier(config.getEventCarrier());
            }
    );

    public static final ConfigType HOOK = of(
            "hook",
            () -> {
                HashMap<String, Node<ConfigParserFunction>> parsers = new HashMap<>();
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getItemFormatFunctions());
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getEffectModifierFormatFunctions());
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getEventFormatFunctions());
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getHookFormatFunctions());
                return parsers;
            },
            (id, section, functions) -> {
                MechanicType.register(id, MechanicType.HOOK);
                HookConfigParser config = new HookConfigParser(id, section, functions);
                BukkitCustomFishingPlugin.getInstance().getItemManager().registerItem(config.getItem());
                //BukkitCustomFishingPlugin.getInstance().getLootManager().registerLoot(config.getLoot());
                BukkitCustomFishingPlugin.getInstance().getEffectManager().registerEffectModifier(config.getEffectModifier(), MechanicType.HOOK);
                BukkitCustomFishingPlugin.getInstance().getEventManager().registerEventCarrier(config.getEventCarrier());
                BukkitCustomFishingPlugin.getInstance().getHookManager().registerHook(config.getHook());
            }
    );

    public static final ConfigType UTIL = of(
            "util",
            () -> {
                HashMap<String, Node<ConfigParserFunction>> parsers = new HashMap<>();
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getItemFormatFunctions());
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getEffectModifierFormatFunctions());
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getEventFormatFunctions());
                return parsers;
            },
            (id, section, functions) -> {
                MechanicType.register(id, MechanicType.UTIL);
                UtilConfigParser config = new UtilConfigParser(id, section, functions);
                BukkitCustomFishingPlugin.getInstance().getItemManager().registerItem(config.getItem());
                //BukkitCustomFishingPlugin.getInstance().getLootManager().registerLoot(config.getLoot());
                BukkitCustomFishingPlugin.getInstance().getEffectManager().registerEffectModifier(config.getEffectModifier(), MechanicType.UTIL);
                BukkitCustomFishingPlugin.getInstance().getEventManager().registerEventCarrier(config.getEventCarrier());
            }
    );

    public static final ConfigType TOTEM = of(
            "totem",
            () -> {
                HashMap<String, Node<ConfigParserFunction>> parsers = new HashMap<>();
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getTotemFormatFunctions());
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getEffectModifierFormatFunctions());
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getEventFormatFunctions());
                return parsers;
            },
            (id, section, functions) -> {
                TotemConfigParser config = new TotemConfigParser(id, section, functions);
                BukkitCustomFishingPlugin.getInstance().getEffectManager().registerEffectModifier(config.getEffectModifier(), MechanicType.TOTEM);
                BukkitCustomFishingPlugin.getInstance().getEventManager().registerEventCarrier(config.getEventCarrier());
                BukkitCustomFishingPlugin.getInstance().getTotemManager().registerTotem(config.getTotemConfig());
            }
    );

    public static final ConfigType ENCHANT = of(
            "enchant",
            () -> {
                HashMap<String, Node<ConfigParserFunction>> parsers = new HashMap<>();
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getEventFormatFunctions());
                parsers.putAll(BukkitCustomFishingPlugin.getInstance().getConfigManager().getEffectModifierFormatFunctions());
                return parsers;
            },
            (id, section, functions) -> {
                EnchantConfigParser config = new EnchantConfigParser(id, section, functions);
                BukkitCustomFishingPlugin.getInstance().getEffectManager().registerEffectModifier(config.getEffectModifier(), MechanicType.ENCHANT);
                BukkitCustomFishingPlugin.getInstance().getEventManager().registerEventCarrier(config.getEventCarrier());
            }
    );

    public static final ConfigType MINI_GAME = of(
        "minigame",
            HashMap::new,
            (id, section, functions) -> {
                MiniGameConfigParser config = new MiniGameConfigParser(id, section);
                BukkitCustomFishingPlugin.getInstance().getGameManager().registerGame(config.getGame());
            }
    );

    private static final ConfigType[] values = new ConfigType[] {ITEM, ENTITY, BLOCK, HOOK, ROD, BAIT, UTIL, TOTEM, ENCHANT, MINI_GAME};

    /**
     * Gets an array of all configuration types.
     *
     * @return An array of all configuration types.
     */
    public static ConfigType[] values() {
        return values;
    }

    private final String path;
    private TriConsumer<String, Section, Map<String, Node<ConfigParserFunction>>> argumentConsumer;
    private Supplier<Map<String, Node<ConfigParserFunction>>> parserSupplier;

    /**
     * Creates a new ConfigType with the specified path and argument consumer.
     *
     * @param path the configuration path.
     * @param argumentConsumer the argument consumer.
     */
    public ConfigType(String path, Supplier<Map<String, Node<ConfigParserFunction>>> parserSupplier, TriConsumer<String, Section, Map<String, Node<ConfigParserFunction>>> argumentConsumer) {
        this.path = path;
        this.argumentConsumer = argumentConsumer;
        this.parserSupplier = parserSupplier;
    }

    /**
     * Set the argument consumer.
     *
     * @param argumentConsumer the argument consumer
     */
    public void argumentConsumer(TriConsumer<String, Section, Map<String, Node<ConfigParserFunction>>> argumentConsumer) {
        this.argumentConsumer = argumentConsumer;
    }

    /**
     * Creates a new ConfigType with the specified path and argument consumer.
     *
     * @param path the configuration path.
     * @param argumentConsumer the argument consumer.
     * @return A new ConfigType instance.
     */
    public static ConfigType of(String path, Supplier<Map<String, Node<ConfigParserFunction>>> parserSupplier, TriConsumer<String, Section, Map<String, Node<ConfigParserFunction>>> argumentConsumer) {
        return new ConfigType(path, parserSupplier, argumentConsumer);
    }

    /**
     * Parses the configuration for this type.
     *
     * @param id the identifier.
     * @param section the configuration section.
     * @param functions the configuration functions.
     */
    public void parse(String id, Section section, Map<String, Node<ConfigParserFunction>> functions) {
        argumentConsumer.accept(id, section, functions);
    }

    /**
     * Gets the configuration path.
     *
     * @return The configuration path.
     */
    public String path() {
        return path;
    }

    /**
     * Get the config parsers
     *
     * @return config parsers
     */
    public Map<String, Node<ConfigParserFunction>> parser() {
        return parserSupplier.get();
    }
}
