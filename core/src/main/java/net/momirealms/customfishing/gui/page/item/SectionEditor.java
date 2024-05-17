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

package net.momirealms.customfishing.gui.page.item;

import net.momirealms.customfishing.gui.icon.property.item.*;
import net.momirealms.customfishing.gui.icon.property.loot.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.item.Item;

import java.util.ArrayList;
import java.util.List;

public class SectionEditor extends AbstractSectionEditor {

    public SectionEditor(Player player, String key, ItemSelector itemSelector, ConfigurationSection section) {
        super(player, itemSelector, section, key);
    }

    @Override
    public List<Item> getItemList() {
        ArrayList<Item> items = new ArrayList<>();
        items.add(new MaterialItem(this));
        items.add(new NickItem(this));
        items.add(new DisplayNameItem(this));
        items.add(new LoreItem(this));
        items.add(new CMDItem(this));
        items.add(new AmountItem(this));
        items.add(new TagItem(this));
        items.add(new UnbreakableItem(this));
        items.add(new DurabilityItem(this));
        items.add(new RandomDurabilityItem(this));
        items.add(new StackableItem(this));
        items.add(new PreventGrabItem(this));
        items.add(new PriceItem(this));
        items.add(new ShowInFinderItem(this));
        items.add(new DisableStatsItem(this));
        items.add(new DisableGameItem(this));
        items.add(new InstantGameItem(this));
        items.add(new ScoreItem(this));
        items.add(new SizeItem(this));
        items.add(new ItemFlagItem(this));
        items.add(new Head64Item(this));
        items.add(new NBTItem(this));
        items.add(new EnchantmentItem(this));
        items.add(new StoredEnchantmentItem(this));
        return items;
    }
}
