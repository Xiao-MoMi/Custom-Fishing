package net.momirealms.customfishing.storage.method;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.DataStorageInterface;

import java.time.Instant;

public abstract class AbstractStorage implements DataStorageInterface {

    protected CustomFishingPlugin plugin;

    public AbstractStorage(CustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void disable() {

    }

    public int getCurrentSeconds() {
        return (int) Instant.now().getEpochSecond();
    }
}
