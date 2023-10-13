package net.momirealms.customfishing.gui.page.item;

import net.momirealms.customfishing.gui.icon.property.item.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.item.Item;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
public class RodEditor extends AbstractSectionEditor {

    public RodEditor(Player player, String key, ItemSelector itemSelector, ConfigurationSection section) {
        super(player, itemSelector, section, key);
    }

    @Override
    public List<Item> getItemList() {
        ArrayList<Item> items = new ArrayList<>();
        items.add(new MaterialItem(this));
        items.add(new DisplayNameItem(this));
        items.add(new LoreItem(this));
        items.add(new CMDItem(this));
        items.add(new TagItem(this));
        items.add(new UnbreakableItem(this));
        items.add(new DurabilityItem(this));
        items.add(new RandomDurabilityItem(this));
        items.add(new ItemFlagItem(this));
        items.add(new NBTItem(this));
        items.add(new EnchantmentItem(this));
        return items;
    }
}
