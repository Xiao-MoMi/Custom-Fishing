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

package net.momirealms.customfishing.bukkit.gui.page.property;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import net.momirealms.customfishing.bukkit.adventure.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.bukkit.gui.SectionPage;
import net.momirealms.customfishing.bukkit.gui.icon.BackGroundItem;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.locale.MessageConstants;
import net.momirealms.customfishing.common.locale.TranslationManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.window.AnvilWindow;

import java.util.*;

@SuppressWarnings("DuplicatedCode")
public class NBTEditor {

    private final Player player;
    private final SectionPage parentPage;
    private Section nbtSection;
    private Section currentSection;
    private String value;
    private String currentNode;

    public NBTEditor(Player player, SectionPage parentPage) {
        this.player = player;
        this.parentPage = parentPage;
        this.nbtSection = parentPage.getSection().getSection("nbt");
        if (this.nbtSection == null)
            this.nbtSection = parentPage.getSection().createSection("nbt");
        this.currentSection = nbtSection;
        this.currentNode = "";
        reOpenMain();
    }

    public List<Item> getNBTContents() {
        Deque<Item> deque = new ArrayDeque<>();
        for (Map.Entry<String, Object> entry : currentSection.getStringRouteMappedValues(false).entrySet()) {
            String path = Objects.equals(currentNode, "") ? entry.getKey() : currentNode + "." + entry.getKey();
            if (entry.getValue() instanceof List<?> list) {
                deque.addLast(new InvListIcon(path));
            } else if (entry.getValue() instanceof String str) {
                deque.addLast(new InvValueIcon(path, str));
            } else if (entry.getValue() instanceof Section inner) {
                deque.addFirst(new InvCompoundIcon(path, inner));
            } else if (entry.getValue() instanceof Map<?,?> map) {
                deque.addLast(new InvMapIcon(path));
            }
        }
        deque.addLast(new NewCompoundIcon());
        deque.addLast(new NewListIcon());
        deque.addLast(new NewValueIcon());
        if (currentSection.getParent() != null && !currentSection.getName().equals("nbt")) {
            deque.addLast(new BackToParentIcon());
        }
        return new ArrayList<>(deque);
    }

