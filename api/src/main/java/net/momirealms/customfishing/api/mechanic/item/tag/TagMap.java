package net.momirealms.customfishing.api.mechanic.item.tag;

import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.entity.Player;

import java.util.Map;

public interface TagMap {

    Map<String, Object> apply(Context<Player> context);
}
