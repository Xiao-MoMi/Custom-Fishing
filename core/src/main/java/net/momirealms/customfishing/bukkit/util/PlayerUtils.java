package net.momirealms.customfishing.bukkit.util;

import net.momirealms.customfishing.common.util.RandomUtils;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public class PlayerUtils {

    public static void dropItem(@NotNull Player player, @NotNull ItemStack itemStack, boolean retainOwnership, boolean noPickUpDelay, boolean throwRandomly) {
        requireNonNull(player, "player");
        requireNonNull(itemStack, "itemStack");
        Location location = player.getLocation().clone();
        Item item = player.getWorld().dropItem(player.getEyeLocation().clone().subtract(new Vector(0,0.3,0)), itemStack);
        item.setPickupDelay(noPickUpDelay ? 0 : 40);
        if (retainOwnership) {
            item.setThrower(player.getUniqueId());
        }
        if (throwRandomly) {
            double d1 = RandomUtils.generateRandomDouble(0,1) * 0.5f;
            double d2 = RandomUtils.generateRandomDouble(0,1) * (Math.PI * 2);
            item.setVelocity(new Vector(-Math.sin(d2) * d1, 0.2f, Math.cos(d2) * d1));
        } else {
            double d1 = Math.sin(location.getPitch() * (Math.PI/180));
            double d2 = RandomUtils.generateRandomDouble(0, 0.02);
            double d3 = RandomUtils.generateRandomDouble(0,1) * (Math.PI * 2);
            Vector vector = location.getDirection().multiply(0.3).setY(-d1 * 0.3 + 0.1 + (RandomUtils.generateRandomDouble(0,1) - RandomUtils.generateRandomDouble(0,1)) * 0.1);
            vector.add(new Vector(Math.cos(d3) * d2, 0, Math.sin(d3) * d2));
            item.setVelocity(vector);
        }
    }
}
