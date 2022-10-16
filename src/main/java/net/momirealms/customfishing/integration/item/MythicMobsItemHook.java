package net.momirealms.customfishing.integration.item;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.ItemExecutor;
import net.momirealms.customfishing.integration.ItemInterface;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MythicMobsItemHook implements ItemInterface {

    private final ItemExecutor itemManager;

    public MythicMobsItemHook() {
        this.itemManager = MythicBukkit.inst().getItemManager();
    }

    @Override
    @Nullable
    public ItemStack build(String material) {
        if (!material.startsWith("MythicMobs:")) return null;
        material = material.substring(11);
        return itemManager.getItemStack(material);
    }
}
