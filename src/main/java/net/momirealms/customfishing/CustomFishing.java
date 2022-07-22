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
    public void onLoad(){

//        Paper原生的adventure似乎存在冲突导致1.18.1以下版本无法使用，所以方法废弃
//        LibraryLoader.load("net.kyori","adventure-api","4.11.0","https://oss.sonatype.org/content/groups/public");
//        LibraryLoader.load("net.kyori","adventure-platform-api","4.1.1","https://oss.sonatype.org/content/groups/public");
//        LibraryLoader.load("net.kyori","adventure-platform-bukkit","4.1.1","https://oss.sonatype.org/content/groups/public");
//        LibraryLoader.load("net.kyori","adventure-platform-facet","4.1.1","https://oss.sonatype.org/content/groups/public");
//        LibraryLoader.load("net.kyori","adventure-text-serializer-gson","4.11.0","https://oss.sonatype.org/content/groups/public");
//        LibraryLoader.load("net.kyori","adventure-text-serializer-plain","4.11.0","https://oss.sonatype.org/content/groups/public");
//        LibraryLoader.load("net.kyori","adventure-text-serializer-gson-legacy-impl","4.11.0","https://oss.sonatype.org/content/groups/public");
//        LibraryLoader.load("net.kyori","adventure-text-serializer-legacy","4.11.0","https://oss.sonatype.org/content/groups/public");
//        LibraryLoader.load("net.kyori","adventure-nbt","4.11.0","https://oss.sonatype.org/content/groups/public");
//        LibraryLoader.load("net.kyori","adventure-key","4.11.0","https://oss.sonatype.org/content/groups/public");
//        LibraryLoader.load("net.kyori","adventure-text-minimessage","4.11.0","https://oss.sonatype.org/content/groups/public");

    }

    @Override
    public void onEnable() {
        instance = this;
        adventure = BukkitAudiences.create(this);
        Objects.requireNonNull(Bukkit.getPluginCommand("customfishing")).setExecutor(new Execute());
        Objects.requireNonNull(Bukkit.getPluginCommand("customfishing")).setTabCompleter(new TabComplete());
        Bukkit.getPluginManager().registerEvents(new PlayerListener(),this);
        AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><color:#E1FFFF>Running on " + Bukkit.getVersion());
        ConfigReader.Reload();
        AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><color:#E1FFFF>插件已加载! 作者:小默米 QQ:3266959688");
    }

    @Override
    public void onDisable() {
        if(adventure != null) {
            adventure.close();
            adventure = null;
        }
    }
}
