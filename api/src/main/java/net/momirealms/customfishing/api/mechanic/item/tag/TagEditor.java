package net.momirealms.customfishing.api.mechanic.item.tag;

import com.saicone.rtag.RtagItem;
import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface TagEditor {

    void apply(RtagItem item, Context<Player> context);
}
