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
import net.momirealms.customfishing.util.AdventureUtil;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

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
        this.integrationManager = new IntegrationManager(this);
        this.competitionManager = new CompetitionManager(this);
        this.effectManager = new EffectManager(this);
        this.lootManager = new LootManager(this);
        this.barMechanicManager = new BarMechanicManager(this);
        this.totemManager = new TotemManager(this);
        this.sellManager = new SellManager(this);
        this.bagDataManager = new BagDataManager(this);
        this.offsetManager = new OffsetManager(this);
        this.statisticsManager = new StatisticsManager(this);
        this.reload();
        this.registerCommands();
        this.registerQuests();
        AdventureUtil.consoleMessage("[CustomFishing] Plugin Enabled!");
        if (ConfigManager.bStats) new Metrics(this, 16648);
    }

    @Override
    public void onDisable() {
        this.fishingManager.unload();
        this.integrationManager.unload();
        this.competitionManager.unload();
        this.effectManager.unload();
        this.lootManager.unload();
        this.barMechanicManager.unload();
        this.bagDataManager.disable();
        this.totemManager.unload();
        this.sellManager.disable();
        this.dataManager.disable();
        this.statisticsManager.disable();
        if (adventure != null) {
            adventure.close();
        }
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
        LibraryLoader.load("redis.clients","jedis","4.3.1","https://repo.maven.apache.org/maven2/");
        LibraryLoader.load("org.apache.commons","commons-pool2","2.11.1","https://repo.maven.apache.org/maven2/");
        LibraryLoader.load("dev.dejvokep","boosted-yaml","1.3","https://repo.maven.apache.org/maven2/");
        LibraryLoader.load("com.zaxxer","HikariCP","5.0.1","https://repo.maven.apache.org/maven2/");
        LibraryLoader.load("net.objecthunter","exp4j","0.4.8","https://repo.maven.apache.org/maven2/");
        LibraryLoader.load("org.mariadb.jdbc","mariadb-java-client","3.0.6","https://repo.maven.apache.org/maven2/");
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

    public void reload() {
        ConfigManager.load();
        MessageManager.load();
        getDataManager().unload();
        getDataManager().load();
        getIntegrationManager().unload();
        getIntegrationManager().load();
        getBarMechanicManager().unload();
        getBarMechanicManager().load();
        getEffectManager().unload();
        getEffectManager().load();
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
