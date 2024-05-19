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

package net.momirealms.customfishing.bukkit;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.event.CustomFishingReloadEvent;
import net.momirealms.customfishing.api.mechanic.misc.cooldown.CoolDownManager;
import net.momirealms.customfishing.api.mechanic.misc.placeholder.BukkitPlaceholderManager;
import net.momirealms.customfishing.bukkit.bag.BukkitBagManager;
import net.momirealms.customfishing.bukkit.block.BukkitBlockManager;
import net.momirealms.customfishing.bukkit.compatibility.BukkitIntegrationManager;
import net.momirealms.customfishing.bukkit.competition.BukkitCompetitionManager;
import net.momirealms.customfishing.bukkit.effect.BukkitEffectManager;
import net.momirealms.customfishing.bukkit.entity.BukkitEntityManager;
import net.momirealms.customfishing.bukkit.fishing.BukkitFishingManager;
import net.momirealms.customfishing.bukkit.game.BukkitGameManager;
import net.momirealms.customfishing.bukkit.hook.BukkitHookManager;
import net.momirealms.customfishing.bukkit.item.ItemManagerImpl;
import net.momirealms.customfishing.bukkit.loot.LootManagerImpl;
import net.momirealms.customfishing.bukkit.market.BukkitMarketManager;
import net.momirealms.customfishing.bukkit.misc.ChatCatcherManager;
import net.momirealms.customfishing.bukkit.statistic.BukkitStatisticsManager;
import net.momirealms.customfishing.bukkit.storage.BukkitStorageManager;
import net.momirealms.customfishing.bukkit.totem.BukkitTotemManager;
import net.momirealms.customfishing.common.helper.VersionHelper;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BukkitCustomFishingPluginImpl extends BukkitCustomFishingPlugin {

    private static ProtocolManager protocolManager;
    private CoolDownManager coolDownManager;
    private ChatCatcherManager chatCatcherManager;
    private DependencyManager dependencyManager;

    public BukkitCustomFishingPluginImpl() {
        super();
    }

    @Override
    public void onLoad() {
        this.versionManager = new VersionHelper(this);
        this.dependencyManager = new DependencyManagerImpl(this, new ReflectionClassPathAppender(this.getClassLoader()));
        this.dependencyManager.loadDependencies(new ArrayList<>(
                List.of(
                        Dependency.GSON,
                        Dependency.SLF4J_API,
                        Dependency.SLF4J_SIMPLE,
                        Dependency.BOOSTED_YAML,
                        Dependency.EXP4J,
                        Dependency.MYSQL_DRIVER,
                        Dependency.MARIADB_DRIVER,
                        Dependency.MONGODB_DRIVER_SYNC,
                        Dependency.MONGODB_DRIVER_CORE,
                        Dependency.MONGODB_DRIVER_BSON,
                        Dependency.JEDIS,
                        Dependency.COMMONS_POOL_2,
                        Dependency.COMMONS_LANG_3,
                        Dependency.H2_DRIVER,
                        Dependency.SQLITE_DRIVER,
                        Dependency.BSTATS_BASE,
                        Dependency.HIKARI,
                        Dependency.BSTATS_BUKKIT,
                        versionManager.isMojmap() ? Dependency.COMMAND_API_MOJMAP : Dependency.COMMAND_API
                )
        ));
    }

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();

        NBTUtils.disableNBTAPILogs();
        ReflectionUtils.load();

        this.actionManager = new ActionManagerImpl(this);
        this.adventure = new AdventureHelper(this);
        this.bagManager = new BukkitBagManager(this);
        this.blockManager = new BukkitBlockManager(this);
        this.commandManager = new CommandManagerImpl(this);
        this.effectManager = new BukkitEffectManager(this);
        this.fishingManager = new BukkitFishingManager(this);
        this.gameManager = new BukkitGameManager(this);
        this.itemManager = new ItemManagerImpl(this);
        this.lootManager = new LootManagerImpl(this);
        this.marketManager = new BukkitMarketManager(this);
        this.entityManager = new BukkitEntityManager(this);
        this.placeholderManager = new BukkitPlaceholderManager(this);
        this.requirementManager = new RequirementManagerImpl(this);
        this.scheduler = new SchedulerImpl(this);
        this.storageManager = new BukkitStorageManager(this);
        this.competitionManager = new BukkitCompetitionManager(this);
        this.integrationManager = new BukkitIntegrationManager(this);
        this.statisticsManager = new BukkitStatisticsManager(this);
        this.coolDownManager = new CoolDownManager(this);
        this.totemManager = new BukkitTotemManager(this);
        this.hookManager = new BukkitHookManager(this);
        this.chatCatcherManager = new ChatCatcherManager(this);
        this.reload();
        super.initialized = true;

        if (CFConfig.metrics) new Metrics(this, 16648);
        if (CFConfig.updateChecker)
            this.versionManager.checkUpdate().thenAccept(result -> {
                if (!result) this.getAdventure().sendConsoleMessage("[CustomFishing] You are using the latest version.");
                else this.getAdventure().sendConsoleMessage("[CustomFishing] Update is available: <u>https://polymart.org/resource/2723<!u>");
            });
    }

    @Override
    public void onDisable() {
        if (this.adventure != null) ((AdventureHelper) this.adventure).close();
        if (this.bagManager != null) ((BukkitBagManager) this.bagManager).disable();
        if (this.blockManager != null) ((BukkitBlockManager) this.blockManager).disable();
        if (this.effectManager != null) ((BukkitEffectManager) this.effectManager).disable();
        if (this.fishingManager != null) ((BukkitFishingManager) this.fishingManager).disable();
        if (this.gameManager != null) ((BukkitGameManager) this.gameManager).disable();
        if (this.itemManager != null) ((ItemManagerImpl) this.itemManager).disable();
        if (this.lootManager != null) ((LootManagerImpl) this.lootManager).disable();
        if (this.marketManager != null) ((BukkitMarketManager) this.marketManager).disable();
        if (this.entityManager != null) ((BukkitEntityManager) this.entityManager).disable();
        if (this.requirementManager != null) ((RequirementManagerImpl) this.requirementManager).disable();
        if (this.scheduler != null) ((SchedulerImpl) this.scheduler).shutdown();
        if (this.integrationManager != null) ((BukkitIntegrationManager) this.integrationManager).disable();
        if (this.competitionManager != null) ((BukkitCompetitionManager) this.competitionManager).disable();
        if (this.storageManager != null) ((BukkitStorageManager) this.storageManager).disable();
        if (this.placeholderManager != null) ((BukkitPlaceholderManager) this.placeholderManager).disable();
        if (this.statisticsManager != null) ((BukkitStatisticsManager) this.statisticsManager).disable();
        if (this.actionManager != null) ((ActionManagerImpl) this.actionManager).disable();
        if (this.totemManager != null) ((BukkitTotemManager) this.totemManager).disable();
        if (this.hookManager != null) ((BukkitHookManager) this.hookManager).disable();
        if (this.coolDownManager != null) this.coolDownManager.disable();
        if (this.chatCatcherManager != null) this.chatCatcherManager.disable();
        if (this.commandManager != null) this.commandManager.unload();
        HandlerList.unregisterAll(this);
    }

    /**
     * Reload the plugin
     */
    @Override
    public void reload() {
        CFConfig.load();
        CFLocale.load();
        ((SchedulerImpl) this.scheduler).reload();
        ((RequirementManagerImpl) this.requirementManager).unload();
        ((RequirementManagerImpl) this.requirementManager).load();
        ((ActionManagerImpl) this.actionManager).unload();
        ((ActionManagerImpl) this.actionManager).load();
        ((BukkitGameManager) this.gameManager).unload();
        ((BukkitGameManager) this.gameManager).load();
        ((ItemManagerImpl) this.itemManager).unload();
        ((ItemManagerImpl) this.itemManager).load();
        ((LootManagerImpl) this.lootManager).unload();
        ((LootManagerImpl) this.lootManager).load();
        ((BukkitFishingManager) this.fishingManager).unload();
        ((BukkitFishingManager) this.fishingManager).load();
        ((BukkitTotemManager) this.totemManager).unload();
        ((BukkitTotemManager) this.totemManager).load();
        ((BukkitEffectManager) this.effectManager).unload();
        ((BukkitEffectManager) this.effectManager).load();
        ((BukkitMarketManager) this.marketManager).unload();
        ((BukkitMarketManager) this.marketManager).load();
        ((BukkitBagManager) this.bagManager).unload();
        ((BukkitBagManager) this.bagManager).load();
        ((BukkitBlockManager) this.blockManager).unload();
        ((BukkitBlockManager) this.blockManager).load();
        ((BukkitEntityManager) this.entityManager).unload();
        ((BukkitEntityManager) this.entityManager).load();
        ((BukkitCompetitionManager) this.competitionManager).unload();
        ((BukkitCompetitionManager) this.competitionManager).load();
        ((BukkitStorageManager) this.storageManager).reload();
        ((BukkitStatisticsManager) this.statisticsManager).unload();
        ((BukkitStatisticsManager) this.statisticsManager).load();
        ((BukkitPlaceholderManager) this.placeholderManager).unload();
        ((BukkitPlaceholderManager) this.placeholderManager).load();
        ((BukkitHookManager) this.hookManager).unload();
        ((BukkitHookManager) this.hookManager).load();
        this.commandManager.unload();
        this.commandManager.load();
        this.coolDownManager.unload();
        this.coolDownManager.load();
        this.chatCatcherManager.unload();
        this.chatCatcherManager.load();

        CustomFishingReloadEvent event = new CustomFishingReloadEvent(this);
        Bukkit.getPluginManager().callEvent(event);
    }

    /**
     * Retrieves a YAML configuration from a file within the plugin's data folder.
     *
     * @param file The name of the configuration file.
     * @return A YamlConfiguration object representing the configuration.
     */
    @Override
    public YamlConfiguration getConfig(String file) {
        File config = new File(this.getDataFolder(), file);
        if (!config.exists()) this.saveResource(file, false);
        return YamlConfiguration.loadConfiguration(config);
    }

    /**
     * Checks if a specified plugin is enabled on the Bukkit server.
     *
     * @param plugin The name of the plugin to check.
     * @return True if the plugin is enabled, false otherwise.
     */
    @Override
    public boolean isHookedPluginEnabled(String plugin) {
        return Bukkit.getPluginManager().isPluginEnabled(plugin);
    }

    /**
     * Outputs a debugging message if the debug mode is enabled.
     *
     * @param message The debugging message to be logged.
     */
    @Override
    public void debug(String message) {
        if (!CFConfig.debug) return;
        LogUtils.info(message);
    }

    /**
     * Gets the CoolDownManager instance associated with the plugin.
     *
     * @return The CoolDownManager instance.
     */
    public CoolDownManager getCoolDownManager() {
        return coolDownManager;
    }

    /**
     * Gets the ChatCatcherManager instance associated with the plugin.
     *
     * @return The ChatCatcherManager instance.
     */
    public ChatCatcherManager getChatCatcherManager() {
        return chatCatcherManager;
    }

    public DependencyManager getDependencyManager() {
        return dependencyManager;
    }

    /**
     * Retrieves the ProtocolManager instance used for managing packets.
     *
     * @return The ProtocolManager instance.
     */
    @NotNull
    public static ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public static void sendPacket(Player player, PacketContainer packet) {
        protocolManager.sendServerPacket(player, packet);
    }

    public static void sendPackets(Player player, PacketContainer... packets) {
        List<PacketContainer> bundle = new ArrayList<>(Arrays.asList(packets));
        PacketContainer bundlePacket = new PacketContainer(PacketType.Play.Server.BUNDLE);
        bundlePacket.getPacketBundles().write(0, bundle);
        sendPacket(player, bundlePacket);
    }
}
