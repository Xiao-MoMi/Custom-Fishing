package net.momirealms.customfishing.api.mechanic.item.component;

import com.saicone.rtag.RtagItem;
import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.entity.Player;

public interface ComponentEditor {

    void apply(RtagItem item, Context<Player> context);
}
