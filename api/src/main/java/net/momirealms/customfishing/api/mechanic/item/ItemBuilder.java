package net.momirealms.customfishing.api.mechanic.item;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.momirealms.customfishing.api.common.Pair;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ItemBuilder {

    ItemBuilder customModelData(int value);

    ItemBuilder name(String name);

    ItemBuilder amount(int amount);

    ItemBuilder tag(boolean tag, String type, String id);

    ItemBuilder unbreakable(boolean unbreakable);

    ItemBuilder lore(List<String> lore);

    ItemBuilder nbt(Map<String, Object> nbt);

    ItemBuilder itemFlag(List<ItemFlag> itemFlags);

    ItemBuilder nbt(ConfigurationSection section);

    ItemBuilder enchantment(List<Pair<String, Short>> enchantments, boolean store);

    ItemBuilder maxDurability(int max);

    ItemBuilder price(float base, float bonus);

    ItemBuilder size(Pair<Float, Float> size);

    ItemBuilder stackable(boolean stackable);

    ItemBuilder preventGrabbing(boolean prevent);

    ItemBuilder head(String base64);

    ItemBuilder randomDamage(boolean damage);

    @NotNull
    String getId();

    @NotNull
    String getLibrary();

    int getAmount();

    Collection<ItemPropertyEditor> getEditors();

    ItemBuilder removeEditor(String type);

    ItemBuilder registerCustomEditor(String type, ItemPropertyEditor editor);

    interface ItemPropertyEditor {

        void edit(Player player, NBTItem nbtItem, Map<String, String> placeholders);

        default void edit(Player player, NBTItem nbtItem) {
            edit(player, nbtItem, null);
        }
    }
}
