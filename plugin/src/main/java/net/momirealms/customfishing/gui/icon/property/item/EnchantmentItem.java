package net.momirealms.customfishing.gui.icon.property.item;

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.adventure.component.ShadedAdventureComponentWrapper;
import net.momirealms.customfishing.gui.ItemPage;
import net.momirealms.customfishing.gui.page.property.EnchantmentEditor;
import net.momirealms.customfishing.gui.page.property.SizeEditor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.Map;

public class EnchantmentItem extends AbstractItem {

    private final ItemPage itemPage;

    public EnchantmentItem(ItemPage itemPage) {
        this.itemPage = itemPage;
    }

    @Override
    public ItemProvider getItemProvider() {
        ItemBuilder itemBuilder = new ItemBuilder(Material.IRON_HOE)
                .setDisplayName(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                        "<#8A2BE2>● Enchantment"
                )))
                .addEnchantment(Enchantment.ARROW_FIRE,1,true)
                .addItemFlags(ItemFlag.HIDE_ENCHANTS);

        if (itemPage.getSection().contains("enchantments")) {
            itemBuilder.addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                            "<gray>Current value: "
                    )));

            for (Map.Entry<String, Object> entry : itemPage.getSection().getConfigurationSection("enchantments").getValues(false).entrySet()) {
                itemBuilder.addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                        " <gray>- <white>" + entry.getKey() + ":" + entry.getValue()
                )));
            }

            itemBuilder.addLoreLines("").addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                    "<#00FF7F> -> Left click to edit"
            ))).addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                    "<#FF6347> -> Right click to reset"
            )));
        } else {
            itemBuilder.addLoreLines(new ShadedAdventureComponentWrapper(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                    "<#00FF7F> -> Left click to set"
            )));
        }

        return itemBuilder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (clickType.isLeftClick()) {
            new EnchantmentEditor(player, itemPage, itemPage.getSection(), false);
        } else if (clickType.isRightClick()) {
            itemPage.getSection().set("enchantments", null);
            itemPage.save();
            itemPage.reOpen();
        }
    }
}
