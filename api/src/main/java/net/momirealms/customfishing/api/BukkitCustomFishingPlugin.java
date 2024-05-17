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

package net.momirealms.customfishing.api;

import net.momirealms.customfishing.api.integration.IntegrationManager;
import net.momirealms.customfishing.api.manager.*;
import net.momirealms.customfishing.api.mechanic.action.ActionManager;
import net.momirealms.customfishing.api.mechanic.bag.BagManager;
import net.momirealms.customfishing.api.mechanic.block.BlockManager;
import net.momirealms.customfishing.api.mechanic.competition.CompetitionManager;
import net.momirealms.customfishing.api.mechanic.effect.EffectManager;
import net.momirealms.customfishing.api.mechanic.entity.EntityManager;
import net.momirealms.customfishing.api.mechanic.fishing.FishingManager;
import net.momirealms.customfishing.api.mechanic.game.GameManager;
import net.momirealms.customfishing.api.mechanic.hook.HookManager;
import net.momirealms.customfishing.api.mechanic.item.ItemManager;
import net.momirealms.customfishing.api.mechanic.loot.LootManager;
import net.momirealms.customfishing.api.mechanic.market.MarketManager;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementManager;
import net.momirealms.customfishing.api.mechanic.statistic.StatisticsManager;
import net.momirealms.customfishing.api.mechanic.totem.TotemManager;
import net.momirealms.customfishing.common.command.CustomFishingCommandManager;
import net.momirealms.customfishing.common.plugin.CustomFishingPlugin;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerAdapter;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class BukkitCustomFishingPlugin extends JavaPlugin implements CustomFishingPlugin {

    protected boolean initialized;
    protected SchedulerAdapter<Location> scheduler;
    protected CustomFishingCommandManager<CommandSender> commandManager;
    protected VersionManager versionManager;
    protected ItemManager itemManager;
    protected RequirementManager requirementManager;
    protected ActionManager actionManager;
    protected LootManager lootManager;
    protected FishingManager fishingManager;
    protected EffectManager effectManager;
    protected EntityManager entityManager;
    protected BlockManager blockManager;
    protected BagManager bagManager;
    protected GameManager gameManager;
    protected MarketManager marketManager;
    protected IntegrationManager integrationManager;
    protected CompetitionManager competitionManager;
    protected StorageManager storageManager;
    protected PlaceholderManager placeholderManager;
    protected StatisticsManager statisticsManager;
    protected TotemManager totemManager;
    protected HookManager hookManager;

    private static BukkitCustomFishingPlugin instance;

    public BukkitCustomFishingPlugin() {
        instance = this;
    }

    public static BukkitCustomFishingPlugin get() {
        return getInstance();
    }

    public static BukkitCustomFishingPlugin getInstance() {
        return instance;
    }

    public VersionManager getVersionManager() {
        return versionManager;
    }

    public RequirementManager getRequirementManager() {
        return requirementManager;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public BlockManager getBlockManager() {
        return blockManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public MarketManager getMarketManager() {
        return marketManager;
    }

    public FishingManager getFishingManager() {
        return fishingManager;
    }

    public AdventureManager getAdventure() {
        return adventure;
    }

    public BagManager getBagManager() {
        return bagManager;
    }

    public LootManager getLootManager() {
        return lootManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public TotemManager getTotemManager() {
        return totemManager;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }

    public StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }

    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    public CompetitionManager getCompetitionManager() {
        return competitionManager;
    }

    public abstract void reload();

    public abstract YamlConfiguration getConfig(String file);

    public abstract boolean isHookedPluginEnabled(String plugin);

    public abstract void debug(String message);
}
