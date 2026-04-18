/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.bukkit.item;

import com.saicone.rtag.RtagItem;
import net.momirealms.customfishing.bukkit.item.impl.ComponentItemFactory;
import net.momirealms.customfishing.bukkit.item.impl.ComponentItemFactory1_21_5;
import net.momirealms.customfishing.bukkit.item.impl.UniversalItemFactory;
import net.momirealms.customfishing.common.helper.VersionHelper;
import net.momirealms.customfishing.common.item.Item;
import net.momirealms.customfishing.common.item.ItemFactory;
import net.momirealms.customfishing.common.plugin.CustomFishingPlugin;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.Optional;

public abstract class BukkitItemFactory extends ItemFactory<CustomFishingPlugin, RtagItem, ItemStack> {

    protected BukkitItemFactory(CustomFishingPlugin plugin) {
        super(plugin);
    }

    public static BukkitItemFactory create(CustomFishingPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        if (VersionHelper.isVersionNewerThan1_21_5()) {
            return new ComponentItemFactory1_21_5(plugin);
        } else if (VersionHelper.isVersionNewerThan1_20_5()) {
            return new ComponentItemFactory(plugin);
        } else {
            return new UniversalItemFactory(plugin);
        }
    }

    public Item<ItemStack> wrap(ItemStack item) {
        Objects.requireNonNull(item, "item");
        return wrap(new RtagItem(item));
    }

    @Override
    protected void setTag(RtagItem item, Object value, Object... path) {
        item.set(value, path);
    }

    @Override
    protected Optional<Object> getTag(RtagItem item, Object... path) {
        return Optional.ofNullable(item.get(path));
    }

    @Override
    protected boolean hasTag(RtagItem item, Object... path) {
        return item.hasTag(path);
    }

    @Override
    protected boolean removeTag(RtagItem item, Object... path) {
        return item.remove(path);
    }

    @Override
    protected void update(RtagItem item) {
        item.update();
    }

    @Override
    protected ItemStack load(RtagItem item) {
        return item.load();
    }

    @Override
    protected ItemStack getItem(RtagItem item) {
        return item.getItem();
    }

    @Override
    protected ItemStack loadCopy(RtagItem item) {
        return item.loadCopy();
    }
}
