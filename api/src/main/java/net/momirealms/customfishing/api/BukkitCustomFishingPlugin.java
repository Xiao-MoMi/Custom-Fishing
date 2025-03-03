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

package net.momirealms.customfishing.api;

import net.momirealms.customfishing.api.integration.IntegrationManager;
import net.momirealms.customfishing.api.mechanic.action.ActionManager;
import net.momirealms.customfishing.api.mechanic.bag.BagManager;
import net.momirealms.customfishing.api.mechanic.block.BlockManager;
import net.momirealms.customfishing.api.mechanic.competition.CompetitionManager;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.effect.EffectManager;
import net.momirealms.customfishing.api.mechanic.entity.EntityManager;
import net.momirealms.customfishing.api.mechanic.event.EventManager;
import net.momirealms.customfishing.api.mechanic.fishing.FishingManager;
import net.momirealms.customfishing.api.mechanic.game.AbstractGamingPlayer;
import net.momirealms.customfishing.api.mechanic.game.GameManager;
import net.momirealms.customfishing.api.mechanic.hook.HookManager;
import net.momirealms.customfishing.api.mechanic.item.ItemManager;
import net.momirealms.customfishing.api.mechanic.loot.LootManager;
import net.momirealms.customfishing.api.mechanic.market.MarketManager;
import net.momirealms.customfishing.api.mechanic.misc.cooldown.CoolDownManager;
import net.momirealms.customfishing.api.mechanic.misc.hologram.HologramManager;
import net.momirealms.customfishing.api.mechanic.misc.placeholder.PlaceholderManager;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementManager;
import net.momirealms.customfishing.api.mechanic.statistic.StatisticsManager;
import net.momirealms.customfishing.api.mechanic.totem.TotemManager;
import net.momirealms.customfishing.api.storage.StorageManager;
import net.momirealms.customfishing.common.dependency.DependencyManager;
import net.momirealms.customfishing.common.locale.TranslationManager;
import net.momirealms.customfishing.common.plugin.CustomFishingPlugin;
import net.momirealms.customfishing.common.plugin.scheduler.AbstractJavaScheduler;
import net.momirealms.customfishing.common.sender.SenderFactory;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.function.Supplier;

/**
 * Abstract class representing the main CustomFishing plugin.
 * This class provides access to various managers and functionalities within the plugin.
 */
public abstract class BukkitCustomFishingPlugin implements CustomFishingPlugin {

    private static BukkitCustomFishingPlugin instance;
    private final Plugin bootstrap;

    protected EventManager eventManager;
    protected ConfigManager configManager;
    protected RequirementManager<Player> requirementManager;
    protected ActionManager<Player> actionManager;
    protected SenderFactory<BukkitCustomFishingPlugin, CommandSender> senderFactory;
    protected PlaceholderManager placeholderManager;
    protected AbstractJavaScheduler<Location> scheduler;
    protected ItemManager itemManager;
    protected IntegrationManager integrationManager;
    protected CompetitionManager competitionManager;
    protected MarketManager marketManager;
    protected StorageManager storageManager;
    protected LootManager lootManager;
    protected CoolDownManager coolDownManager;
    protected EntityManager entityManager;
    protected BlockManager blockManager;
    protected StatisticsManager statisticsManager;
    protected EffectManager effectManager;
    protected HookManager hookManager;
    protected BagManager bagManager;
    protected DependencyManager dependencyManager;
    protected TranslationManager translationManager;
    protected TotemManager totemManager;
    protected FishingManager fishingManager;
    protected GameManager gameManager;
    protected HologramManager hologramManager;

    /**
     * Constructs a new BukkitCustomFishingPlugin instance.
     *
     * @param bootstrap the plugin instance used to initialize this class
     */
    public BukkitCustomFishingPlugin(Plugin bootstrap) {
        if (!bootstrap.getName().equals("CustomFishing")) {
            throw new IllegalArgumentException("CustomFishing plugin requires custom fishing plugin");
        }
        this.bootstrap = bootstrap;
        instance = this;
    }

    /**
     * Retrieves the singleton instance of BukkitCustomFishingPlugin.
     *
     * @return the singleton instance
     * @throws IllegalArgumentException if the plugin is not initialized
     */
    public static BukkitCustomFishingPlugin getInstance() {
        if (instance == null) {
            throw new IllegalArgumentException("Plugin not initialized");
        }
        return instance;
    }

    /**
     * Retrieves the EventManager.
     *
     * @return the {@link EventManager}
     */
    public EventManager getEventManager() {
        return eventManager;
    }

