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

package net.momirealms.customfishing.bukkit.integration;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.integration.*;
import net.momirealms.customfishing.bukkit.block.BukkitBlockManager;
import net.momirealms.customfishing.bukkit.entity.BukkitEntityManager;
import net.momirealms.customfishing.bukkit.integration.action.CEActionExpansion;
import net.momirealms.customfishing.bukkit.integration.block.CraftEngineBlockProvider;
import net.momirealms.customfishing.bukkit.integration.block.ItemsAdderBlockProvider;
import net.momirealms.customfishing.bukkit.integration.block.NexoBlockProvider;
import net.momirealms.customfishing.bukkit.integration.block.OraxenBlockProvider;
import net.momirealms.customfishing.bukkit.integration.enchant.AdvancedEnchantmentsProvider;
import net.momirealms.customfishing.bukkit.integration.enchant.VanillaEnchantmentsProvider;
import net.momirealms.customfishing.bukkit.integration.entity.ItemsAdderEntityProvider;
import net.momirealms.customfishing.bukkit.integration.entity.MythicEntityProvider;
import net.momirealms.customfishing.bukkit.integration.item.*;
import net.momirealms.customfishing.bukkit.integration.level.*;
import net.momirealms.customfishing.bukkit.integration.papi.CompetitionPapi;
import net.momirealms.customfishing.bukkit.integration.papi.CustomFishingPapi;
import net.momirealms.customfishing.bukkit.integration.papi.StatisticsPapi;
import net.momirealms.customfishing.bukkit.integration.quest.BattlePassQuest;
import net.momirealms.customfishing.bukkit.integration.quest.BeautyFishingQuest;
import net.momirealms.customfishing.bukkit.integration.quest.ClueScrollsQuest;
import net.momirealms.customfishing.bukkit.integration.region.WorldGuardRegion;
import net.momirealms.customfishing.bukkit.integration.season.AdvancedSeasonsProvider;
import net.momirealms.customfishing.bukkit.integration.season.CustomCropsSeasonProvider;
import net.momirealms.customfishing.bukkit.integration.season.RealisticSeasonsProvider;
import net.momirealms.customfishing.bukkit.integration.shop.ShopGUIHook;
import net.momirealms.customfishing.bukkit.item.BukkitItemManager;
import net.momirealms.customfishing.bukkit.item.SNBTItemProvider;
import net.momirealms.customfishing.common.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BukkitIntegrationManager implements IntegrationManager {
    private static BukkitIntegrationManager instance;
    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<String, LevelerProvider> levelerProviders = new HashMap<>();
    private final HashMap<String, EnchantmentProvider> enchantmentProviders = new HashMap<>();
    private SeasonProvider seasonProvider;
    private boolean hasFloodGate;
    private boolean hasGeyser;

    public BukkitIntegrationManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        try {
            this.load();
        } catch (Throwable e) {
            plugin.getPluginLogger().warn("Failed to load integrations", e);
        } finally {
            instance = this;
        }
    }

    public static BukkitIntegrationManager instance() {
        return instance;
    }

    public boolean hasFloodGate() {
        return hasFloodGate;
    }

    public boolean hasGeyser() {
        return hasGeyser;
    }

    @Override
    public void disable() {
        this.enchantmentProviders.clear();
        this.levelerProviders.clear();
    }

    @Override
    public void delayedLoad() {
        if (isHooked("ClueScrolls")) {
            runCatchingHook(() -> {
                ClueScrollsQuest clueScrollsQuest = new ClueScrollsQuest();
                clueScrollsQuest.register();
            }, "ClueScrolls");
        }
    }

    @Override
    public void load() {
        registerEnchantmentProvider(new VanillaEnchantmentsProvider());
        registerItemProvider(new SNBTItemProvider());
        if (isHooked("ItemsAdder")) {
            runCatchingHook(() -> {
                registerItemProvider(new ItemsAdderItemProvider());
                registerBlockProvider(new ItemsAdderBlockProvider());
                registerEntityProvider(new ItemsAdderEntityProvider());
            }, "ItemsAdder");
        }
        if (isHooked("CraftEngine", "26.")) {
            runCatchingHook(() -> {
                registerItemProvider(new CraftEngineItemProvider());
                registerBlockProvider(new CraftEngineBlockProvider());
                CEActionExpansion.register();
            }, "CraftEngine");
        }
        if (isHooked("Nexo")) {
            runCatchingHook(() -> {
                registerItemProvider(new NexoItemProvider());
                registerBlockProvider(new NexoBlockProvider());
            }, "Nexo");
        }
        if (isHooked("MMOItems")) {
            runCatchingHook(() -> {
                registerItemProvider(new MMOItemsItemProvider());
            }, "MMOItems");
        }
        if (isHooked("EcoItems")) {
            runCatchingHook(() -> {
                registerItemProvider(new EcoItemsProvider());
            }, "EcoItems");
        }
        if (isHooked("Oraxen", "1")) {
            runCatchingHook(() -> {
                registerItemProvider(new OraxenItemProvider());
                registerBlockProvider(new OraxenBlockProvider());
            }, "Oraxen");
        }
        if (isHooked("Zaphkiel")) {
            runCatchingHook(() -> {
                registerItemProvider(new ZaphkielItemProvider());
            }, "Zaphkiel");
        }
        if (isHooked("NeigeItems")) {
            runCatchingHook(() -> {
                registerItemProvider(new NeigeItemsItemProvider());
            }, "NeigeItems");
        }
        if (isHooked("ExecutableItems")) {
            runCatchingHook(() -> {
                registerItemProvider(new ExecutableItemProvider());
            }, "ExecutableItems");
        }
        if (isHooked("MythicMobs", "5")) {
            runCatchingHook(() -> {
                registerItemProvider(new MythicMobsItemProvider());
                registerEntityProvider(new MythicEntityProvider());
            }, "MythicMobs");
        }
        if (isHooked("EcoJobs")) {
            runCatchingHook(() -> {
                registerLevelerProvider(new EcoJobsLevelerProvider());
            }, "EcoJobs");
        }
        if (isHooked("EcoSkills")) {
            runCatchingHook(() -> {
                registerLevelerProvider(new EcoSkillsLevelerProvider());
            }, "EcoSkills");
        }
        if (isHooked("Jobs")) {
            runCatchingHook(() -> {
                registerLevelerProvider(new JobsRebornLevelerProvider());
            }, "JobsReborn");
        }
        if (isHooked("MMOCore")) {
            runCatchingHook(() -> {
                registerLevelerProvider(new MMOCoreLevelerProvider());
            }, "MMOCore");
        }
        if (isHooked("mcMMO")) {
            runCatchingHook(() -> {
                registerLevelerProvider(new McMMOLevelerProvider());
                registerItemProvider(new McMMOTreasureProvider());
            }, "mcMMO");
        }
        if (isHooked("AureliumSkills")) {
            runCatchingHook(() -> {
                registerLevelerProvider(new AureliumSkillsProvider());
            }, "AureliumSkills");
        }
        if (isHooked("AuraSkills")) {
            runCatchingHook(() -> {
                registerLevelerProvider(new AuraSkillsLevelerProvider());
                registerItemProvider(new AuraSkillItemProvider());
            }, "AuraSkills");
        }
        if (isHooked("AdvancedEnchantments")) {
            runCatchingHook(() -> {
                registerEnchantmentProvider(new AdvancedEnchantmentsProvider());
            }, "AdvancedEnchantments");
        }
        if (isHooked("RealisticSeasons")) {
            runCatchingHook(() -> {
                registerSeasonProvider(new RealisticSeasonsProvider());
            }, "RealisticSeasons");
        } else if (isHooked("AdvancedSeasons")) {
            runCatchingHook(() -> {
                registerSeasonProvider(new AdvancedSeasonsProvider());
            }, "AdvancedSeasons");
        } else if (isHooked("CustomCrops")) {
            runCatchingHook(() -> {
                registerSeasonProvider(new CustomCropsSeasonProvider());
            }, "CustomCrops");
        }
        if (isHooked("Vault")) {
            VaultHook.init();
        }
        if (isHooked("BattlePass")){
            runCatchingHook(() -> {
                BattlePassQuest battlePassQuest = new BattlePassQuest();
                battlePassQuest.register();
            }, "BattlePass");
        }
        if (isHooked("WorldGuard", "7")) {
            runCatchingHook(WorldGuardRegion::register, "WorldGuard");
        }
        if (isHooked("PlaceholderAPI")) {
            new CustomFishingPapi(plugin).load();
            new CompetitionPapi(plugin).load();
            new StatisticsPapi(plugin).load();
        }
        if (isHooked("ShopGUIPlus")) {
            runCatchingHook(ShopGUIHook::register, "ShopGUIPlus");
        }
        if (isHooked("BeautyQuests", "2")) {
            runCatchingHook(BeautyFishingQuest::register, "BeautyQuests");
        } else if (isOutdated("BeautyQuests", "1")) {
            this.plugin.getPluginLogger().info("CustomFishing no longer supports BeautyQuests1.x, please consider updating to BeautyQuests2.x. https://www.spigotmc.org/resources/beautyquests.39255/");
        }
        if (Bukkit.getPluginManager().getPlugin("Geyser-Spigot") != null) {
            this.hasGeyser = true;
        }
        if (Bukkit.getPluginManager().getPlugin("floodgate") != null) {
            this.hasFloodGate = true;
        }
    }

    private boolean isHooked(String hooked) {
        if (Bukkit.getPluginManager().getPlugin(hooked) != null) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private boolean isHooked(String hooked, String... versionPrefix) {
        Plugin p = Bukkit.getPluginManager().getPlugin(hooked);
        if (p != null) {
            String ver = p.getDescription().getVersion();
            for (String prefix : versionPrefix) {
                if (ver.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isOutdated(String hooked, String... versionPrefix) {
        Plugin p = Bukkit.getPluginManager().getPlugin(hooked);
        if (p != null) {
            String ver = p.getDescription().getVersion();
            for (String prefix : versionPrefix) {
                if (ver.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean registerLevelerProvider(@NotNull LevelerProvider leveler) {
        if (levelerProviders.containsKey(leveler.identifier())) return false;
        levelerProviders.put(leveler.identifier(), leveler);
        return true;
    }

    @Override
    public boolean unregisterLevelerProvider(@NotNull String id) {
        return levelerProviders.remove(id) != null;
    }

    @Override
    public boolean registerEnchantmentProvider(@NotNull EnchantmentProvider enchantment) {
        if (enchantmentProviders.containsKey(enchantment.identifier())) return false;
        enchantmentProviders.put(enchantment.identifier(), enchantment);
        return true;
    }

    @Override
    public boolean unregisterEnchantmentProvider(@NotNull String id) {
        return enchantmentProviders.remove(id) != null;
    }

    @Override
    @Nullable
    public LevelerProvider getLevelerProvider(String plugin) {
        return levelerProviders.get(plugin);
    }

    @Override
    @Nullable
    public EnchantmentProvider getEnchantmentProvider(String id) {
        return enchantmentProviders.get(id);
    }

    @Override
    public List<Pair<String, Short>> getEnchantments(ItemStack itemStack) {
        ArrayList<Pair<String, Short>> list = new ArrayList<>();
        for (EnchantmentProvider enchantmentProvider : enchantmentProviders.values()) {
            list.addAll(enchantmentProvider.getEnchants(itemStack));
        }
        return list;
    }

    @Nullable
    @Override
    public SeasonProvider getSeasonProvider() {
        return seasonProvider;
    }

    @Override
    public boolean registerSeasonProvider(@NotNull SeasonProvider season) {
        if (this.seasonProvider != null) return false;
        this.seasonProvider = season;
        return true;
    }

    @Override
    public boolean unregisterSeasonProvider() {
        if (this.seasonProvider == null) return false;
        this.seasonProvider = null;
        return true;
    }

    @Override
    public boolean registerEntityProvider(@NotNull EntityProvider entity) {
        return ((BukkitEntityManager) plugin.getEntityManager()).registerEntityProvider(entity);
    }

    @Override
    public boolean unregisterEntityProvider(@NotNull String id) {
        return ((BukkitEntityManager) plugin.getEntityManager()).unregisterEntityProvider(id);
    }

    @Override
    public boolean registerItemProvider(@NotNull ItemProvider item) {
        return ((BukkitItemManager) plugin.getItemManager()).registerItemProvider(item);
    }

    @Override
    public boolean unregisterItemProvider(@NotNull String id) {
        return ((BukkitItemManager) plugin.getItemManager()).unregisterItemProvider(id);
    }

    @Override
    public boolean registerBlockProvider(@NotNull BlockProvider block) {
        return ((BukkitBlockManager) plugin.getBlockManager()).registerBlockProvider(block);
    }

    @Override
    public boolean unregisterBlockProvider(@NotNull String id) {
        return ((BukkitBlockManager) plugin.getBlockManager()).unregisterBlockProvider(id);
    }

    private void runCatchingHook(ThrowableRunnable runnable, String plugin) {
        try {
            runnable.run();
            this.plugin.getPluginLogger().info(plugin + " hooked!");
        } catch (Throwable e) {
            this.plugin.getPluginLogger().warn("Failed to hook " + plugin, e);
        }
    }

    private interface ThrowableRunnable {
        void run() throws Throwable;
    }

}
