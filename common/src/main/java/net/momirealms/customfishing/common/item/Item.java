package net.momirealms.customfishing.common.item;

import java.util.List;
import java.util.Optional;

public interface Item<I> {

    Item<I> customModelData(Integer data);

    Optional<Integer> customModelData();

    Item<I> displayName(String displayName);

    Optional<String> displayName();

    Item<I> lore(List<String> lore);

    Optional<List<String>> lore();

    Item<I> skull(String data);

    Optional<Object> getTag(Object... path);

    Item<I> setTag(Object value, Object... path);

    boolean hasTag(Object... path);

    boolean removeTag(Object... path);

    I getItem();

    I load();

    I loadCopy();

    void update();
}
