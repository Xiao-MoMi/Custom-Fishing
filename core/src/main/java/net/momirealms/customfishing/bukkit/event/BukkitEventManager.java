package net.momirealms.customfishing.bukkit.event;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.event.EventCarrier;
import net.momirealms.customfishing.api.mechanic.event.EventManager;

import java.util.HashMap;
import java.util.Optional;

public class BukkitEventManager implements EventManager {

    private final HashMap<String, EventCarrier> carrierMap = new HashMap<>();
    private BukkitCustomFishingPlugin plugin;

    public BukkitEventManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Optional<EventCarrier> getEventCarrier(String id) {
        return Optional.ofNullable(carrierMap.get(id));
    }

    @Override
    public boolean registerEventCarrier(String id, EventCarrier carrier) {
        if (carrierMap.containsKey(id)) {
            return false;
        }
        carrierMap.put(id, carrier);
        return true;
    }
}
