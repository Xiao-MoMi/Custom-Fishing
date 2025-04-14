package net.momirealms.customfishing.bukkit.item;

import com.saicone.rtag.item.ItemTagStream;
import net.momirealms.customfishing.api.integration.ItemProvider;
import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SNBTItemProvider implements ItemProvider {

    @Override
    public @NotNull ItemStack buildItem(@NotNull Context<Player> player, @NotNull String id) {
        return ItemTagStream.INSTANCE.fromString(id);
    }

    @Nullable
    @Override
    public String itemID(@NotNull ItemStack itemStack) {
        return null;
    }

    @Override
    public String identifier() {
        return "snbt";
    }
}