    public void reOpenMain() {
        Gui upperGui = Gui.normal()
                .setStructure("b b c")
                .addIngredient('b', new ItemStack(Material.AIR))
                .addIngredient('c', new SaveIcon())
                .build();

        var gui = PagedGui.items()
                .setStructure(
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "# # # # c # # # #"
                )
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('c', parentPage.getBackItem())
                .addIngredient('#', new BackGroundItem())
                .setContent(getNBTContents())
                .build();

        var window = AnvilWindow.split()
                .setViewer(player)
                .setTitle(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_PAGE_NBT_EDIT_TITLE.build())))
                .setUpperGui(upperGui)
                .setLowerGui(gui)
                .build();

        window.open();
    }

    public void reOpenAddCompound() {
        var confirm = new ConfirmCompoundItem();
        Gui upperGui = Gui.normal()
                .setStructure("a b c")
                .addIngredient('a', new ItemBuilder(Material.COMMAND_BLOCK_MINECART).setDisplayName(""))
                .addIngredient('b', new ItemStack(Material.AIR))
                .addIngredient('c', confirm)
                .build();

        value = "";

        var gui = PagedGui.items()
                .setStructure(
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "# # # # c # # # #"
                )
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('c', parentPage.getBackItem())
                .addIngredient('#', new BackGroundItem())
                .setContent(getNBTContents())
                .build();

        var window = AnvilWindow.split()
                .setViewer(player)
                .setTitle(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_PAGE_NBT_COMPOUND_KEY_TITLE.build())))
                .addRenameHandler(s -> {
                    value = s;
                    confirm.notifyWindows();
                })
                .setUpperGui(upperGui)
                .setLowerGui(gui)
                .build();

        window.open();
    }

    public void reOpenAddList() {
        var confirm = new ConfirmListItem();
        Gui upperGui = Gui.normal()
                .setStructure("a b c")
                .addIngredient('a', new ItemBuilder(Material.CHAIN_COMMAND_BLOCK).setDisplayName(""))
                .addIngredient('b', new ItemStack(Material.AIR))
                .addIngredient('c', confirm)
                .build();

        value = "";

        var gui = PagedGui.items()
                .setStructure(
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "# # # # c # # # #"
                )
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('c', parentPage.getBackItem())
                .addIngredient('#', new BackGroundItem())
                .setContent(getNBTContents())
                .build();

        var window = AnvilWindow.split()
                .setViewer(player)
                .setTitle(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_PAGE_NBT_LIST_KEY_TITLE.build())))
                .addRenameHandler(s -> {
                    value = s;
                    confirm.notifyWindows();
                })
                .setUpperGui(upperGui)
                .setLowerGui(gui)
                .build();

        window.open();
    }

    public void reOpenAddValue() {
        var confirm =new ConfirmValueItem();
        Gui upperGui = Gui.normal()
                .setStructure("a b c")
                .addIngredient('a', new ItemBuilder(Material.COMMAND_BLOCK).setDisplayName(""))
                .addIngredient('b', new ItemStack(Material.AIR))
                .addIngredient('c', confirm)
                .build();

        value = "";

        var gui = PagedGui.items()
                .setStructure(
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "# # # # c # # # #"
                )
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('c', parentPage.getBackItem())
                .addIngredient('#', new BackGroundItem())
                .setContent(getNBTContents())
                .build();

        var window = AnvilWindow.split()
                .setViewer(player)
                .setTitle(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_PAGE_NBT_KEY_TITLE.build())))
                .addRenameHandler(s -> {
                    value = s;
                    confirm.notifyWindows();
                })
                .setUpperGui(upperGui)
                .setLowerGui(gui)
                .build();

        window.open();
    }

    public void reOpenSetValue(String key, String type) {
        var save = new SaveValueIcon(key);
        Gui upperGui = Gui.normal()
                .setStructure("a b c")
                .addIngredient('a', new ItemBuilder(Material.COMMAND_BLOCK).setDisplayName(type == null ? "" : "(" + type + ") "))
                .addIngredient('b', new ItemStack(Material.AIR))
                .addIngredient('c', save)
                .build();

        value = "";

        var gui = PagedGui.items()
                .setStructure(
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "# # # # c # # # #"
                )
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('c', parentPage.getBackItem())
                .addIngredient('#', new BackGroundItem())
                .setContent(getTypeContents(key))
                .build();

        var window = AnvilWindow.split()
                .setViewer(player)
                .setTitle(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_PAGE_NBT_SET_VALUE_TITLE.build())))
                .addRenameHandler(s -> {
                    value = s;
                    save.notifyWindows();
                })
                .setUpperGui(upperGui)
                .setLowerGui(gui)
                .build();

        window.open();
    }

    public void removeByNode(String node) {
        nbtSection.set(node, null);
        parentPage.save();
    }

    public List<Item> getTypeContents(String key) {
        ArrayList<Item> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : Map.of(
                "String","some text",
                "Byte","1",
                "Short","123",
                "Int","123456",
                "Long","123456789",
                "Double", "1.2345",
                "Float", "1.23",
                "Boolean", "true",
                "IntArray", "[111,222,333,444]",
                "ByteArray","[1,2,3,4]"
        ).entrySet()) {
            list.add(new TypeItem(key, entry.getKey(), entry.getValue()));
        }
        return list;
    }

    public class TypeItem extends AbstractItem {

        private final String type;
        private final String tip;
        private final String key;

        public TypeItem(String key, String type, String tip) {
            this.type = type;
            this.tip = tip;
            this.key = key;
        }

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.BELL).setDisplayName(new ShadedAdventureComponentWrapper(AdventureHelper.miniMessage("(" + type + ") " + tip)));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            reOpenSetValue(key, type);
        }
    }

    public class ConfirmCompoundItem extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            if (value == null || value.isEmpty() || value.contains(".") || currentSection.contains(value)) {
                return new ItemBuilder(Material.BARRIER).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_PAGE_NBT_INVALID_KEY.build())));
            }

            return new ItemBuilder(Material.COMMAND_BLOCK_MINECART)
                    .setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_NEW_VALUE.arguments(Component.text(value)).build())))
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_CLICK_CONFIRM.build())))
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_RIGHT_CLICK_CANCEL.build())));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (clickType.isLeftClick()) {
                if (value == null || value.isEmpty() || value.contains(".")) {
                    return;
                }
                if (currentSection.contains(value)) {
                    return;
                }
                currentSection.createSection(value);
                parentPage.save();
            }
            reOpenMain();
        }
    }

    public class ConfirmListItem extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            if (value == null || value.isEmpty() || value.contains(".") || currentSection.contains(value)) {
                return new ItemBuilder(Material.BARRIER).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_PAGE_NBT_INVALID_KEY.build())));
            }
            return new ItemBuilder(Material.CHAIN_COMMAND_BLOCK)
                    .setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_NEW_VALUE.arguments(Component.text(value)).build())))
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_CLICK_CONFIRM.build())))
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_RIGHT_CLICK_CANCEL.build())));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (clickType.isLeftClick()) {
                if (value == null || value.isEmpty() || value.contains(".")) {
                    return;
                }
                if (currentSection.contains(value)) {
                    return;
                }
                currentSection.set(value, new ArrayList<>());
                parentPage.save();
            }
            reOpenMain();
        }
    }

    public class ConfirmValueItem extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            if (value == null || value.isEmpty() || value.contains(".") || currentSection.contains(value)) {
                return new ItemBuilder(Material.BARRIER).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_PAGE_NBT_INVALID_KEY.build())));
            }

            return new ItemBuilder(Material.COMMAND_BLOCK)
                    .setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_NEW_VALUE.arguments(Component.text(value)).build())))
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_CLICK_CONFIRM.build())))
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_RIGHT_CLICK_CANCEL.build())));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (clickType.isLeftClick()) {
                if (value == null || value.isEmpty() || value.contains(".")) {
                    return;
                }
                if (currentSection.contains(value)) {
                    return;
                }
                reOpenSetValue(value, null);
            } else if (clickType.isRightClick()) {
                reOpenMain();
            }
        }
    }

    public class NewCompoundIcon extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.OAK_SIGN).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_PAGE_NBT_ADD_NEW_COMPOUND.build())));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            reOpenAddCompound();
        }
    }

    public class NewListIcon extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.SPRUCE_SIGN).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_PAGE_NBT_ADD_NEW_LIST.build())));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            reOpenAddList();
        }
    }

    public class NewValueIcon extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.ACACIA_SIGN).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_PAGE_NBT_ADD_NEW_VALUE.build())));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            reOpenAddValue();
        }
    }

    public class InvCompoundIcon extends AbstractItem {

        private final String node;
        private final Section compound;

        public InvCompoundIcon(String node, Section compound) {
            this.compound = compound;
            this.node = node;
        }

        @Override
        public ItemProvider getItemProvider() {
            String[] splits = node.split("\\.");
            return new ItemBuilder(Material.COMMAND_BLOCK_MINECART)
                    .setDisplayName(new ShadedAdventureComponentWrapper(AdventureHelper.miniMessage("Compound: " + splits[splits.length -1])))
                    .addLoreLines("")
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_LEFT_CLICK_EDIT.build())))
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_RIGHT_CLICK_DELETE.build())));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (clickType.isLeftClick()) {
                currentSection = compound;
                currentNode = node;
            } else if (clickType.isRightClick()) {
                removeByNode(node);
            }
            reOpenMain();
        }
    }

    public class InvValueIcon extends AbstractItem {

        private final String node;
        private final String value;

        public InvValueIcon(String node, String value) {
            this.node = node;
            this.value = value;
        }

        @Override
        public ItemProvider getItemProvider() {
            String[] splits = node.split("\\.");
            return new ItemBuilder(Material.REPEATING_COMMAND_BLOCK)
                    .setDisplayName(new ShadedAdventureComponentWrapper(AdventureHelper.miniMessage(splits[splits.length -1] + ": " + value)))
                    .addLoreLines("")
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_LEFT_CLICK_EDIT.build())))
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_RIGHT_CLICK_DELETE.build())));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            String[] split = node.split("\\.");
            if (clickType.isLeftClick()) {
                //reOpenSetValue(split[split.length-1], NBTUtils.getTypeAndData(value)[0]);
            } else if (clickType.isRightClick()) {
                removeByNode(node);
                reOpenMain();
            }
        }
    }

    public class InvListIcon extends AbstractItem {

        private final String node;

        public InvListIcon(String node) {
            this.node = node;
        }

        @Override
        public ItemProvider getItemProvider() {
            String[] splits = node.split("\\.");
            return new ItemBuilder(Material.CHAIN_COMMAND_BLOCK)
                    .setDisplayName(new ShadedAdventureComponentWrapper(AdventureHelper.miniMessage("List: " + splits[splits.length -1])))
                    .addLoreLines("")
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_LEFT_CLICK_EDIT.build())))
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_RIGHT_CLICK_DELETE.build())));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (clickType.isRightClick()) {
                removeByNode(node);
                reOpenMain();
            }
        }
    }

    public class InvMapIcon extends AbstractItem {

        private final String node;

        public InvMapIcon(String node) {
            this.node = node;
        }

        @Override
        public ItemProvider getItemProvider() {
            String[] splits = node.split("\\.");
            return new ItemBuilder(Material.COMMAND_BLOCK)
                    .setDisplayName(new ShadedAdventureComponentWrapper(AdventureHelper.miniMessage("Map: " + splits[splits.length -1])))
                    .addLoreLines("")
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_LEFT_CLICK_EDIT.build())))
                    .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_RIGHT_CLICK_DELETE.build())));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (clickType.isRightClick()) {
                removeByNode(node);
                reOpenMain();
            }
        }
    }

    public class SaveValueIcon extends AbstractItem {

        private final String key;

        public SaveValueIcon(String key) {
            this.key = key;
        }

        @Override
        public ItemProvider getItemProvider() {
            try {
                //NBTUtils.getTypeAndData(value);
                return new ItemBuilder(Material.COMMAND_BLOCK)
                        .setDisplayName(value)
                        .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_CLICK_CONFIRM.build())))
                        .addLoreLines(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_RIGHT_CLICK_CANCEL.build())));
            } catch (IllegalArgumentException e) {
                return new ItemBuilder(Material.BARRIER).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(
                        MessageConstants.GUI_ILLEGAL_FORMAT.build()
                )));
            }
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (clickType == ClickType.LEFT) {
                try {
                    //NBTUtils.getTypeAndData(value);
                    currentSection.set(key, value);
                    parentPage.save();
                    reOpenMain();
                } catch (IllegalArgumentException e) {
                    reOpenMain();
                }
            } else if (clickType == ClickType.RIGHT) {
                reOpenMain();
            }
        }
    }

    public class SaveIcon extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            if (!nbtSection.getStringRouteMappedValues(false).isEmpty()) {
                var builder = new ItemBuilder(Material.ACACIA_SIGN).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_PAGE_NBT_PREVIEW.build())));
//                for (String line : ConfigUtils.getReadableSection(nbtSection.getValues(false))) {
//                    builder.addLoreLines(new ShadedAdventureComponentWrapper(AdventureHelper.miniMessage(line)));
//                }
                return builder;
            } else {
                return new ItemBuilder(Material.STRUCTURE_VOID).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_DELETE_PROPERTY.build())));
            }
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (nbtSection.getStringRouteMappedValues(false).isEmpty()) {
                Objects.requireNonNull(nbtSection.getParent()).set("nbt", null);
            }
            parentPage.save();
            parentPage.reOpen();
        }
    }

    public class BackToParentIcon extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.MINECART).setDisplayName(new ShadedAdventureComponentWrapper(TranslationManager.render(MessageConstants.GUI_PAGE_NBT_BACK_TO_COMPOUND.build())));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            currentSection = currentSection.getParent();
            currentNode = currentNode.lastIndexOf(".") == -1 ? "" : currentNode.substring(0, currentNode.lastIndexOf("."));
            reOpenMain();
        }
    }
}
