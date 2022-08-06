package net.momirealms.customfishing.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PapiHook {
    public static String parse(Player player, String text){
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
