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
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import de.tr7zw.changeme.nbtapi.utils.VersionChecker;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.api.util.ReflectionUtils;
import net.momirealms.customfishing.command.CommandManagerImpl;
import net.momirealms.customfishing.compatibility.IntegrationManagerImpl;
import net.momirealms.customfishing.compatibility.papi.PlaceholderManagerImpl;
import net.momirealms.customfishing.libraries.libraryloader.LibraryLoader;
import net.momirealms.customfishing.mechanic.action.ActionManagerImpl;
import net.momirealms.customfishing.mechanic.bag.BagManagerImpl;
import net.momirealms.customfishing.mechanic.block.BlockManagerImpl;
import net.momirealms.customfishing.mechanic.competition.CompetitionManagerImpl;
import net.momirealms.customfishing.mechanic.effect.EffectManagerImpl;
import net.momirealms.customfishing.mechanic.entity.EntityManagerImpl;
import net.momirealms.customfishing.mechanic.fishing.FishingManagerImpl;
import net.momirealms.customfishing.mechanic.game.GameManagerImpl;
import net.momirealms.customfishing.mechanic.item.ItemManagerImpl;
import net.momirealms.customfishing.mechanic.loot.LootManagerImpl;
import net.momirealms.customfishing.mechanic.market.MarketManagerImpl;
import net.momirealms.customfishing.mechanic.misc.CoolDownManager;
import net.momirealms.customfishing.mechanic.requirement.RequirementManagerImpl;
import net.momirealms.customfishing.mechanic.statistic.StatisticsManagerImpl;
import net.momirealms.customfishing.mechanic.totem.TotemManagerImpl;
import net.momirealms.customfishing.scheduler.SchedulerImpl;
import net.momirealms.customfishing.setting.CFConfig;
import net.momirealms.customfishing.setting.CFLocale;
import net.momirealms.customfishing.storage.StorageManagerImpl;
import net.momirealms.customfishing.version.VersionManagerImpl;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.util.TimeZone;

public class CustomFishingPluginImpl extends CustomFishingPlugin {

    private static ProtocolManager protocolManager;
    private CoolDownManager coolDownManager;

    public CustomFishingPluginImpl() {
        super();
    }

    @Override
    public void onLoad() {
        this.loadDependencies();
    }

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        this.versionManager = new VersionManagerImpl(this);
        this.disableNBTAPILogs();
        ReflectionUtils.load();

