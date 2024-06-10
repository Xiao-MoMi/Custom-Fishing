/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.bukkit.gui.page.item;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.bukkit.gui.icon.property.item.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.item.Item;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
public class RodEditor extends AbstractSectionEditor {

    public RodEditor(Player player, String key, ItemSelector itemSelector, Section section) {
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
