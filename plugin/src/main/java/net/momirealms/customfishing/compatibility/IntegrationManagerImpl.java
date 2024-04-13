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

package net.momirealms.customfishing.compatibility;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.integration.EnchantmentInterface;
import net.momirealms.customfishing.api.integration.LevelInterface;
import net.momirealms.customfishing.api.integration.SeasonInterface;
import net.momirealms.customfishing.api.manager.IntegrationManager;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.compatibility.block.ItemsAdderBlockImpl;
import net.momirealms.customfishing.compatibility.enchant.AdvancedEnchantmentsImpl;
import net.momirealms.customfishing.compatibility.enchant.VanillaEnchantmentsImpl;
import net.momirealms.customfishing.compatibility.entity.ItemsAdderEntityImpl;
import net.momirealms.customfishing.compatibility.entity.MythicEntityImpl;
import net.momirealms.customfishing.compatibility.item.*;
import net.momirealms.customfishing.compatibility.level.*;
import net.momirealms.customfishing.compatibility.quest.BattlePassHook;
import net.momirealms.customfishing.compatibility.quest.BetonQuestHook;
import net.momirealms.customfishing.compatibility.quest.ClueScrollsHook;
import net.momirealms.customfishing.compatibility.season.CustomCropsSeasonImpl;
import net.momirealms.customfishing.compatibility.season.RealisticSeasonsImpl;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IntegrationManagerImpl implements IntegrationManager {

    private final CustomFishingPlugin plugin;
    private final HashMap<String, LevelInterface> levelPluginMap;
    private final HashMap<String, EnchantmentInterface> enchantmentPluginMap;
    private SeasonInterface seasonInterface;

    public IntegrationManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.levelPluginMap = new HashMap<>();
        this.enchantmentPluginMap = new HashMap<>();
        this.load();
    }

    public void disable() {
        this.enchantmentPluginMap.clear();
        this.levelPluginMap.clear();
    }

    public void load() {
        if (Bukkit.getPluginManager().getPlugin("ItemsAdder") != null) {
            plugin.getItemManager().registerItemLibrary(new ItemsAdderItemImpl());
            plugin.getBlockManager().registerBlockLibrary(new ItemsAdderBlockImpl());
            plugin.getEntityManager().registerEntityLibrary(new ItemsAdderEntityImpl());
            hookMessage("ItemsAdder");
        }
        if (Bukkit.getPluginManager().getPlugin("MMOItems") != null) {
            plugin.getItemManager().registerItemLibrary(new MMOItemsItemImpl());
            hookMessage("MMOItems");
        }
        if (Bukkit.getPluginManager().getPlugin("Oraxen") != null) {
            plugin.getItemManager().registerItemLibrary(new OraxenItemImpl());
            hookMessage("Oraxen");
        }
        if (plugin.isHookedPluginEnabled("Zaphkiel")) {
            plugin.getItemManager().registerItemLibrary(new ZaphkielItemImpl());
            hookMessage("Zaphkiel");
        }
        if (plugin.isHookedPluginEnabled("NeigeItems")) {
            plugin.getItemManager().registerItemLibrary(new NeigeItemsItemImpl());
            hookMessage("NeigeItems");
        }
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            plugin.getItemManager().registerItemLibrary(new MythicMobsItemImpl());
            plugin.getEntityManager().registerEntityLibrary(new MythicEntityImpl());
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
        if (Bukkit.getPluginManager().getPlugin("Jobs") != null) {
            registerLevelPlugin("JobsReborn", new JobsRebornImpl());
            hookMessage("JobsReborn");
        }
        if (plugin.isHookedPluginEnabled("MMOCore")) {
            registerLevelPlugin("MMOCore", new MMOCoreImpl());
            hookMessage("MMOCore");
        }
        if (plugin.isHookedPluginEnabled("mcMMO")) {
            try {
                plugin.getItemManager().registerItemLibrary(new McMMOTreasureImpl());
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
        if (plugin.isHookedPluginEnabled("AuraSkills")) {
            registerLevelPlugin("AuraSkills", new AuraSkillsImpl());
            hookMessage("AuraSkills");
        }
        if (plugin.isHookedPluginEnabled("EcoEnchants")) {
            this.enchantmentPluginMap.put("EcoEnchants", new VanillaEnchantmentsImpl());
            hookMessage("EcoEnchants");
        } else {
            this.enchantmentPluginMap.put("vanilla", new VanillaEnchantmentsImpl());
        }
        if (plugin.isHookedPluginEnabled("AdvancedEnchantments")) {
            this.enchantmentPluginMap.put("AdvancedEnchantments", new AdvancedEnchantmentsImpl());
            hookMessage("AdvancedEnchantments");
        }
        if (plugin.isHookedPluginEnabled("RealisticSeasons")) {
            this.seasonInterface = new RealisticSeasonsImpl();
        } else if (plugin.isHookedPluginEnabled("CustomCrops")) {
            this.seasonInterface = new CustomCropsSeasonImpl();
        }
        if (plugin.isHookedPluginEnabled("Vault")) {
            VaultHook.initialize();
        }
        if (plugin.isHookedPluginEnabled("BattlePass")){
            BattlePassHook battlePassHook = new BattlePassHook();
            battlePassHook.register();
            hookMessage("BattlePass");
        }
        if (plugin.isHookedPluginEnabled("ClueScrolls")) {
            ClueScrollsHook clueScrollsHook = new ClueScrollsHook();
            clueScrollsHook.register();
            hookMessage("ClueScrolls");
        }
        if (plugin.isHookedPluginEnabled("BetonQuest")) {
            BetonQuestHook.register();
            hookMessage("BetonQuest");
        }
//        if (plugin.isHookedPluginEnabled("NotQuests")) {
//            NotQuestHook notQuestHook = new NotQuestHook();
//            notQuestHook.register();
//            hookMessage("NotQuests");
//        }
    }

    /**
     * Registers a level plugin with the specified name.
     *
     * @param plugin The name of the level plugin.
     * @param level The implementation of the LevelInterface.
     * @return true if the registration was successful, false if the plugin name is already registered.
     */
    @Override
    public boolean registerLevelPlugin(String plugin, LevelInterface level) {
        if (levelPluginMap.containsKey(plugin)) return false;
        levelPluginMap.put(plugin, level);
        return true;
    }

    /**
     * Unregisters a level plugin with the specified name.
     *
     * @param plugin The name of the level plugin to unregister.
     * @return true if the unregistration was successful, false if the plugin name is not found.
     */
    @Override
    public boolean unregisterLevelPlugin(String plugin) {
        return levelPluginMap.remove(plugin) != null;
    }

    /**
     * Registers an enchantment provided by a plugin.
     *
     * @param plugin      The name of the plugin providing the enchantment.
     * @param enchantment The enchantment to register.
     * @return true if the registration was successful, false if the enchantment name is already in use.
     */
    @Override
    public boolean registerEnchantment(String plugin, EnchantmentInterface enchantment) {
        if (enchantmentPluginMap.containsKey(plugin)) return false;
        enchantmentPluginMap.put(plugin, enchantment);
        return true;
    }

    /**
     * Unregisters an enchantment provided by a plugin.
     *
     * @param plugin The name of the plugin providing the enchantment.
     * @return true if the enchantment was successfully unregistered, false if the enchantment was not found.
     */
    @Override
    public boolean unregisterEnchantment(String plugin) {
        return enchantmentPluginMap.remove(plugin) != null;
    }

    private void hookMessage(String plugin) {
        LogUtils.info( plugin + " hooked!");
    }

    /**
     * Get the LevelInterface provided by a plugin.
     *
     * @param plugin The name of the plugin providing the LevelInterface.
     * @return The LevelInterface provided by the specified plugin, or null if the plugin is not registered.
     */
    @Override
    @Nullable
    public LevelInterface getLevelPlugin(String plugin) {
        return levelPluginMap.get(plugin);
    }

    /**
     * Get an enchantment plugin by its plugin name.
     *
     * @param plugin The name of the enchantment plugin.
     * @return The enchantment plugin interface, or null if not found.
     */
    @Override
    @Nullable
    public EnchantmentInterface getEnchantmentPlugin(String plugin) {
        return enchantmentPluginMap.get(plugin);
    }

    /**
     * Get a list of enchantment keys with level applied to the given ItemStack.
     *
     * @param itemStack The ItemStack to check for enchantments.
     * @return A list of enchantment names applied to the ItemStack.
     */
    @Override
    public List<String> getEnchantments(ItemStack itemStack) {
        ArrayList<String> list = new ArrayList<>();
        for (EnchantmentInterface enchantmentInterface : enchantmentPluginMap.values()) {
            list.addAll(enchantmentInterface.getEnchants(itemStack));
        }
        return list;
    }

    /**
     * Get the current season interface, if available.
     *
     * @return The current season interface, or null if not available.
     */
    @Nullable
    public SeasonInterface getSeasonInterface() {
        return seasonInterface;
    }

    /**
     * Set the current season interface.
     *
     * @param season The season interface to set.
     */
    @Override
    public void setSeasonInterface(SeasonInterface season) {
        this.seasonInterface = season;
    }
}
