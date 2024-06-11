package net.momirealms.customfishing.api.mechanic.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.config.function.ConfigParserFunction;
import net.momirealms.customfishing.api.mechanic.item.ItemType;
import net.momirealms.customfishing.common.config.node.Node;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.Map;

public class ConfigType {

    public static final ConfigType ITEM = of(
            "item",
            (id, section, functions) -> {
                ItemType.register(id, ItemType.LOOT);
                ItemConfigParser config = new ItemConfigParser(id, section, functions);
                BukkitCustomFishingPlugin.getInstance().getItemManager().registerItem(config.getItem());
                BukkitCustomFishingPlugin.getInstance().getLootManager().registerLoot(config.getLoot());
                BukkitCustomFishingPlugin.getInstance().getEventManager().registerEventCarrier(config.getEventCarrier());
            }
    );

    public static final ConfigType ENTITY = of(
            "entity",
            (id, section, functions) -> {
                EntityConfigParser config = new EntityConfigParser(id, section, functions);
                BukkitCustomFishingPlugin.getInstance().getEntityManager().registerEntity(config.getEntity());
                BukkitCustomFishingPlugin.getInstance().getLootManager().registerLoot(config.getLoot());
                BukkitCustomFishingPlugin.getInstance().getEventManager().registerEventCarrier(config.getEventCarrier());
            }
    );

    public static final ConfigType BLOCK = of(
            "block",
            (id, section, functions) -> {
                BlockConfigParser config = new BlockConfigParser(id, section, functions);
                BukkitCustomFishingPlugin.getInstance().getBlockManager().registerBlock(config.getBlock());
                BukkitCustomFishingPlugin.getInstance().getLootManager().registerLoot(config.getLoot());
                BukkitCustomFishingPlugin.getInstance().getEventManager().registerEventCarrier(config.getEventCarrier());
            }
    );

    public static final ConfigType ROD = of(
            "rod",
            (id, section, functions) -> {
                ItemType.register(id, ItemType.ROD);
                RodConfigParser config = new RodConfigParser(id, section, functions);
                BukkitCustomFishingPlugin.getInstance().getItemManager().registerItem(config.getItem());
                BukkitCustomFishingPlugin.getInstance().getLootManager().registerLoot(config.getLoot());
                BukkitCustomFishingPlugin.getInstance().getEffectManager().registerEffectModifier(config.getEffectModifier());
                BukkitCustomFishingPlugin.getInstance().getEventManager().registerEventCarrier(config.getEventCarrier());
            }
    );

    public static final ConfigType BAIT = of(
            "bait",
            (id, section, functions) -> {
                ItemType.register(id, ItemType.BAIT);
                BaitConfigParser config = new BaitConfigParser(id, section, functions);
                BukkitCustomFishingPlugin.getInstance().getItemManager().registerItem(config.getItem());
                BukkitCustomFishingPlugin.getInstance().getLootManager().registerLoot(config.getLoot());
                BukkitCustomFishingPlugin.getInstance().getEffectManager().registerEffectModifier(config.getEffectModifier());
                BukkitCustomFishingPlugin.getInstance().getEventManager().registerEventCarrier(config.getEventCarrier());
            }
    );

    public static final ConfigType HOOK = of(
            "hook",
            (id, section, functions) -> {
                ItemType.register(id, ItemType.HOOK);
                HookConfigParser config = new HookConfigParser(id, section, functions);
                BukkitCustomFishingPlugin.getInstance().getItemManager().registerItem(config.getItem());
                BukkitCustomFishingPlugin.getInstance().getLootManager().registerLoot(config.getLoot());
                BukkitCustomFishingPlugin.getInstance().getEffectManager().registerEffectModifier(config.getEffectModifier());
                BukkitCustomFishingPlugin.getInstance().getEventManager().registerEventCarrier(config.getEventCarrier());
                BukkitCustomFishingPlugin.getInstance().getHookManager().registerHook(config.getHook());
            }
    );

    public static final ConfigType UTIL = of(
            "util",
            (id, section, functions) -> {
                ItemType.register(id, ItemType.UTIL);
                UtilConfigParser config = new UtilConfigParser(id, section, functions);
                BukkitCustomFishingPlugin.getInstance().getItemManager().registerItem(config.getItem());
                BukkitCustomFishingPlugin.getInstance().getLootManager().registerLoot(config.getLoot());
                BukkitCustomFishingPlugin.getInstance().getEffectManager().registerEffectModifier(config.getEffectModifier());
                BukkitCustomFishingPlugin.getInstance().getEventManager().registerEventCarrier(config.getEventCarrier());
            }
    );

    private static final ConfigType[] values = new ConfigType[] {ITEM, ENTITY, BLOCK, HOOK, ROD, BAIT, UTIL};

    public static ConfigType[] values() {
        return values;
    }

    private final String path;
    private TriConsumer<String, Section, Map<String, Node<ConfigParserFunction>>> argumentConsumer;

    public ConfigType(String path, TriConsumer<String, Section, Map<String, Node<ConfigParserFunction>>> argumentConsumer) {
        this.path = path;
        this.argumentConsumer = argumentConsumer;
    }

    public void argumentConsumer(TriConsumer<String, Section, Map<String, Node<ConfigParserFunction>>> argumentConsumer) {
        this.argumentConsumer = argumentConsumer;
    }

    public static ConfigType of(String path, TriConsumer<String, Section, Map<String, Node<ConfigParserFunction>>> argumentConsumer) {
        return new ConfigType(path, argumentConsumer);
    }

    public void parse(String id, Section section, Map<String, Node<ConfigParserFunction>> functions) {
        argumentConsumer.accept(id, section, functions);
    }

    public String path() {
        return path;
    }
}
