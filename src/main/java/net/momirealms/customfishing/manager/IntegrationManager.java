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
import net.momirealms.customfishing.integration.block.ItemsAdderBlockImpl;
import net.momirealms.customfishing.integration.block.OraxenBlockImpl;
import net.momirealms.customfishing.integration.block.VanillaBlockImpl;
import net.momirealms.customfishing.integration.enchantment.AEImpl;
import net.momirealms.customfishing.integration.enchantment.VanillaImpl;
import net.momirealms.customfishing.integration.item.*;
import net.momirealms.customfishing.integration.job.EcoJobsImpl;
import net.momirealms.customfishing.integration.job.JobsRebornImpl;
import net.momirealms.customfishing.integration.mob.MythicMobsMobImpl;
import net.momirealms.customfishing.integration.papi.PlaceholderManager;
import net.momirealms.customfishing.integration.quest.BattlePassCFQuest;
import net.momirealms.customfishing.integration.quest.ClueScrollCFQuest;
import net.momirealms.customfishing.integration.quest.NewBetonQuestCFQuest;
import net.momirealms.customfishing.integration.quest.OldBetonQuestCFQuest;
import net.momirealms.customfishing.integration.season.CustomCropsSeasonImpl;
import net.momirealms.customfishing.integration.season.RealisticSeasonsImpl;
import net.momirealms.customfishing.integration.skill.AureliumsImpl;
import net.momirealms.customfishing.integration.skill.EcoSkillsImpl;
import net.momirealms.customfishing.integration.skill.MMOCoreImpl;
import net.momirealms.customfishing.integration.skill.mcMMOImpl;
import net.momirealms.customfishing.object.Function;
import net.momirealms.customfishing.util.AdventureUtils;
import net.momirealms.customfishing.util.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
    private JobInterface jobInterface;
    private EnchantmentInterface enchantmentInterface;
    private final PlaceholderManager placeholderManager;
    private VaultHook vaultHook;
    private final CustomFishing plugin;
    private final PluginManager pluginManager;

    public IntegrationManager(CustomFishing plugin) {
        this.plugin = plugin;
        this.pluginManager = Bukkit.getPluginManager();
        this.placeholderManager = new PlaceholderManager(plugin);
    }

    @Override
    public void load() {
        this.placeholderManager.load();
        hookSeasons();
        hookSkills();
        hookJobs();
        hookItems();
        hookVault();
        hookMobs();
        hookBlocks();
        hookEnchants();
    }

    @Override
    public void unload() {
        this.seasonInterface = null;
        this.skillInterface = null;
        this.itemInterfaces = null;
        this.mobInterface = null;
        this.blockInterface = null;
        this.jobInterface = null;
        this.enchantmentInterface = null;
        this.placeholderManager.unload();
    }

    private void hookEnchants() {
        if (pluginManager.isPluginEnabled("AdvancedEnchantments")) {
            this.enchantmentInterface = new AEImpl();
            hookMessage("AdvancedEnchantments");
        }
        else if (pluginManager.isPluginEnabled("EcoEnchants")) {
            this.enchantmentInterface = new VanillaImpl();
            hookMessage("EcoEnchants");
        }
        else {
            this.enchantmentInterface = new VanillaImpl();
        }
    }

    private void hookMobs() {
        if (pluginManager.isPluginEnabled("MythicMobs") && pluginManager.getPlugin("MythicMobs").getDescription().getVersion().startsWith("5")) {
            this.mobInterface = new MythicMobsMobImpl();
        }
    }

    private void hookBlocks() {
        if (pluginManager.isPluginEnabled("Oraxen")) {
            this.blockInterface = new OraxenBlockImpl();
        }
        else if (pluginManager.isPluginEnabled("ItemsAdder")) {
            this.blockInterface = new ItemsAdderBlockImpl();
        }
        else {
            this.blockInterface = new VanillaBlockImpl();
        }
    }

    private void hookSeasons() {
        if (pluginManager.isPluginEnabled("RealisticSeasons")) {
            this.seasonInterface = new RealisticSeasonsImpl();
            hookMessage("RealisticSeasons");
        } else if (pluginManager.isPluginEnabled("CustomCrops")) {
            this.seasonInterface = new CustomCropsSeasonImpl();
            hookMessage("CustomCrops");
        }
    }

    private void hookSkills() {
        if (pluginManager.isPluginEnabled("mcMMO")) {
            this.skillInterface = new mcMMOImpl();
            hookMessage("mcMMO");
        } else if (pluginManager.isPluginEnabled("MMOCore")) {
            this.skillInterface = new MMOCoreImpl(ConfigUtils.getConfig("config.yml").getString("other-settings.MMOCore-profession-name", "fishing"));
            hookMessage("MMOCore");
        } else if (pluginManager.isPluginEnabled("AureliumSkills")) {
            this.skillInterface = new AureliumsImpl();
            hookMessage("AureliumSkills");
        } else if (pluginManager.isPluginEnabled("EcoSkills")) {
            this.skillInterface = new EcoSkillsImpl();
            hookMessage("EcoSkills");
        }
    }

    private void hookJobs() {
        if (pluginManager.isPluginEnabled("Jobs")) {
            this.jobInterface = new JobsRebornImpl();
            hookMessage("JobsReborn");
        } else if (pluginManager.isPluginEnabled("EcoJobs")) {
            this.jobInterface = new EcoJobsImpl();
            hookMessage("EcoJobs");
        }
    }

    private void hookVault() {
        if (pluginManager.isPluginEnabled("Vault")) {
            vaultHook = new VaultHook();
            if (!vaultHook.initialize()) {
                Log.warn("Failed to initialize Vault!");
            }
            else hookMessage("Vault");
        }
    }

    private void hookItems() {
        List<ItemInterface> itemInterfaceList = new ArrayList<>();
        if (pluginManager.isPluginEnabled("ItemsAdder")) {
            itemInterfaceList.add(new ItemsAdderItemImpl());
            hookMessage("ItemsAdder");
        }
        if (pluginManager.isPluginEnabled("Oraxen")) {
            itemInterfaceList.add(new OraxenItemImpl());
            hookMessage("Oraxen");
        }
        if (pluginManager.isPluginEnabled("MMOItems")) {
            itemInterfaceList.add(new MMOItemsItemImpl());
            hookMessage("MMOItems");
        }
        if (pluginManager.isPluginEnabled("MythicMobs") && pluginManager.getPlugin("MythicMobs").getDescription().getVersion().startsWith("5")) {
            itemInterfaceList.add(new MythicMobsItemImpl());
            hookMessage("MythicMobs");
        }
        itemInterfaceList.add(new CustomFishingItemImpl(plugin));
        this.itemInterfaces = itemInterfaceList.toArray(new ItemInterface[0]);

        if (pluginManager.isPluginEnabled("eco")) {
            EcoItemRegister.registerItems();
            hookMessage("eco");
        }
    }

    public void registerQuests() {
        if (pluginManager.isPluginEnabled("ClueScrolls")) {
            ClueScrollCFQuest clueScrollCFQuest = new ClueScrollCFQuest();
            Bukkit.getPluginManager().registerEvents(clueScrollCFQuest, plugin);
            hookMessage("ClueScrolls");
        }
        if (pluginManager.isPluginEnabled("BetonQuest")) {
            if (Bukkit.getPluginManager().getPlugin("BetonQuest").getDescription().getVersion().startsWith("2")) NewBetonQuestCFQuest.register();
            else OldBetonQuestCFQuest.register();
            hookMessage("BetonQuest");
        }
        if (pluginManager.isPluginEnabled("BattlePass")) {
            BattlePassCFQuest.register();
            hookMessage("BattlePass");
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
    public EnchantmentInterface getEnchantmentInterface() {
        return enchantmentInterface;
    }

    @Nullable
    public JobInterface getJobInterface() {
        return jobInterface;
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
        AdventureUtils.consoleMessage("[CustomFishing] " + plugin + " hooked!");
    }

    @Nullable
    public VaultHook getVaultHook() {
        return vaultHook;
    }
}
