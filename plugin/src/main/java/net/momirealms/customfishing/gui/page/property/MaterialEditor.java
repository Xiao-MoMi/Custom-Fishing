package net.momirealms.customfishing.gui.page.property;

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.adventure.component.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.gui.SectionPage;
import net.momirealms.customfishing.gui.icon.BackGroundItem;
import net.momirealms.customfishing.mechanic.item.ItemManagerImpl;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.AnvilWindow;

import java.util.ArrayList;
import java.util.List;

public class MaterialEditor {

    private final Player player;
    private final SectionPage parentPage;
    private String material;
    private final ConfigurationSection section;

    public MaterialEditor(Player player, SectionPage parentPage) {
        this.player = player;
        this.parentPage = parentPage;
        this.section = parentPage.getSection();
        this.material = section.getString("material");

        Item border = new SimpleItem(new ItemBuilder(Material.AIR));
        var confirm = new ConfirmIcon();
        var itemBuilder = new ItemBuilder(CustomFishingPlugin.get()
                .getItemManager()
                .getItemStackAppearance(player, material)
        )
        .setDisplayName(section.getString("material", ""));

        if (section.contains("custom-model-data"))
            itemBuilder.setCustomModelData(section.getInt("custom-model-data", 0));

        Gui upperGui = Gui.normal()
                .setStructure("a # b")
                .addIngredient('a', itemBuilder)
                .addIngredient('#', border)
                .addIngredient('b', confirm)
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
                .setContent(getCompatibilityItemList())
                .build();

        var window = AnvilWindow.split()
                .setViewer(player)
                .setTitle(new ShadedAdventureComponentWrapper(
                        AdventureManagerImpl.getInstance().getComponentFromMiniMessage("Edit Material")
                ))
                .addRenameHandler(s -> {
                    material = s;
                    confirm.notifyWindows();
                })
                .setUpperGui(upperGui)
                .setLowerGui(gui)
                .build();

        window.open();
    }

    public List<Item> getCompatibilityItemList() {
        ArrayList<Item> items = new ArrayList<>();
        for (String lib : ((ItemManagerImpl) CustomFishingPlugin.get().getItemManager()).getItemLibraries()) {
            switch (lib) {
                case "MMOItems" -> {
                    items.add(new SimpleItem(new ItemBuilder(Material.BELL).setDisplayName(lib + ":TYPE:ID")));
                }
                case "vanilla", "CustomFishing" -> {
                }
                default -> {
                    items.add(new SimpleItem(new ItemBuilder(Material.BELL).setDisplayName(lib + ":ID")));
                }
            }

        }
        return items;
    }

    public class ConfirmIcon extends AbstractItem {

        @Override
        public ItemProvider getItemProvider() {
            if (material == null || material.isEmpty()) {
                return new ItemBuilder(Material.STRUCTURE_VOID).setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                        "<#00CED1>● Delete property"
                )));
            } else {
                var builder = new ItemBuilder(
                        CustomFishingPlugin.get()
                                .getItemManager()
                                .getItemStackAppearance(player, material)
                ).setDisplayName("New value: " + material)
                        .addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                                "<#00FF7F> -> Click to confirm"
                        )));
                if (section.contains("custom-model-data"))
                    builder.setCustomModelData(section.getInt("custom-model-data"));

                return builder;
            }
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (material == null || material.isEmpty()) {
                section.set("material", null);
            } else if (CustomFishingPlugin.get().getItemManager().getItemStackAppearance(player, material).getType() == Material.BARRIER) {
                return;
            } else {
                section.set("material", material);
            }
            parentPage.reOpen();
            parentPage.save();
        }
    }
}
