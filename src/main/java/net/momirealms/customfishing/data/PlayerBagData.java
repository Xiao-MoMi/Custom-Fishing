package net.momirealms.customfishing.data;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;

public class PlayerBagData {

    private final OfflinePlayer player;
    private Inventory inventory;

    public PlayerBagData(OfflinePlayer player, Inventory inventory) {
        this.player = player;
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