    /**
     * Retrieves the ConfigManager.
     *
     * @return the {@link ConfigManager}
     */
    @Override
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Retrieves the RequirementManager.
     *
     * @return the {@link RequirementManager} for {@link Player}
     */
    public RequirementManager<Player> getRequirementManager() {
        return requirementManager;
    }

    /**
     * Retrieves the ActionManager.
     *
     * @return the {@link ActionManager} for {@link Player}
     */
    public ActionManager<Player> getActionManager() {
        return actionManager;
    }

    /**
     * Retrieves the SenderFactory.
     *
     * @return the {@link SenderFactory} for {@link BukkitCustomFishingPlugin} and {@link CommandSender}
     */
    public SenderFactory<BukkitCustomFishingPlugin, CommandSender> getSenderFactory() {
        return senderFactory;
    }

    /**
     * Retrieves the data folder of the plugin.
     *
     * @return the data folder as a {@link File}
     */
    public File getDataFolder() {
        return bootstrap.getDataFolder();
    }

    /**
     * Retrieves the PlaceholderManager.
     *
     * @return the {@link PlaceholderManager}
     */
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    /**
     * Retrieves the ItemManager.
     *
     * @return the {@link ItemManager}
     */
    public ItemManager getItemManager() {
        return itemManager;
    }

    /**
     * Retrieves the IntegrationManager.
     *
     * @return the {@link IntegrationManager}
     */
    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }

    /**
     * Retrieves the Scheduler.
     *
     * @return the {@link AbstractJavaScheduler} for {@link Location}
     */
    @Override
    public AbstractJavaScheduler<Location> getScheduler() {
        return scheduler;
    }

    /**
     * Retrieves the CompetitionManager.
     *
     * @return the {@link CompetitionManager}
     */
    public CompetitionManager getCompetitionManager() {
        return competitionManager;
    }

    /**
     * Retrieves the MarketManager.
     *
     * @return the {@link MarketManager}
     */
    public MarketManager getMarketManager() {
        return marketManager;
    }

    /**
     * Retrieves the StorageManager.
     *
     * @return the {@link StorageManager}
     */
    public StorageManager getStorageManager() {
        return storageManager;
    }

    /**
     * Retrieves the LootManager.
     *
     * @return the {@link LootManager}
     */
    public LootManager getLootManager() {
        return lootManager;
    }

    /**
     * Retrieves the EntityManager.
     *
     * @return the {@link EntityManager}
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Retrieves the HookManager.
     *
     * @return the {@link HookManager}
     */
    public HookManager getHookManager() {
        return hookManager;
    }

    /**
     * Retrieves the BlockManager.
     *
     * @return the {@link BlockManager}
     */
    public BlockManager getBlockManager() {
        return blockManager;
    }

    /**
     * Retrieves the CoolDownManager.
     *
     * @return the {@link CoolDownManager}
     */
    public CoolDownManager getCoolDownManager() {
        return coolDownManager;
    }

    /**
     * Retrieves the StatisticsManager.
     *
     * @return the {@link StatisticsManager}
     */
    public StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }

    /**
     * Retrieves the EffectManager.
     *
     * @return the {@link EffectManager}
     */
    public EffectManager getEffectManager() {
        return effectManager;
    }

    /**
     * Retrieves the BagManager.
     *
     * @return the {@link BagManager}
     */
    public BagManager getBagManager() {
        return bagManager;
    }

    /**
     * Retrieves the TotemManager.
     *
     * @return the {@link TotemManager}
     */
    public TotemManager getTotemManager() {
        return totemManager;
    }

    /**
     * Retrieves the FishingManager.
     *
     * @return the {@link FishingManager}
     */
    public FishingManager getFishingManager() {
        return fishingManager;
    }

    /**
     * Retrieves the GameManager.
     *
     * @return the {@link GameManager}
     */
    public GameManager getGameManager() {
        return gameManager;
    }

    /**
     * Retrieves the plugin instance used to initialize this class.
     *
     * @return the {@link Plugin} instance
     */
    public Plugin getBootstrap() {
        return bootstrap;
    }

    /**
     * Retrieves the DependencyManager.
     *
     * @return the {@link DependencyManager}
     */
    @Override
    public DependencyManager getDependencyManager() {
        return dependencyManager;
    }

    /**
     * Retrieves the TranslationManager.
     *
     * @return the {@link TranslationManager}
     */
    @Override
    public TranslationManager getTranslationManager() {
        return translationManager;
    }

    /**
     * Retrieves the HologramManager.
     *
     * @return the {@link HologramManager}
     */
    public HologramManager getHologramManager() {
        return hologramManager;
    }

    /**
     * Logs a debug message.
     *
     * @param message the message to log
     */
    public abstract void debug(Object message);

    public abstract void debug(Supplier<String> messageSupplier);
}
