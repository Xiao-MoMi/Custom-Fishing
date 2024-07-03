package net.momirealms.customfishing.common.item;

import net.momirealms.customfishing.common.util.Key;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Item<I> {

    Item<I> customModelData(Integer data);

    Optional<Integer> customModelData();

    Item<I> damage(Integer data);

    Optional<Integer> damage();

    Item<I> displayName(String displayName);

    Optional<String> displayName();

    Item<I> lore(List<String> lore);

    Optional<List<String>> lore();

    Item<I> skull(String data);

    Item<I> enchantments(Map<Key, Short> enchantments);

    Item<I> addEnchantment(Key enchantment, int level);

    Item<I> storedEnchantments(Map<Key, Short> enchantments);

    Item<I> addStoredEnchantment(Key enchantment, int level);

    Optional<Object> getTag(Object... path);

    Item<I> setTag(Object value, Object... path);

    boolean hasTag(Object... path);

    boolean removeTag(Object... path);

    I getItem();

    I load();

    I loadCopy();

    void update();
}
