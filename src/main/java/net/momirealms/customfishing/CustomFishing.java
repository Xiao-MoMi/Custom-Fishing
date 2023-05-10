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

package net.momirealms.customfishing;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.momirealms.customfishing.commands.FishingBagCommand;
import net.momirealms.customfishing.commands.MainCommand;
import net.momirealms.customfishing.commands.SellFishCommand;
import net.momirealms.customfishing.helper.LibraryLoader;
import net.momirealms.customfishing.helper.VersionHelper;
import net.momirealms.customfishing.manager.*;
import net.momirealms.customfishing.object.Reflection;
import net.momirealms.customfishing.scheduler.Scheduler;
import net.momirealms.customfishing.util.AdventureUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.TimeZone;

public final class CustomFishing extends JavaPlugin {

    private static CustomFishing plugin;
    private static BukkitAudiences adventure;
    private static ProtocolManager protocolManager;
    private IntegrationManager integrationManager;
    private FishingManager fishingManager;
    private CompetitionManager competitionManager;
    private EffectManager effectManager;
    private LootManager lootManager;
    private BarMechanicManager barMechanicManager;
    private BagDataManager bagDataManager;
    private TotemManager totemManager;
    private DataManager dataManager;
    private SellManager sellManager;
    private OffsetManager offsetManager;
    private StatisticsManager statisticsManager;
    private VersionHelper versionHelper;
    private Scheduler scheduler;

    @Override
    public void onLoad() {
        plugin = this;
        loadLibs();
    }

    @Override
    public void onEnable() {
        adventure = BukkitAudiences.create(this);
        protocolManager = ProtocolLibrary.getProtocolManager();
        this.versionHelper = new VersionHelper(this);
        this.fishingManager = new FishingManager(this);
        this.dataManager = new DataManager(this);
        this.statisticsManager = new StatisticsManager(this);
        this.integrationManager = new IntegrationManager(this);
        this.competitionManager = new CompetitionManager(this);
        this.effectManager = new EffectManager(this);
        this.lootManager = new LootManager(this);
        this.barMechanicManager = new BarMechanicManager(this);
        this.totemManager = new TotemManager(this);
        this.sellManager = new SellManager(this);
        this.bagDataManager = new BagDataManager(this);
        this.offsetManager = new OffsetManager(this);
        this.scheduler = new Scheduler(this);
        this.reload();
        this.registerCommands();
        this.registerQuests();
        Reflection.load();
        AdventureUtils.consoleMessage("[CustomFishing] Plugin Enabled!");
        if (ConfigManager.bStats) new Metrics(this, 16648);
        if (ConfigManager.updateChecker) this.versionHelper.checkUpdate();
    }

    @Override
    public void onDisable() {
        if (this.fishingManager != null) this.fishingManager.unload();
        if (this.integrationManager != null) this.integrationManager.unload();
        if (this.competitionManager != null) this.competitionManager.unload();
        if (this.effectManager != null) this.effectManager.unload();
        if (this.lootManager != null) this.lootManager.unload();
        if (this.barMechanicManager != null) this.barMechanicManager.unload();
        if (this.totemManager != null) this.totemManager.unload();
        if (this.bagDataManager != null) this.bagDataManager.disable();
        if (this.sellManager != null) this.sellManager.disable();
        if (this.statisticsManager != null) this.statisticsManager.disable();
        if (this.dataManager != null) this.dataManager.disable();
        if (this.scheduler != null) scheduler.disable();
        if (adventure != null) adventure.close();
    }

    private void registerCommands() {
        MainCommand mainCommand = new MainCommand();
        PluginCommand cfCommand = Bukkit.getPluginCommand("customfishing");
        if (cfCommand != null) {
            cfCommand.setExecutor(mainCommand);
            cfCommand.setTabCompleter(mainCommand);
        }
        FishingBagCommand fishingBagCommand = new FishingBagCommand();
        PluginCommand fbCommand = Bukkit.getPluginCommand("fishingbag");
        if (fbCommand != null) {
            fbCommand.setExecutor(fishingBagCommand);
            fbCommand.setTabCompleter(fishingBagCommand);
        }
        SellFishCommand sellFishCommand = new SellFishCommand();
        PluginCommand sfCommand = Bukkit.getPluginCommand("sellfish");
        if (sfCommand != null) {
            sfCommand.setExecutor(sellFishCommand);
            sfCommand.setTabCompleter(sellFishCommand);
        }
    }

    private void loadLibs() {
        TimeZone timeZone = TimeZone.getDefault();
        String libRepo = timeZone.getID().startsWith("Asia") ? "https://maven.aliyun.com/repository/public/" : "https://repo.maven.apache.org/maven2/";
        LibraryLoader.load("org.apache.commons","commons-pool2","2.11.1", libRepo);
        LibraryLoader.load("redis.clients","jedis","4.3.2", libRepo);
        LibraryLoader.load("dev.dejvokep","boosted-yaml","1.3", libRepo);
        LibraryLoader.load("com.zaxxer","HikariCP","5.0.1", libRepo);
        LibraryLoader.load("net.objecthunter","exp4j","0.4.8", libRepo);
        LibraryLoader.load("org.mariadb.jdbc","mariadb-java-client","3.1.4", libRepo);
        LibraryLoader.load("mysql","mysql-connector-java","8.0.30", libRepo);
    }

    private void registerQuests() {
        this.integrationManager.registerQuests();
    }

    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }

    public FishingManager getFishingManager() {
        return fishingManager;
    }

    public CompetitionManager getCompetitionManager() {
        return competitionManager;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public LootManager getLootManager() {
        return lootManager;
    }

    public TotemManager getTotemManager() {
        return totemManager;
    }

    public SellManager getSellManager() {
        return sellManager;
    }

    public BagDataManager getBagDataManager() {
        return bagDataManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public VersionHelper getVersionHelper() {
        return versionHelper;
    }

    public BarMechanicManager getBarMechanicManager() {
        return barMechanicManager;
    }

    public StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }

    public OffsetManager getOffsetManager() {
        return offsetManager;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void reload() {
        ConfigManager.load();
        MessageManager.load();
        getDataManager().unload();
        getDataManager().load();
        getEffectManager().unload();
        getEffectManager().load();
        getIntegrationManager().unload();
        getIntegrationManager().load();
        getBarMechanicManager().unload();
        getBarMechanicManager().load();
        getOffsetManager().unload();
        getOffsetManager().load();
        getLootManager().unload();
        getLootManager().load();
        getTotemManager().unload();
        getTotemManager().load();
        getFishingManager().unload();
        getFishingManager().load();
        getSellManager().unload();
        getSellManager().load();
        getCompetitionManager().unload();
        getCompetitionManager().load();
        getBagDataManager().unload();
        getBagDataManager().load();
        getStatisticsManager().unload();
        getStatisticsManager().load();
        getScheduler().reload();
    }

    public static BukkitAudiences getAdventure() {
        return adventure;
    }

    public static ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public static CustomFishing getInstance() {
        return plugin;
    }
}
