package net.momirealms.customfishing.manager;

import net.momirealms.customfishing.helper.Log;
import net.momirealms.customfishing.integration.*;
import net.momirealms.customfishing.integration.antigrief.*;
import net.momirealms.customfishing.integration.block.ItemsAdderBlockImpl;
import net.momirealms.customfishing.integration.block.OraxenBlockImpl;
import net.momirealms.customfishing.integration.block.VanillaBlockImpl;
import net.momirealms.customfishing.integration.item.*;
import net.momirealms.customfishing.integration.mob.MythicMobsMobImpl;
import net.momirealms.customfishing.integration.papi.PlaceholderManager;
import net.momirealms.customfishing.integration.season.CustomCropsSeasonImpl;
import net.momirealms.customfishing.integration.season.RealisticSeasonsImpl;
import net.momirealms.customfishing.integration.skill.*;
import net.momirealms.customfishing.object.Function;
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
    private BlockInterface blockInterface;
    private PlaceholderManager placeholderManager;
    private AntiGriefInterface[] antiGriefs;

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
        itemInterfaceList.add(new CustomFishingItemImpl());
        if (pluginManager.getPlugin("ItemsAdder") != null) itemInterfaceList.add(new ItemsAdderItemImpl());
        if (pluginManager.getPlugin("Oraxen") != null) itemInterfaceList.add(new OraxenItemImpl());
        if (pluginManager.getPlugin("MMOItems") != null) itemInterfaceList.add(new MMOItemsItemImpl());
        if (pluginManager.getPlugin("MythicMobs") != null) {
            itemInterfaceList.add(new MythicMobsItemImpl());
            this.mobInterface = new MythicMobsMobImpl();
        }
        this.itemInterfaces = itemInterfaceList.toArray(new ItemInterface[0]);

        if (pluginManager.getPlugin("ItemsAdder") != null) {
            this.blockInterface = new ItemsAdderBlockImpl();
        } else if (pluginManager.getPlugin("Oraxen") != null) {
            this.blockInterface = new OraxenBlockImpl();
        } else {
            this.blockInterface = new VanillaBlockImpl();
        }

        if (pluginManager.getPlugin("eco") != null) {
            EcoItemRegister.registerItems();
        }

        YamlConfiguration config = ConfigUtil.getConfig("config.yml");
        if (config.getBoolean("integration.RealisticSeasons", false) && pluginManager.getPlugin("RealisticSeasons") != null) {
            this.seasonInterface = new RealisticSeasonsImpl();
            hookMessage("RealisticSeasons");
        } else if (config.getBoolean("integration.CustomCrops", false) && pluginManager.getPlugin("CustomCrops") != null) {
            this.seasonInterface = new CustomCropsSeasonImpl();
            hookMessage("CustomCrops");
        }
        if (config.getBoolean("integration.mcMMO", false) && Bukkit.getPluginManager().getPlugin("mcMMO") != null) {
            this.skillInterface = new mcMMOImpl();
            hookMessage("mcMMO");
        } else if (config.getBoolean("integration.MMOCore", false) && Bukkit.getPluginManager().getPlugin("MMOCore") != null) {
            this.skillInterface = new MMOCoreImpl();
            hookMessage("MMOCore");
        } else if (config.getBoolean("integration.AureliumSkills", false) && Bukkit.getPluginManager().getPlugin("AureliumSkills") != null) {
            this.skillInterface = new AureliumsImpl();
            hookMessage("AureliumSkills");
        } else if (config.getBoolean("integration.EcoSkills", false) && Bukkit.getPluginManager().getPlugin("EcoSkills") != null) {
            this.skillInterface = new EcoSkillsImpl();
            hookMessage("EcoSkills");
        } else if (config.getBoolean("integration.JobsReborn", false) && Bukkit.getPluginManager().getPlugin("Jobs") != null) {
            this.skillInterface = new JobsRebornImpl();
            hookMessage("JobsReborn");
        }

        List<AntiGriefInterface> antiGriefsList = new ArrayList<>();
        if (config.getBoolean("integration.Residence",false)){
            if (Bukkit.getPluginManager().getPlugin("Residence") == null) Log.warn("Failed to initialize Residence!");
            else {antiGriefsList.add(new net.momirealms.customfishing.integration.antigrief.ResidenceHook());hookMessage("Residence");}
        }
        if (config.getBoolean("integration.Kingdoms",false)){
            if (Bukkit.getPluginManager().getPlugin("Kingdoms") == null) Log.warn("Failed to initialize Kingdoms!");
            else {antiGriefsList.add(new KingdomsXHook());hookMessage("Kingdoms");}
        }
        if (config.getBoolean("integration.WorldGuard",false)){
            if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) Log.warn("Failed to initialize WorldGuard!");
            else {antiGriefsList.add(new WorldGuardHook());hookMessage("WorldGuard");}
        }
        if (config.getBoolean("integration.GriefDefender",false)){
            if(Bukkit.getPluginManager().getPlugin("GriefDefender") == null) Log.warn("Failed to initialize GriefDefender!");
            else {antiGriefsList.add(new GriefDefenderHook());hookMessage("GriefDefender");}
        }
        if (config.getBoolean("integration.PlotSquared",false)){
            if(Bukkit.getPluginManager().getPlugin("PlotSquared") == null) Log.warn("Failed to initialize PlotSquared!");
            else {antiGriefsList.add(new PlotSquaredHook());hookMessage("PlotSquared");}
        }
        if (config.getBoolean("integration.Towny",false)){
            if (Bukkit.getPluginManager().getPlugin("Towny") == null) Log.warn("Failed to initialize Towny!");
            else {antiGriefsList.add(new TownyHook());hookMessage("Towny");}
        }
        if (config.getBoolean("integration.Lands",false)){
            if (Bukkit.getPluginManager().getPlugin("Lands") == null) Log.warn("Failed to initialize Lands!");
            else {antiGriefsList.add(new LandsHook());hookMessage("Lands");}
        }
        if (config.getBoolean("integration.GriefPrevention",false)){
            if (Bukkit.getPluginManager().getPlugin("GriefPrevention") == null) Log.warn("Failed to initialize GriefPrevention!");
            else {antiGriefsList.add(new GriefPreventionHook());hookMessage("GriefPrevention");}
        }
        if (config.getBoolean("integration.CrashClaim",false)){
            if (Bukkit.getPluginManager().getPlugin("CrashClaim") == null) Log.warn("Failed to initialize CrashClaim!");
            else {antiGriefsList.add(new CrashClaimHook());hookMessage("CrashClaim");}
        }
        if (config.getBoolean("integration.BentoBox",false)){
            if (Bukkit.getPluginManager().getPlugin("BentoBox") == null) Log.warn("Failed to initialize BentoBox!");
            else {antiGriefsList.add(new BentoBoxHook());hookMessage("BentoBox");}
        }
        antiGriefs = antiGriefsList.toArray(new AntiGriefInterface[0]);
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

    @Nullable
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    @NotNull
    public AntiGriefInterface[] getAntiGriefs() {
        return antiGriefs;
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
