package net.momirealms.customfishing.mechanic.market;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.manager.MarketManager;
import net.momirealms.customfishing.api.mechanic.item.ItemBuilder;
import net.momirealms.customfishing.api.mechanic.market.MarketGUI;
import net.momirealms.customfishing.libraries.inventorygui.InventoryGui;
import net.momirealms.customfishing.libraries.inventorygui.StaticGuiElement;
import net.momirealms.customfishing.mechanic.item.ItemManagerImpl;
import net.momirealms.customfishing.util.ConfigUtils;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MarketManagerImpl implements MarketManager {

    private final CustomFishingPlugin plugin;
    private final HashMap<String, Double> priceMap;
    private String[] layout;
    private String title;
    private String formula;
    private final HashMap<Character, ItemBuilder> decorativeIcons;
    private char itemSlot;

    public MarketManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.priceMap = new HashMap<>();
        this.decorativeIcons = new HashMap<>();
    }

    public void load() {
        YamlConfiguration config = plugin.getConfig("market.yml");
        this.layout = config.getStringList("layout").toArray(new String[0]);
        this.title = config.getString("title", "market.title");
        this.formula = config.getString("price-formula", "{base} + {bonus} * {size}");
        ConfigurationSection priceSection = config.getConfigurationSection("item-price");
        if (priceSection != null) {
            for (Map.Entry<String, Object> entry : priceSection.getValues(false).entrySet()) {
                this.priceMap.put(entry.getKey(), ConfigUtils.getDoubleValue(entry.getValue()));
            }
        }
        ConfigurationSection decorativeSection = config.getConfigurationSection("decorative-icons");
        if (decorativeSection != null) {
            for (Map.Entry<String, Object> entry : decorativeSection.getValues(false).entrySet()) {
                if (entry.getValue() instanceof ConfigurationSection innerSection) {
                    char symbol = Objects.requireNonNull(innerSection.getString("symbol")).charAt(0);
                    var builder = plugin.getItemManager().getItemBuilder(innerSection, "gui", entry.getKey());
                    decorativeIcons.put(symbol, builder);
                }
            }
        }
    }

    public void unload() {
        this.priceMap.clear();
        this.decorativeIcons.clear();
    }

    public void disable() {
        unload();
    }

    public void openMarketGUI(Player player) {
        player.closeInventory();

        InventoryGui gui = new InventoryGui(
                plugin,
                new MarketGUI(),
                AdventureManagerImpl.getInstance().getComponentFromMiniMessage(title),
                layout
        );

        gui.setCloseAction(close -> {
            var elements = gui.getElement(itemSlot);


            return false;
        });

        for (Map.Entry<Character, ItemBuilder> entry : decorativeIcons.entrySet()) {
            gui.addElement(new StaticGuiElement(
                entry.getKey(),
                ((ItemManagerImpl.CFBuilder) entry.getValue()).build()
            ));
        }
    }

    @Override
    public int getDate() {
        Calendar calendar = Calendar.getInstance();
        return (calendar.get(Calendar.MONTH) +1) * 100 + calendar.get(Calendar.DATE);
    }

    @Override
    public double getItemPrice(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return 0;
        NBTItem nbtItem = new NBTItem(itemStack);
        Double price = nbtItem.getDouble("Price");
        if (price != null && price != 0) {
            return price;
        }
        String itemID = itemStack.getType().name();
        if (nbtItem.hasTag("CustomModelData")) {
            itemID = itemID + ":" + nbtItem.getInteger("CustomModelData");
        }
        return priceMap.getOrDefault(itemID, 0d);
    }

    @Override
    public String getFormula() {
        return formula;
    }

    @Override
    public double getPrice(float base, float bonus, float size) {
        Expression expression = new ExpressionBuilder(getFormula())
                .variables("base", "bonus", "size")
                .build()
                .setVariable("base", base)
                .setVariable("bonus", bonus)
                .setVariable("size", size);
        return expression.evaluate();
    }
}
