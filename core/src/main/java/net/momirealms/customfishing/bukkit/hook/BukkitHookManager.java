package net.momirealms.customfishing.bukkit.hook;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.hook.HookConfig;
import net.momirealms.customfishing.api.mechanic.hook.HookManager;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BukkitHookManager implements HookManager {

    private BukkitCustomFishingPlugin plugin;

    public BukkitHookManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean registerHook(HookConfig hook) {
        return false;
    }

    @NotNull
    @Override
    public Optional<HookConfig> getHook(String id) {
        return Optional.empty();
    }
}
