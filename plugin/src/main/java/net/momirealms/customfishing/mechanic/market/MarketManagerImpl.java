package net.momirealms.customfishing.mechanic.market;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.user.OnlineUser;
import net.momirealms.customfishing.api.manager.MarketManager;
import net.momirealms.customfishing.api.mechanic.item.BuildableItem;
import net.momirealms.customfishing.api.mechanic.item.ItemBuilder;
import net.momirealms.customfishing.api.mechanic.market.MarketGUIHolder;
import net.momirealms.customfishing.util.ConfigUtils;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MarketManagerImpl implements MarketManager, Listener {

    private final CustomFishingPlugin plugin;
    private final HashMap<String, Double> priceMap;
    private String[] layout;
    private String title;
    private String formula;
    private final HashMap<Character, ItemBuilder> decorativeIcons;
    private char itemSlot;
    private char functionSlot;
    private BuildableItem functionIconAllowBuilder;
    private BuildableItem functionIconDenyBuilder;
    private double earningLimit;
    private ConcurrentHashMap<UUID, MarketGUI> marketGUIMap;

    public MarketManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.priceMap = new HashMap<>();
        this.decorativeIcons = new HashMap<>();
        this.marketGUIMap = new ConcurrentHashMap<>();
    }

    public void load() {
        this.loadConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }


    public void unload() {
        HandlerList.unregisterAll(this);
        this.priceMap.clear();
        this.decorativeIcons.clear();
    }

    public void disable() {
        unload();
    }

    private void loadConfig() {
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

    public void openMarketGUI(Player player) {
        MarketGUI gui = new MarketGUI(this, player);

    }

    @EventHandler
    public void onClickInv(InventoryClickEvent event) {
        if (event.isCancelled())
            return;
        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null)
            return;
        HumanEntity human = event.getWhoClicked();
        if (!(clickedInv.getHolder() instanceof MarketGUIHolder holder))
            return;

        MarketGUI gui = marketGUIMap.get(human.getUniqueId());
        if (gui == null) {
            event.setCancelled(true);
            human.closeInventory();
            return;
        }

        int slot = event.getSlot();
        MarketGUIElement element = gui.getElement(slot);
        if (element == null) {
            event.setCancelled(true);
            return;
        }

        if (element.getSymbol() == itemSlot) {
            plugin.getScheduler().runTaskSyncLater(gui::refresh, human.getLocation(), 50, TimeUnit.MILLISECONDS);
            return;
        }

        if (element.getSymbol() == functionSlot) {
            event.setCancelled(true);
            double worth = gui.getTotalWorth();
            if (worth > 0) {
                double remainingToEarn = getRemainingMoneyToEarn(human.getUniqueId());
                if (remainingToEarn < worth) {

                } else {
                    gui.clearWorthyItems();
                    this.setRemainMoneyToEarn(human.getUniqueId(), remainingToEarn + worth);
                }
            }
            plugin.getScheduler().runTaskSyncLater(gui::refresh, human.getLocation(), 50, TimeUnit.MILLISECONDS);
            return;
        }

        event.setCancelled(true);
    }

    public double getRemainingMoneyToEarn(UUID uuid) {
        OnlineUser user = plugin.getStorageManager().getOnlineUser(uuid);
        if (user == null) {
            return -1;
        }
        return earningLimit - user.getEarningData().earnings;
    }

    public void setRemainMoneyToEarn(UUID uuid, double remaining) {
        OnlineUser user = plugin.getStorageManager().getOnlineUser(uuid);
        if (user == null) {
            return;
        }
        user.getEarningData().earnings = remaining;
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

    public char getItemSlot() {
        return itemSlot;
    }

    public char getFunctionSlot() {
        return functionSlot;
    }

    public String[] getLayout() {
        return layout;
    }

    public String getTitle() {
        return title;
    }

    public BuildableItem getFunctionIconAllowBuilder() {
        return functionIconAllowBuilder;
    }

    public BuildableItem getFunctionIconDenyBuilder() {
        return functionIconDenyBuilder;
    }
}
