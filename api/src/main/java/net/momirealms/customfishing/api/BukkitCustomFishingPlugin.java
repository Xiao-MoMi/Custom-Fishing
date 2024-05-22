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
import net.momirealms.customfishing.api.mechanic.action.ActionManager;
import net.momirealms.customfishing.api.mechanic.block.BlockManager;
import net.momirealms.customfishing.api.mechanic.competition.CompetitionManager;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.effect.EffectManager;
import net.momirealms.customfishing.api.mechanic.entity.EntityManager;
import net.momirealms.customfishing.api.mechanic.event.EventManager;
import net.momirealms.customfishing.api.mechanic.hook.HookManager;
import net.momirealms.customfishing.api.mechanic.item.ItemManager;
import net.momirealms.customfishing.api.mechanic.loot.LootManager;
import net.momirealms.customfishing.api.mechanic.market.MarketManager;
import net.momirealms.customfishing.api.mechanic.misc.cooldown.CoolDownManager;
import net.momirealms.customfishing.api.mechanic.misc.placeholder.PlaceholderManager;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementManager;
import net.momirealms.customfishing.api.mechanic.statistic.StatisticsManager;
import net.momirealms.customfishing.api.storage.StorageManager;
import net.momirealms.customfishing.common.plugin.CustomFishingPlugin;
import net.momirealms.customfishing.common.plugin.scheduler.AbstractJavaScheduler;
import net.momirealms.customfishing.common.sender.SenderFactory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;

import static java.util.Objects.requireNonNull;

public abstract class BukkitCustomFishingPlugin implements CustomFishingPlugin {

    private static BukkitCustomFishingPlugin instance;
    private final Plugin boostrap = requireNonNull(Bukkit.getPluginManager().getPlugin("CustomFishing"));

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

    public BukkitCustomFishingPlugin() {
        instance = this;
    }

    public static BukkitCustomFishingPlugin getInstance() {
        if (instance == null) {
            throw new IllegalArgumentException("Plugin not initialized");
        }
        return instance;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public RequirementManager<Player> getRequirementManager() {
        return requirementManager;
    }

    public ActionManager<Player> getActionManager() {
        return actionManager;
    }

    public SenderFactory<BukkitCustomFishingPlugin, CommandSender> getSenderFactory() {
        return senderFactory;
    }

    public File getDataFolder() {
        return boostrap.getDataFolder();
    }

    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }

    @Override
    public AbstractJavaScheduler<Location> getScheduler() {
        return scheduler;
    }

    public CompetitionManager getCompetitionManager() {
        return competitionManager;
    }

    public MarketManager getMarketManager() {
        return marketManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public LootManager getLootManager() {
        return lootManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public BlockManager getBlockManager() {
        return blockManager;
    }

    public CoolDownManager getCoolDownManager() {
        return coolDownManager;
    }

    public StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public Plugin getBoostrap() {
        return boostrap;
    }

    public void reload() {

    }
}
