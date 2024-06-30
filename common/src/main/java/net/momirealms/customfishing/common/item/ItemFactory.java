package net.momirealms.customfishing.common.item;

import net.momirealms.customfishing.common.plugin.CustomFishingPlugin;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class ItemFactory<P extends CustomFishingPlugin, R, I> {

    protected final P plugin;

    protected ItemFactory(P plugin) {
        this.plugin = plugin;
    }

    public Item<I> wrap(R item) {
        Objects.requireNonNull(item, "item");
        return new AbstractItem<>(this.plugin, this, item);
    }

    protected abstract Optional<Object> getTag(R item, Object... path);

    protected abstract void setTag(R item, Object value, Object... path);

    protected abstract boolean hasTag(R item, Object... path);

    protected abstract boolean removeTag(R item, Object... path);

    protected abstract void update(R item);

    protected abstract I load(R item);

    protected abstract I getItem(R item);

    protected abstract I loadCopy(R item);

    protected abstract void customModelData(R item, Integer data);

    protected abstract Optional<Integer> customModelData(R item);

    protected abstract void displayName(R item, String json);

    protected abstract Optional<String> displayName(R item);

    protected abstract void skull(R item, String skullData);

    protected abstract Optional<List<String>> lore(R item);

    protected abstract void lore(R item, List<String> lore);

    protected abstract boolean unbreakable(R item);

    protected abstract void unbreakable(R item, boolean unbreakable);

    protected abstract Optional<Boolean> glint(R item);

    protected abstract void glint(R item, Boolean glint);

    protected abstract Optional<Integer> damage(R item);

    protected abstract void damage(R item, Integer damage);
}
