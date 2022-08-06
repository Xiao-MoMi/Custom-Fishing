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

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.momirealms.customfishing.command.Execute;
import net.momirealms.customfishing.command.TabComplete;
import net.momirealms.customfishing.competition.CompetitionSchedule;
import net.momirealms.customfishing.competition.bossbar.BossBarManager;
import net.momirealms.customfishing.listener.PlayerListener;
import net.momirealms.customfishing.utils.AdventureManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.C;

import java.util.Objects;

public final class CustomFishing extends JavaPlugin {

    public static JavaPlugin instance;
    public static BukkitAudiences adventure;
    private CompetitionSchedule competitionSchedule;

    @Override
    public void onEnable() {
        instance = this;
        adventure = BukkitAudiences.create(this);
        Objects.requireNonNull(Bukkit.getPluginCommand("customfishing")).setExecutor(new Execute());
        Objects.requireNonNull(Bukkit.getPluginCommand("customfishing")).setTabCompleter(new TabComplete());
        Bukkit.getPluginManager().registerEvents(new PlayerListener(),this);
        AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><color:#E1FFFF>Running on " + Bukkit.getVersion());
        ConfigReader.Reload();
        if (ConfigReader.Config.competition){
            competitionSchedule = new CompetitionSchedule();
            competitionSchedule.checkTime();
            Bukkit.getPluginManager().registerEvents(new BossBarManager(), this);
        }
        AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><color:#E1FFFF>Plugin Enabled!");
    }

    @Override
    public void onDisable() {
        if (competitionSchedule != null){
            competitionSchedule.stopCheck();
            competitionSchedule = null;
        }
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
        if (instance != null){
            instance = null;
        }
    }
}
