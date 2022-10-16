package net.momirealms.customfishing.manager;

import net.momirealms.customfishing.Function;
import net.momirealms.customfishing.integration.ItemInterface;
import net.momirealms.customfishing.integration.MobInterface;
import net.momirealms.customfishing.integration.SeasonInterface;
import net.momirealms.customfishing.integration.SkillInterface;
import net.momirealms.customfishing.integration.item.*;
import net.momirealms.customfishing.integration.mob.MythicMobsMobHook;
import net.momirealms.customfishing.integration.papi.PlaceholderManager;
import net.momirealms.customfishing.integration.season.RealisticSeasonsImpl;
import net.momirealms.customfishing.integration.skill.*;
import net.momirealms.customfishing.util.AdventureUtil;
import net.momirealms.customfishing.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
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
    private PlaceholderManager placeholderManager;

    public IntegrationManager() {
        load();
    }

    @Override
    public void load() {

        PluginManager pluginManager = Bukkit.getPluginManager();

        if (this.placeholderManager != null) {
            this.placeholderManager.unload();
        }
        if (pluginManager.getPlugin("PlaceholderAPI") != null) {
            this.placeholderManager = new PlaceholderManager();
        }

        List<ItemInterface> itemInterfaceList = new ArrayList<>();
        itemInterfaceList.add(new CustomFishingItemHook());
        if (pluginManager.getPlugin("ItemsAdder") != null) itemInterfaceList.add(new ItemsAdderItemHook());
        if (pluginManager.getPlugin("Oraxen") != null) itemInterfaceList.add(new OraxenItemHook());
        if (pluginManager.getPlugin("MMOItems") != null) itemInterfaceList.add(new MMOItemsItemHook());
        if (pluginManager.getPlugin("MythicMobs") != null) {
            itemInterfaceList.add(new MythicMobsItemHook());
            this.mobInterface = new MythicMobsMobHook();
        }
        this.itemInterfaces = itemInterfaceList.toArray(new ItemInterface[0]);


        if (pluginManager.getPlugin("eco") != null) {
            EcoItemHook.registerItems();
        }

        YamlConfiguration config = ConfigUtil.getConfig("config.yml");
        if (config.getBoolean("integration.RealisticSeasons", false) && pluginManager.getPlugin("RealisticSeasons") != null) {
            this.seasonInterface = new RealisticSeasonsImpl();
            hookMessage("RealisticSeasons");
        } else if (config.getBoolean("integration.CustomCrops", false) && pluginManager.getPlugin("CustomCrops") != null) {
            //TODO
            this.seasonInterface = null;
            hookMessage("CustomCrops");
        }
        if (config.getBoolean("integration.mcMMO", false) && Bukkit.getPluginManager().getPlugin("mcMMO") != null) {
            this.skillInterface = new mcMMOHook();
            hookMessage("mcMMO");
        } else if (config.getBoolean("integration.MMOCore", false) && Bukkit.getPluginManager().getPlugin("MMOCore") != null) {
            this.skillInterface = new MMOCoreHook();
            hookMessage("MMOCore");
        } else if (config.getBoolean("integration.AureliumSkills", false) && Bukkit.getPluginManager().getPlugin("AureliumSkills") != null) {
            this.skillInterface = new AureliumsHook();
            hookMessage("AureliumSkills");
        } else if (config.getBoolean("integration.EcoSkills", false) && Bukkit.getPluginManager().getPlugin("EcoSkills") != null) {
            this.skillInterface = new EcoSkillsHook();
            hookMessage("EcoSkills");
        } else if (config.getBoolean("integration.JobsReborn", false) && Bukkit.getPluginManager().getPlugin("Jobs") != null) {
            this.skillInterface = new JobsRebornHook();
            hookMessage("JobsReborn");
        }
    }

    @Override
    public void unload() {
        this.seasonInterface = null;
        this.skillInterface = null;
        this.itemInterfaces = null;
        this.mobInterface = null;
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

    @Nullable
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    @NotNull
    public ItemStack build(String key) {
        for (ItemInterface itemInterface : itemInterfaces) {
            ItemStack itemStack = itemInterface.build(key);
            if (itemStack != null) {
                return itemStack;
            }
        }
        return new ItemStack(Material.AIR);
    }

    private void hookMessage(String plugin){
        AdventureUtil.consoleMessage("[CustomFishing] <white>" + plugin + " Hooked!");
    }
}
