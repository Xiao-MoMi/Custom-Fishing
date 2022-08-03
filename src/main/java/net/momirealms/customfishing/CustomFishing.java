package net.momirealms.customfishing;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.momirealms.customfishing.command.Execute;
import net.momirealms.customfishing.command.TabComplete;
import net.momirealms.customfishing.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class CustomFishing extends JavaPlugin {

    public static JavaPlugin instance;
    public static BukkitAudiences adventure;

    @Override
    public void onEnable() {
        instance = this;
        adventure = BukkitAudiences.create(this);
        Objects.requireNonNull(Bukkit.getPluginCommand("customfishing")).setExecutor(new Execute());
        Objects.requireNonNull(Bukkit.getPluginCommand("customfishing")).setTabCompleter(new TabComplete());
        Bukkit.getPluginManager().registerEvents(new PlayerListener(),this);
        AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><color:#E1FFFF>Running on " + Bukkit.getVersion());
        ConfigReader.Reload();
        AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><color:#E1FFFF>Plugin Enabled!");
    }

    @Override
    public void onDisable() {
        if(adventure != null) {
            adventure.close();
            adventure = null;
        }
    }
}
