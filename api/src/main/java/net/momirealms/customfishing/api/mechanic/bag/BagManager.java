package net.momirealms.customfishing.api.mechanic.bag;

import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import org.bukkit.entity.Player;

public interface BagManager extends Reloadable {

    int getBagInventoryRows(Player player);
}
