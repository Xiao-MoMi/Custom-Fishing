package net.momirealms.customfishing.bukkit;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitBootstrap extends JavaPlugin {

    private BukkitCustomFishingPlugin plugin;

    @Override
    public void onLoad() {
        this.plugin = new BukkitCustomFishingPluginImpl(this);
        this.plugin.load();
    }

    @Override
    public void onEnable() {
        this.plugin.enable();
    }

    @Override
    public void onDisable() {
        this.plugin.disable();
    }
}
