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

package net.momirealms.customfishing.bukkit.gui.icon;

import net.momirealms.customfishing.bukkit.adventure.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.bukkit.gui.Icon;
import net.momirealms.customfishing.bukkit.gui.page.file.FileSelector;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.locale.MessageConstants;
import net.momirealms.customfishing.common.locale.TranslationManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.io.File;
import java.util.List;

public class BackToFolderItem extends AbstractItem implements Icon {

    private final File file;

    public BackToFolderItem(File file) {
        this.file = file;
    }

    @Override
    public ItemProvider getItemProvider() {
        if (file != null && (file.getPath().startsWith("plugins\\CustomFishing\\contents") || file.getPath().startsWith("plugins/CustomFishing/contents"))) {
            return new ItemBuilder(Material.ORANGE_STAINED_GLASS_PANE)
                    .setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_BACK_TO_PARENT_FOLDER.build())))
                    .setLore(List.of(new ShadedAdventureComponentWrapper(AdventureHelper.miniMessage(
                            "<#FFA500>-> " + file.getName()
                    ))));
        } else {
            return new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE);
        }
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (file != null && (file.getPath().startsWith("plugins\\CustomFishing\\contents") || file.getPath().startsWith("plugins/CustomFishing/contents")))
            new FileSelector(player, file);
    }
}
