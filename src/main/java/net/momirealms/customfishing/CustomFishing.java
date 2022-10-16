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
import net.momirealms.customfishing.commands.PluginCommand;
import net.momirealms.customfishing.competition.CompetitionSchedule;
import net.momirealms.customfishing.helper.LibraryLoader;
import net.momirealms.customfishing.manager.FishingManager;
import net.momirealms.customfishing.manager.IntegrationManager;
import net.momirealms.customfishing.manager.MessageManager;
import net.momirealms.customfishing.util.AdventureUtil;
import net.momirealms.customfishing.util.ConfigUtil;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomFishing extends JavaPlugin {

    public static CustomFishing plugin;
    public static BukkitAudiences adventure;
    public static ProtocolManager protocolManager;

    private IntegrationManager integrationManager;
    private FishingManager fishingManager;
    private CompetitionSchedule competitionSchedule;

//                              _ooOoo_
//                             o8888888o
//                             88" . "88
//                             (| -_- |)
//                             O\  =  /O
//                          ____/`---'\____
//                        .'  \\|     |//  `.
//                       /  \\|||  :  |||//  \
//                      /  _||||| -:- |||||_  \
//                      |   | \\\  -  /'| |   |
//                      | \_|  `\`---'//  |_/ |
//                      \  .-\__ `-. -'__/-.  /
//                    ___`. .'  /--.--\  `. .'___
//                 ."" '<  `.___\_<|>_/___.' _> \"".
//                | | :  `- \`. ;`. _/; .'/ /  .' ; |
//                \  \ `-.   \_\_`. _.'_/_/  -' _.' /
//  ================-.`___`-.__\ \___  /__.-'_.'_.-'================
//                              `=--=-'
//                   佛祖保佑    永无BUG    永不卡服

    @Override
    public void onLoad() {
        plugin = this;
        LibraryLoader.load("redis.clients","jedis","4.2.3","https://repo.maven.apache.org/maven2/");
        LibraryLoader.load("org.apache.commons","commons-pool2","2.11.1","https://repo.maven.apache.org/maven2/");
        LibraryLoader.load("dev.dejvokep","boosted-yaml","1.3","https://repo.maven.apache.org/maven2/");
    }

    @Override
    public void onEnable() {
        adventure = BukkitAudiences.create(this);
        protocolManager = ProtocolLibrary.getProtocolManager();
        this.fishingManager = new FishingManager();
        this.integrationManager = new IntegrationManager();
        this.competitionSchedule = new CompetitionSchedule();
        ConfigUtil.reload();

        PluginCommand pluginCommand = new PluginCommand();
        Bukkit.getPluginCommand("customfishing").setExecutor(pluginCommand);
        Bukkit.getPluginCommand("customfishing").setTabCompleter(pluginCommand);
        AdventureUtil.consoleMessage("[CustomFishing] Plugin Enabled!");
        new Metrics(this, 16648);
    }

    @Override
    public void onDisable() {
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }

    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }

    public FishingManager getFishingManager() {
        return fishingManager;
    }

    public CompetitionSchedule getCompetitionSchedule() {
        return competitionSchedule;
    }
}
