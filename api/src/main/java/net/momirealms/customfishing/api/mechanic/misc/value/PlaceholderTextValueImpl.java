package net.momirealms.customfishing.api.mechanic.misc.value;

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.misc.placeholder.BukkitPlaceholderManager;
import org.bukkit.OfflinePlayer;

import java.util.Map;

public class PlaceholderTextValueImpl<T> implements TextValue<T> {

    private final String raw;

    public PlaceholderTextValueImpl(String raw) {
        this.raw = raw;
    }

    @Override
    public String render(Context<T> context) {
        Map<String, String> replacements = context.placeholderMap();
        String text;
        if (context.getHolder() instanceof OfflinePlayer player) text = BukkitPlaceholderManager.getInstance().parse(player, raw, replacements);
        else text = BukkitPlaceholderManager.getInstance().parse(null, raw, replacements);
        return text;
    }
}
