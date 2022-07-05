package net.momirealms.customfishing.requirements;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public record FishingCondition(Player player, Location location) {

    public Location getLocation() { return location; }
    public Player getPlayer() {
        return player;
    }
}