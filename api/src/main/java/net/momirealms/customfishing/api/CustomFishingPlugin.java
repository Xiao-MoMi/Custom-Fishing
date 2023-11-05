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

import net.momirealms.customfishing.api.manager.*;
import net.momirealms.customfishing.api.scheduler.Scheduler;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class CustomFishingPlugin extends JavaPlugin {

    protected boolean initialized;
    protected Scheduler scheduler;
    protected CommandManager commandManager;
    protected VersionManager versionManager;
    protected ItemManager itemManager;
    protected RequirementManager requirementManager;
    protected ActionManager actionManager;
    protected LootManager lootManager;
    protected FishingManager fishingManager;
    protected EffectManager effectManager;
    protected EntityManager entityManager;
    protected BlockManager blockManager;
    protected AdventureManager adventure;
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

    private static CustomFishingPlugin instance;

    public CustomFishingPlugin() {
        instance = this;
    }

    public static CustomFishingPlugin get() {
        return getInstance();
    }

    @NotNull
    public static CustomFishingPlugin getInstance() {
        return instance;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public CommandManager getCommandManager() {
        return commandManager;
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
