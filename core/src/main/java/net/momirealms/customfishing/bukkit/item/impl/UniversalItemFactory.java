package net.momirealms.customfishing.bukkit.item.impl;

import com.saicone.rtag.RtagItem;
import net.momirealms.customfishing.bukkit.item.BukkitItemFactory;
import net.momirealms.customfishing.common.plugin.CustomFishingPlugin;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UniversalItemFactory extends BukkitItemFactory {

    public UniversalItemFactory(CustomFishingPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void displayName(RtagItem item, String json) {
        if (json != null) {
            item.set(json, "display", "Name");
        } else {
            item.remove("display", "Name");
        }
    }

    @Override
    protected Optional<String> displayName(RtagItem item) {
        if (!item.hasTag("display", "Name")) return Optional.empty();
        return Optional.of(item.get("display", "Name"));
    }

    @Override
    protected void customModelData(RtagItem item, Integer data) {
        if (data == null) {
            item.remove("CustomModelData");
        } else {
            item.set(data, "CustomModelData");
        }
    }

    @Override
    protected Optional<Integer> customModelData(RtagItem item) {
        if (!item.hasTag("CustomModelData")) return Optional.empty();
        return Optional.of(item.get("CustomModelData"));
    }

    @Override
    protected void skull(RtagItem item, String skullData) {
        if (skullData == null) {
            item.remove("SkullOwner");
        } else {
            item.set(List.of(Map.of("Value", skullData)), "SkullOwner", "Properties", "textures");
        }
    }

    @Override
    protected Optional<List<String>> lore(RtagItem item) {
        if (!item.hasTag("display", "Lore")) return Optional.empty();
        return Optional.of(item.get("display", "Lore"));
    }

    @Override
    protected void lore(RtagItem item, List<String> lore) {
        if (lore == null || lore.isEmpty()) {
            item.remove("display", "Lore");
        } else {
            item.set(lore, "display", "Lore");
        }
    }

    @Override
    protected boolean unbreakable(RtagItem item) {
        return item.isUnbreakable();
    }

    @Override
    protected void unbreakable(RtagItem item, boolean unbreakable) {
        item.setUnbreakable(unbreakable);
    }

    @Override
    protected Optional<Boolean> glint(RtagItem item) {
        return Optional.of(false);
    }

    @Override
    protected void glint(RtagItem item, Boolean glint) {
        throw new UnsupportedOperationException("This feature is only available on 1.20.5+");
    }

    @Override
    protected Optional<Integer> damage(RtagItem item) {
        if (!item.hasTag("Damage")) return Optional.empty();
        return Optional.of(item.get("Damage"));
    }

    @Override
    protected void damage(RtagItem item, Integer damage) {
        item.set(damage, "Damage");
    }
}