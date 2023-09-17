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
import java.util.Objects;

public class IntegrationManagerImpl implements IntegrationManager {

    private final CustomFishingPlugin plugin;
    private final HashMap<String, LevelInterface> levelPluginMap;
    private final HashMap<String, EnchantmentInterface> enchantments;
    private SeasonInterface seasonInterface;

    public IntegrationManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.levelPluginMap = new HashMap<>();
        this.enchantments = new HashMap<>();
        this.load();
    }

    public void disable() {
        this.enchantments.clear();
        this.levelPluginMap.clear();
    }

    public void load() {
        if (plugin.isHookedPluginEnabled("ItemsAdder")) {
            plugin.getItemManager().registerItemLibrary(new ItemsAdderItemImpl());
            plugin.getBlockManager().registerBlockLibrary(new ItemsAdderBlockImpl());
            plugin.getEntityManager().registerEntityLibrary(new ItemsAdderEntityImpl());
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
                plugin.getItemManager().registerCustomItem("item", "mcmmo", new McMMOBuildableItem());
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
        if (plugin.isHookedPluginEnabled("Vault")) {
            VaultHook.initialize();
        }
        if (plugin.isHookedPluginEnabled("ClueScrolls")) {
            ClueScrollsHook clueScrollsHook = new ClueScrollsHook();
            Bukkit.getPluginManager().registerEvents(clueScrollsHook, plugin);
            hookMessage("ClueScrolls");
        }
        if (plugin.isHookedPluginEnabled("BetonQuest")) {
            if (Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("BetonQuest")).getPluginMeta().getVersion().startsWith("2")) {
                BetonQuestHook.register();
            }
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
        LogUtils.info( plugin + " hooked!");
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
