package net.momirealms.customfishing.gui.page.file;

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.adventure.component.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.gui.Icon;
import net.momirealms.customfishing.gui.icon.BackGroundItem;
import net.momirealms.customfishing.gui.icon.BackToFolderItem;
import net.momirealms.customfishing.gui.icon.ScrollDownItem;
import net.momirealms.customfishing.gui.icon.ScrollUpItem;
import net.momirealms.customfishing.gui.page.item.ItemSelector;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.animation.impl.SequentialAnimation;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.ScrollGui;
import xyz.xenondevs.invui.gui.SlotElement;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.window.Window;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

public class FileSelector {

    public FileSelector(Player player, File folder) {
        File[] files = folder.listFiles();
        Deque<Item> items = new ArrayDeque<>();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".yml")) {
                    items.addLast(new FileItem(file));
                } else if (file.isDirectory()) {
                    items.addFirst(new FolderItem(file));
                }
            }
        }

        Gui gui = ScrollGui.items()
                .setStructure(
                        "x x x x x x x x u",
                        "x x x x x x x x #",
                        "x x x x x x x x b",
                        "x x x x x x x x #",
                        "x x x x x x x x d"
                )
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('#', new BackGroundItem())
                .addIngredient('u', new ScrollUpItem())
                .addIngredient('d', new ScrollDownItem())
                .addIngredient('b', new BackToFolderItem(folder.getParentFile()))
                .setContent(items.stream().toList())
                .build();

        Window window = Window.single()
                .setViewer(player)
                .setTitle(new ShadedAdventureComponentWrapper(
                        AdventureManagerImpl.getInstance().getComponentFromMiniMessage("Select file")
                ))
                .setGui(gui)
                .build();

        gui.playAnimation(new SequentialAnimation(1, true), slotElement -> {
            if (slotElement instanceof SlotElement.ItemSlotElement itemSlotElement) {
                return !(itemSlotElement.getItem() instanceof Icon);
            }
            return true;
        });

        window.open();
    }

    public static class FileItem extends AbstractItem {

        private final File file;

        public FileItem(File file) {
            this.file = file;
        }

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.PAPER).setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                    "<#FDF5E6>" + file.getName()
            )));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            String path = file.getPath();
            String[] split = path.split("\\\\");
            String type = split[3];
            switch (type) {
                case "item" -> {
                    new ItemSelector(player, file);
                }
            }
        }
    }

    public static class FolderItem extends AbstractItem {

        private final File file;

        public FolderItem(File file) {
            this.file = file;
        }

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Material.BOOK).setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                    "<#D2B48C><b>" + file.getName()
            )));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            new FileSelector(player, file);
        }
    }
}
