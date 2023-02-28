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

package net.momirealms.customfishing.manager;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.helper.Log;
import net.momirealms.customfishing.integration.*;
import net.momirealms.customfishing.integration.antigrief.*;
import net.momirealms.customfishing.integration.block.ItemsAdderBlockImpl;
import net.momirealms.customfishing.integration.block.OraxenBlockImpl;
import net.momirealms.customfishing.integration.block.VanillaBlockImpl;
import net.momirealms.customfishing.integration.item.*;
import net.momirealms.customfishing.integration.mob.MythicMobsMobImpl;
import net.momirealms.customfishing.integration.papi.PlaceholderManager;
import net.momirealms.customfishing.integration.quest.BattlePassCFQuest;
import net.momirealms.customfishing.integration.quest.ClueScrollCFQuest;
import net.momirealms.customfishing.integration.quest.NewBetonQuestCFQuest;
import net.momirealms.customfishing.integration.quest.OldBetonQuestCFQuest;
import net.momirealms.customfishing.integration.season.CustomCropsSeasonImpl;
import net.momirealms.customfishing.integration.season.RealisticSeasonsImpl;
import net.momirealms.customfishing.integration.skill.*;
import net.momirealms.customfishing.object.Function;
import net.momirealms.customfishing.util.AdventureUtil;
import net.momirealms.customfishing.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class IntegrationManager extends Function {

    private SeasonInterface seasonInterface;
    private SkillInterface skillInterface;
    private ItemInterface[] itemInterfaces;
    private MobInterface mobInterface;
    private BlockInterface blockInterface;
    private PlaceholderManager placeholderManager;
    private AntiGriefInterface[] antiGriefs;
    private VaultHook vaultHook;

    @Override
    public void load() {

        PluginManager pluginManager = Bukkit.getPluginManager();

        if (this.placeholderManager != null) {
            this.placeholderManager.unload();
        }

        this.placeholderManager = new PlaceholderManager();

        YamlConfiguration config = ConfigUtil.getConfig("config.yml");

        if (ConfigManager.vaultHook && pluginManager.getPlugin("Vault") != null) {
            vaultHook = new VaultHook();
            if (!vaultHook.initialize()) {
                ConfigManager.vaultHook = false;
                Log.warn("Failed to initialize Vault!");
            }
            else hookMessage("Vault");
        }

        this.blockInterface = new VanillaBlockImpl();

        List<ItemInterface> itemInterfaceList = new ArrayList<>();
        if (config.getBoolean("integrations.ItemsAdder") && pluginManager.getPlugin("ItemsAdder") != null) {
            this.blockInterface = new ItemsAdderBlockImpl();
            itemInterfaceList.add(new ItemsAdderItemImpl());
            hookMessage("ItemsAdder");
        }
        if (config.getBoolean("integrations.Oraxen") && pluginManager.getPlugin("Oraxen") != null) {
            this.blockInterface = new OraxenBlockImpl();
            itemInterfaceList.add(new OraxenItemImpl());
            hookMessage("Oraxen");
        }
        if (config.getBoolean("integrations.MMOItems") && pluginManager.getPlugin("MMOItems") != null) {
            itemInterfaceList.add(new MMOItemsItemImpl());
            hookMessage("MMOItems");
        }
        if (config.getBoolean("integrations.MythicMobs") && pluginManager.getPlugin("MythicMobs") != null) {
            itemInterfaceList.add(new MythicMobsItemImpl());
            this.mobInterface = new MythicMobsMobImpl();
            hookMessage("MythicMobs");
        }
        itemInterfaceList.add(new CustomFishingItemImpl());
        this.itemInterfaces = itemInterfaceList.toArray(new ItemInterface[0]);

        if (pluginManager.getPlugin("eco") != null) {
            EcoItemRegister.registerItems();
            hookMessage("eco");
        }

        if (config.getBoolean("integrations.RealisticSeasons", false) && pluginManager.getPlugin("RealisticSeasons") != null) {
            this.seasonInterface = new RealisticSeasonsImpl();
            hookMessage("RealisticSeasons");
        } else if (config.getBoolean("integrations.CustomCrops", false) && pluginManager.getPlugin("CustomCrops") != null) {
            this.seasonInterface = new CustomCropsSeasonImpl();
            hookMessage("CustomCrops");
        }
        if (config.getBoolean("integrations.mcMMO", false) && Bukkit.getPluginManager().getPlugin("mcMMO") != null) {
            this.skillInterface = new mcMMOImpl();
            hookMessage("mcMMO");
        } else if (config.getBoolean("integrations.MMOCore", false) && Bukkit.getPluginManager().getPlugin("MMOCore") != null) {
            this.skillInterface = new MMOCoreImpl(config.getString("other-settings.MMOCore-profession-name", "fishing"));
            hookMessage("MMOCore");
        } else if (config.getBoolean("integrations.AureliumSkills", false) && Bukkit.getPluginManager().getPlugin("AureliumSkills") != null) {
            this.skillInterface = new AureliumsImpl();
            hookMessage("AureliumSkills");
        } else if (config.getBoolean("integrations.EcoSkills", false) && Bukkit.getPluginManager().getPlugin("EcoSkills") != null) {
            this.skillInterface = new EcoSkillsImpl();
            hookMessage("EcoSkills");
        } else if (config.getBoolean("integrations.JobsReborn", false) && Bukkit.getPluginManager().getPlugin("Jobs") != null) {
            this.skillInterface = new JobsRebornImpl();
            hookMessage("JobsReborn");
        }

        List<AntiGriefInterface> antiGriefsList = new ArrayList<>();
        if (config.getBoolean("integrations.Residence",false)){
            if (Bukkit.getPluginManager().getPlugin("Residence") == null) Log.warn("Failed to initialize Residence!");
            else {
                antiGriefsList.add(new ResidenceHook());
                hookMessage("Residence");
            }
        }
        if (config.getBoolean("integrations.Kingdoms",false)){
            if (Bukkit.getPluginManager().getPlugin("Kingdoms") == null) Log.warn("Failed to initialize Kingdoms!");
            else {
                antiGriefsList.add(new KingdomsXHook());
                hookMessage("Kingdoms");
            }
        }
        if (config.getBoolean("integrations.WorldGuard",false)){
            if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) Log.warn("Failed to initialize WorldGuard!");
            else {
                antiGriefsList.add(new WorldGuardHook());
                hookMessage("WorldGuard");
            }
        }
        if (config.getBoolean("integrations.GriefDefender",false)){
            if(Bukkit.getPluginManager().getPlugin("GriefDefender") == null) Log.warn("Failed to initialize GriefDefender!");
            else {
                antiGriefsList.add(new GriefDefenderHook());
                hookMessage("GriefDefender");
            }
        }
        if (config.getBoolean("integrations.PlotSquared",false)){
            if(Bukkit.getPluginManager().getPlugin("PlotSquared") == null) Log.warn("Failed to initialize PlotSquared!");
            else {
                antiGriefsList.add(new PlotSquaredHook());
                hookMessage("PlotSquared");
            }
        }
        if (config.getBoolean("integrations.Towny",false)){
            if (Bukkit.getPluginManager().getPlugin("Towny") == null) Log.warn("Failed to initialize Towny!");
            else {
                antiGriefsList.add(new TownyHook());
                hookMessage("Towny");
            }
        }
        if (config.getBoolean("integrations.Lands",false)){
            if (Bukkit.getPluginManager().getPlugin("Lands") == null) Log.warn("Failed to initialize Lands!");
            else {
                antiGriefsList.add(new LandsHook());
                hookMessage("Lands");
            }
        }
        if (config.getBoolean("integrations.GriefPrevention",false)){
            if (Bukkit.getPluginManager().getPlugin("GriefPrevention") == null) Log.warn("Failed to initialize GriefPrevention!");
            else {
                antiGriefsList.add(new GriefPreventionHook());
                hookMessage("GriefPrevention");
            }
        }
        if (config.getBoolean("integrations.CrashClaim",false)){
            if (Bukkit.getPluginManager().getPlugin("CrashClaim") == null) Log.warn("Failed to initialize CrashClaim!");
            else {
                antiGriefsList.add(new CrashClaimHook());
                hookMessage("CrashClaim");
            }
        }
        if (config.getBoolean("integrations.BentoBox",false)){
            if (Bukkit.getPluginManager().getPlugin("BentoBox") == null) Log.warn("Failed to initialize BentoBox!");
            else {
                antiGriefsList.add(new BentoBoxHook());
                hookMessage("BentoBox");
            }
        }
        antiGriefs = antiGriefsList.toArray(new AntiGriefInterface[0]);
    }

    public void registerQuests() {
        if (Bukkit.getPluginManager().isPluginEnabled("ClueScrolls")) {
            ClueScrollCFQuest clueScrollCFQuest = new ClueScrollCFQuest();
            Bukkit.getPluginManager().registerEvents(clueScrollCFQuest, CustomFishing.plugin);
            hookMessage("ClueScrolls");
        }
        if (Bukkit.getPluginManager().isPluginEnabled("BetonQuest")) {
            if (Bukkit.getPluginManager().getPlugin("BetonQuest").getDescription().getVersion().startsWith("2.")) NewBetonQuestCFQuest.register();
            else OldBetonQuestCFQuest.register();
            hookMessage("BetonQuest");
        }
        if (Bukkit.getPluginManager().isPluginEnabled("BattlePass")) {
            BattlePassCFQuest.register();
            hookMessage("BattlePass");
        }
    }

    @Override
    public void unload() {
        this.seasonInterface = null;
        this.skillInterface = null;
        this.itemInterfaces = null;
        this.mobInterface = null;
        this.blockInterface = null;
        if (this.placeholderManager != null) {
            this.placeholderManager.unload();
            this.placeholderManager = null;
        }
    }

    @Nullable
    public SeasonInterface getSeasonInterface() {
        return seasonInterface;
    }

    @Nullable
    public SkillInterface getSkillInterface() {
        return skillInterface;
    }

    @NotNull
    public ItemInterface[] getItemInterfaces() {
        return itemInterfaces;
    }

    @Nullable
    public MobInterface getMobInterface() {
        return mobInterface;
    }

    @NotNull
    public BlockInterface getBlockInterface() {
        return blockInterface;
    }

    @NotNull
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    @NotNull
    public AntiGriefInterface[] getAntiGriefs() {
        return antiGriefs;
    }

    @NotNull
    public ItemStack build(String key) {
        for (ItemInterface itemInterface : getItemInterfaces()) {
            ItemStack itemStack = itemInterface.build(key);
            if (itemStack != null) {
                return itemStack;
            }
        }
        return new ItemStack(Material.AIR);
    }

    public void loseCustomDurability(ItemStack itemStack, Player player) {
        Damageable damageable = (Damageable) itemStack.getItemMeta();
        if (damageable.isUnbreakable()) return;
        for (ItemInterface itemInterface : getItemInterfaces()) {
            if (itemInterface.loseCustomDurability(itemStack, player)) {
                return;
            }
        }
    }

    private void hookMessage(String plugin){
        AdventureUtil.consoleMessage("[CustomFishing] <white>" + plugin + " Hooked!");
    }

    @Nullable
    public VaultHook getVaultHook() {
        return vaultHook;
    }
}
