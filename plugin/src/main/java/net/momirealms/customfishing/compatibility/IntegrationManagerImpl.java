package net.momirealms.customfishing.compatibility;

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.integration.EnchantmentInterface;
import net.momirealms.customfishing.api.integration.LevelInterface;
import net.momirealms.customfishing.api.integration.SeasonInterface;
import net.momirealms.customfishing.api.manager.IntegrationManager;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.compatibility.enchant.AdvancedEnchantmentsImpl;
import net.momirealms.customfishing.compatibility.enchant.VanillaEnchantmentsImpl;
import net.momirealms.customfishing.compatibility.item.*;
import net.momirealms.customfishing.compatibility.level.*;
import net.momirealms.customfishing.compatibility.season.CustomCropsSeasonImpl;
import net.momirealms.customfishing.compatibility.season.RealisticSeasonsImpl;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IntegrationManagerImpl implements IntegrationManager {

    private final CustomFishingPlugin plugin;
    private final HashMap<String, LevelInterface> levelPluginMap;
    private final HashMap<String, EnchantmentInterface> enchantments;
    private SeasonInterface seasonInterface;

    public IntegrationManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.levelPluginMap = new HashMap<>();
        this.enchantments = new HashMap<>();
        this.init();
    }

    public void disable() {
        this.enchantments.clear();
        this.levelPluginMap.clear();
    }

    public void init() {
        if (plugin.isHookedPluginEnabled("ItemsAdder")) {
            plugin.getItemManager().registerItemLibrary(new ItemsAdderItemImpl());
            hookMessage("ItemsAdder");
        }
        if (plugin.isHookedPluginEnabled("MMOItems")) {
            plugin.getItemManager().registerItemLibrary(new MMOItemsItemImpl());
            hookMessage("MMOItems");
        }
        if (plugin.isHookedPluginEnabled("Oraxen")) {
            plugin.getItemManager().registerItemLibrary(new OraxenItemImpl());
            hookMessage("Oraxen");
        }
        if (plugin.isHookedPluginEnabled("NeigeItems")) {
            plugin.getItemManager().registerItemLibrary(new NeigeItemsItemImpl());
            hookMessage("NeigeItems");
        }
        if (plugin.isHookedPluginEnabled("MythicMobs")) {
            plugin.getItemManager().registerItemLibrary(new MythicMobsItemImpl());
            hookMessage("MythicMobs");
        }
        if (plugin.isHookedPluginEnabled("EcoJobs")) {
            registerLevelPlugin("EcoJobs", new EcoJobsImpl());
            hookMessage("EcoJobs");
        }
        if (plugin.isHookedPluginEnabled("EcoSkills")) {
            registerLevelPlugin("EcoSkills", new EcoSkillsImpl());
            hookMessage("EcoSkills");
        }
        if (plugin.isHookedPluginEnabled("Jobs")) {
            registerLevelPlugin("JobsReborn", new JobsRebornImpl());
            hookMessage("Jobs");
        }
        if (plugin.isHookedPluginEnabled("MMOCore")) {
            registerLevelPlugin("MMOCore", new MMOCoreImpl());
            hookMessage("MMOCore");
        }
        if (plugin.isHookedPluginEnabled("mcMMO")) {
            try {
                plugin.getItemManager().registerCustomItem("loot", "mcmmo", new McMMOBuildableItem());
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                LogUtils.warn("Failed to initialize mcMMO Treasure");
            }
            registerLevelPlugin("mcMMO", new McMMOImpl());
            hookMessage("mcMMO");
        }
        if (plugin.isHookedPluginEnabled("AureliumSkills")) {
            registerLevelPlugin("AureliumSkills", new AureliumSkillsImpl());
            hookMessage("AureliumSkills");
        }
        if (plugin.isHookedPluginEnabled("EcoEnchants")) {
            this.enchantments.put("EcoEnchants", new VanillaEnchantmentsImpl());
            hookMessage("EcoEnchants");
        } else {
            this.enchantments.put("vanilla", new VanillaEnchantmentsImpl());
        }
        if (plugin.isHookedPluginEnabled("AdvancedEnchantments")) {
            this.enchantments.put("AdvancedEnchantments", new AdvancedEnchantmentsImpl());
            hookMessage("AdvancedEnchantments");
        }
        if (plugin.isHookedPluginEnabled("RealisticSeasons")) {
            this.seasonInterface = new RealisticSeasonsImpl();
        } else if (plugin.isHookedPluginEnabled("CustomCrops")) {
            this.seasonInterface = new CustomCropsSeasonImpl();
        }
    }

    @Override
    public boolean registerLevelPlugin(String plugin, LevelInterface level) {
        if (levelPluginMap.containsKey(plugin)) return false;
        levelPluginMap.put(plugin, level);
        return true;
    }

    @Override
    public boolean unregisterLevelPlugin(String plugin) {
        return levelPluginMap.remove(plugin) != null;
    }

    @Override
    public boolean registerEnchantment(String plugin, EnchantmentInterface enchantment) {
        if (enchantments.containsKey(plugin)) return false;
        enchantments.put(plugin, enchantment);
        return true;
    }

    @Override
    public boolean unregisterEnchantment(String plugin) {
        return enchantments.remove(plugin) != null;
    }

    private void hookMessage(String plugin) {
        AdventureManagerImpl.getInstance().sendConsoleMessage("[CustomFishing] <green>" + plugin + "</green> hooked!");
    }

    @Override
    public LevelInterface getLevelHook(String plugin) {
        return levelPluginMap.get(plugin);
    }

    @Override
    public List<String> getEnchantments(ItemStack itemStack) {
        ArrayList<String> list = new ArrayList<>();
        for (EnchantmentInterface enchantmentInterface : enchantments.values()) {
            list.addAll(enchantmentInterface.getEnchants(itemStack));
        }
        return list;
    }

    @Nullable
    public SeasonInterface getSeasonInterface() {
        return seasonInterface;
    }

    @Override
    public void setSeasonInterface(SeasonInterface season) {
        this.seasonInterface = season;
    }
}