        this.actionManager = new ActionManagerImpl(this);
        this.adventure = new AdventureManagerImpl(this);
        this.bagManager = new BagManagerImpl(this);
        this.blockManager = new BlockManagerImpl(this);
        this.commandManager = new CommandManagerImpl(this);
        this.effectManager = new EffectManagerImpl(this);
        this.fishingManager = new FishingManagerImpl(this);
        this.gameManager = new GameManagerImpl(this);
        this.itemManager = new ItemManagerImpl(this);
        this.lootManager = new LootManagerImpl(this);
        this.marketManager = new MarketManagerImpl(this);
        this.entityManager = new EntityManagerImpl(this);
        this.placeholderManager = new PlaceholderManagerImpl(this);
        this.requirementManager = new RequirementManagerImpl(this);
        this.scheduler = new SchedulerImpl(this);
        this.storageManager = new StorageManagerImpl(this);
        this.competitionManager = new CompetitionManagerImpl(this);
        this.integrationManager = new IntegrationManagerImpl(this);
        this.statisticsManager = new StatisticsManagerImpl(this);
        this.coolDownManager = new CoolDownManager(this);
        this.totemManager = new TotemManagerImpl(this);
        this.reload();
        if (CFConfig.updateChecker)
            this.versionManager.checkUpdate().thenAccept(result -> {
                if (!result) this.getAdventure().sendConsoleMessage("[CustomFishing] You are using the latest version.");
                else this.getAdventure().sendConsoleMessage("[CustomFishing] Update is available: <u>https://polymart.org/resource/customfishing.2723<!u>");
            });
    }

    @Override
    public void onDisable() {
        ((AdventureManagerImpl) this.adventure).close();
        ((BagManagerImpl) this.bagManager).disable();
        ((BlockManagerImpl) this.blockManager).disable();
        ((EffectManagerImpl) this.effectManager).disable();
        ((FishingManagerImpl) this.fishingManager).disable();
        ((GameManagerImpl) this.gameManager).disable();
        ((ItemManagerImpl) this.itemManager).disable();
        ((LootManagerImpl) this.lootManager).disable();
        ((MarketManagerImpl) this.marketManager).disable();
        ((EntityManagerImpl) this.entityManager).disable();
        ((RequirementManagerImpl) this.requirementManager).disable();
        ((SchedulerImpl) this.scheduler).shutdown();
        ((IntegrationManagerImpl) this.integrationManager).disable();
        ((StorageManagerImpl) this.storageManager).disable();
        ((CompetitionManagerImpl) this.competitionManager).disable();
        ((PlaceholderManagerImpl) this.placeholderManager).disable();
        ((StatisticsManagerImpl) this.statisticsManager).disable();
        ((ActionManagerImpl) this.actionManager).disable();
        ((TotemManagerImpl) this.totemManager).disable();
        this.coolDownManager.disable();
        this.commandManager.unload();
        HandlerList.unregisterAll(this);
    }

    @Override
    public void reload() {
        CFConfig.load();
        CFLocale.load();
        ((SchedulerImpl) this.scheduler).reload();
        ((RequirementManagerImpl) this.requirementManager).unload();
        ((RequirementManagerImpl) this.requirementManager).load();
        ((ActionManagerImpl) this.actionManager).unload();
        ((ActionManagerImpl) this.actionManager).load();
        ((GameManagerImpl) this.gameManager).unload();
        ((GameManagerImpl) this.gameManager).load();
        ((ItemManagerImpl) this.itemManager).unload();
        ((ItemManagerImpl) this.itemManager).load();
        ((LootManagerImpl) this.lootManager).unload();
        ((LootManagerImpl) this.lootManager).load();
        ((FishingManagerImpl) this.fishingManager).unload();
        ((FishingManagerImpl) this.fishingManager).load();
        ((TotemManagerImpl) this.totemManager).unload();
        ((TotemManagerImpl) this.totemManager).load();
        ((EffectManagerImpl) this.effectManager).unload();
        ((EffectManagerImpl) this.effectManager).load();
        ((MarketManagerImpl) this.marketManager).unload();
        ((MarketManagerImpl) this.marketManager).load();
        ((BagManagerImpl) this.bagManager).unload();
        ((BagManagerImpl) this.bagManager).load();
        ((BlockManagerImpl) this.blockManager).unload();
        ((BlockManagerImpl) this.blockManager).load();
        ((EntityManagerImpl) this.entityManager).unload();
        ((EntityManagerImpl) this.entityManager).load();
        ((CompetitionManagerImpl) this.competitionManager).unload();
        ((CompetitionManagerImpl) this.competitionManager).load();
        ((StorageManagerImpl) this.storageManager).reload();
        ((StatisticsManagerImpl) this.statisticsManager).unload();
        ((StatisticsManagerImpl) this.statisticsManager).load();
        ((PlaceholderManagerImpl) this.placeholderManager).unload();
        ((PlaceholderManagerImpl) this.placeholderManager).load();
        this.commandManager.unload();
        this.commandManager.load();
        this.coolDownManager.unload();
        this.coolDownManager.load();
    }

    private void loadDependencies() {
        String libRepo = TimeZone.getDefault().getID().startsWith("Asia") ?
                "https://maven.aliyun.com/repository/public/" : "https://repo.maven.apache.org/maven2/";
        LibraryLoader.loadDependencies(
                "org.apache.commons:commons-pool2:2.11.1", libRepo,
                "redis.clients:jedis:4.4.2", libRepo,
                "dev.dejvokep:boosted-yaml:1.3.1", libRepo,
                "com.zaxxer:HikariCP:5.0.1", libRepo,
                "net.objecthunter:exp4j:0.4.8", libRepo,
                "org.mariadb.jdbc:mariadb-java-client:3.1.4", libRepo,
                "mysql:mysql-connector-java:8.0.30", libRepo,
                "commons-io:commons-io:2.13.0", libRepo,
                "com.google.code.gson:gson:2.10.1", libRepo,
                "com.h2database:h2:2.2.220", libRepo,
                "org.mongodb:mongodb-driver-sync:4.10.2", libRepo,
                "org.mongodb:mongodb-driver-core:4.10.2", libRepo,
                "org.mongodb:bson:4.10.2", libRepo,
                "org.xerial:sqlite-jdbc:3.42.0.0", libRepo,
                "dev.jorel:commandapi-bukkit-shade:9.1.0", libRepo
        );
    }

    private void disableNBTAPILogs() {
        MinecraftVersion.disableBStats();
        MinecraftVersion.disableUpdateCheck();
        VersionChecker.hideOk = true;
        try {
            Field field = MinecraftVersion.class.getDeclaredField("version");
            field.setAccessible(true);
            MinecraftVersion minecraftVersion;
            try {
                minecraftVersion = MinecraftVersion.valueOf(getVersionManager().getServerVersion().replace("v", "MC"));
            } catch (IllegalArgumentException ex) {
                minecraftVersion = MinecraftVersion.UNKNOWN;
            }
            field.set(MinecraftVersion.class, minecraftVersion);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        boolean hasGsonSupport;
        try {
            Class.forName("com.google.gson.Gson");
            hasGsonSupport = true;
        } catch (Exception ex) {
            hasGsonSupport = false;
        }
        try {
            Field field= MinecraftVersion.class.getDeclaredField("hasGsonSupport");
            field.setAccessible(true);
            field.set(Boolean.class, hasGsonSupport);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public YamlConfiguration getConfig(String file) {
        File config = new File(this.getDataFolder(), file);
        if (!config.exists()) this.saveResource(file, false);
        return YamlConfiguration.loadConfiguration(config);
    }

    @Override
    public boolean isHookedPluginEnabled(String plugin) {
        return Bukkit.getPluginManager().isPluginEnabled(plugin);
    }

    @NotNull
    public static ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    @Override
    public void debug(String message) {
        if (!CFConfig.debug) return;
        LogUtils.info(message);
    }

    public CoolDownManager getCoolDownManager() {
        return coolDownManager;
    }
}
