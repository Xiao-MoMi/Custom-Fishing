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

package net.momirealms.customfishing.manager;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.api.event.SellFishEvent;
import net.momirealms.customfishing.integration.papi.PlaceholderManager;
import net.momirealms.customfishing.listener.InventoryListener;
import net.momirealms.customfishing.listener.WindowPacketListener;
import net.momirealms.customfishing.object.Function;
import net.momirealms.customfishing.object.loot.Item;
import net.momirealms.customfishing.util.AdventureUtil;
import net.momirealms.customfishing.util.ConfigUtil;
import net.momirealms.customfishing.util.ItemStackUtil;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SellManager extends Function {

    private final WindowPacketListener windowPacketListener;
    private final InventoryListener inventoryListener;
    public static String formula;
    public static String title;
    public static int guiSize;
    public static String msgNotification;
    public static String actionbarNotification;
    public static String titleNotification;
    public static String subtitleNotification;
    public static int titleIn;
    public static int titleStay;
    public static int titleOut;
    public static String[] commands;
    public static Item sellIcon;
    public static Item denyIcon;
    public static Key closeKey;
    public static Key openKey;
    public static Key successKey;
    public static Key denyKey;
    public static Sound.Source soundSource;
    public static HashMap<Integer, ItemStack> guiItems;
    public static HashSet<Integer> functionIconSlots;
    public static HashMap<Material, Float> vanillaPrices = new HashMap<>();
    public static boolean sellLimitation;
    public static int upperLimit;
    private final HashMap<Player, Inventory> inventoryCache;
    private HashMap<String, Double> todayEarning;
    private int date;

    public SellManager() {
        this.windowPacketListener = new WindowPacketListener(this);
        this.inventoryListener = new InventoryListener(this);
        this.inventoryCache = new HashMap<>();

    }

    @Override
    public void load() {
        loadConfig();
        CustomFishing.protocolManager.addPacketListener(windowPacketListener);
        Bukkit.getPluginManager().registerEvents(inventoryListener, CustomFishing.plugin);
        if (sellLimitation) {
            readLimitationCache();
        }
    }

    private void readLimitationCache() {
        this.todayEarning = new HashMap<>();
        YamlConfiguration data = ConfigUtil.readData(new File(CustomFishing.plugin.getDataFolder(), "sell-cache.yml"));
        Calendar calendar = Calendar.getInstance();
        date = calendar.get(Calendar.DATE);
        int lastDate = data.getInt("date");
        if (lastDate == date) {
            ConfigurationSection configurationSection = data.getConfigurationSection("player_data");
            if (configurationSection != null) {
                for (String player : configurationSection.getKeys(false)) {
                    todayEarning.put(player, configurationSection.getDouble(player));
                }
            }
        }
    }

    private void unloadLimitationCache() {
        YamlConfiguration data = new YamlConfiguration();
        data.set("date", date);
        for (Map.Entry<String, Double> entry : todayEarning.entrySet()) {
            data.set("player_data." + entry.getKey(), entry.getValue());
        }
        try {
            data.save(new File(CustomFishing.plugin.getDataFolder(), "sell-cache.yml"));
        }
        catch (IOException e) {
            AdventureUtil.consoleMessage("<red>[CustomFishing] Failed to unload earnings data!");
            e.printStackTrace();
        }
        this.todayEarning.clear();
    }

    @Override
    public void unload() {
        for (Player player : this.inventoryCache.keySet()) {
            player.closeInventory();
        }
        this.inventoryCache.clear();
        CustomFishing.protocolManager.removePacketListener(windowPacketListener);
        HandlerList.unregisterAll(inventoryListener);
        if (sellLimitation) unloadLimitationCache();
    }

    private void loadConfig() {
        functionIconSlots = new HashSet<>();
        guiItems = new HashMap<>();
        vanillaPrices = new HashMap<>();
        YamlConfiguration config = ConfigUtil.getConfig("sell-fish.yml");
        formula = config.getString("price-formula", "{base} + {bonus} * {size}");
        sellLimitation = config.getBoolean("sell-limitation.enable", false);
        upperLimit = config.getInt("sell-limitation.upper-limit", 10000);
        title = config.getString("container-title");
        guiSize = config.getInt("rows") * 9;
        openKey = config.contains("sounds.open") ? Key.key(config.getString("sounds.open")) : null;
        closeKey = config.contains("sounds.close") ? Key.key(config.getString("sounds.close")) : null;
        successKey = config.contains("sounds.success") ? Key.key(config.getString("sounds.success")) : null;
        denyKey = config.contains("sounds.deny") ? Key.key(config.getString("sounds.deny")) : null;
        soundSource = Sound.Source.valueOf(config.getString("sounds.type","player").toUpperCase());
        if (config.contains("decorative-icons")){
            config.getConfigurationSection("decorative-icons").getKeys(false).forEach(key -> {
                Item item = new Item(Material.valueOf(config.getString("decorative-icons." + key + ".material", "PAPER").toUpperCase()));
                if (config.contains("decorative-icons." + key + ".display.name")) item.setName(config.getString("decorative-icons." + key + ".display.name"));
                if (config.contains("decorative-icons." + key + ".display.lore")) item.setLore(config.getStringList("decorative-icons." + key + ".display.lore"));
                if (config.contains("decorative-icons." + key + ".custom-model-data")) item.setCustomModelData(config.getInt("decorative-icons." + key + ".custom-model-data"));
                ItemStack itemStack = ItemStackUtil.getFromItem(item);
                for (int slot : config.getIntegerList("decorative-icons." + key + ".slot")) {
                    guiItems.put(slot - 1, itemStack);
                }
            });
        }

        sellIcon = new Item(Material.valueOf(config.getString("functional-icons.sell.material", "PAPER").toUpperCase()));
        if (config.contains("functional-icons.sell.display.name")) sellIcon.setName(config.getString("functional-icons.sell.display.name"));
        if (config.contains("functional-icons.sell.display.lore")) sellIcon.setLore(config.getStringList("functional-icons.sell.display.lore"));
        if (config.contains("functional-icons.sell.custom-model-data")) sellIcon.setCustomModelData(config.getInt("functional-icons.sell.custom-model-data"));
        denyIcon = new Item(Material.valueOf(config.getString("functional-icons.deny.material", "PAPER").toUpperCase()));
        if (config.contains("functional-icons.deny.display.name")) denyIcon.setName(config.getString("functional-icons.deny.display.name"));
        if (config.contains("functional-icons.deny.display.lore")) denyIcon.setLore(config.getStringList("functional-icons.deny.display.lore"));
        if (config.contains("functional-icons.deny.custom-model-data")) denyIcon.setCustomModelData(config.getInt("functional-icons.deny.custom-model-data"));

        for (int slot : config.getIntegerList("functional-icons.slots")) {
            guiItems.put(slot - 1, ItemStackUtil.getFromItem(sellIcon));
            functionIconSlots.add(slot - 1);
        }

        if (config.getBoolean("actions.message.enable", false)) {
            msgNotification = config.getString("actions.message.text");
        } else msgNotification = null;
        if (config.getBoolean("actions.actionbar.enable", false)) {
            actionbarNotification = config.getString("actions.actionbar.text");
        } else actionbarNotification = null;
        if (config.getBoolean("actions.title.enable", false)) {
            titleNotification = config.getString("actions.title.title");
            subtitleNotification = config.getString("actions.title.subtitle");
            titleIn = config.getInt("actions.title.in");
            titleStay = config.getInt("actions.title.stay");
            titleOut = config.getInt("actions.title.out");
        } else actionbarNotification = null;
        if (config.getBoolean("actions.commands.enable")) {
            commands = config.getStringList("actions.commands.value").toArray(new String[0]);
        } else commands = null;

        ConfigurationSection configurationSection = config.getConfigurationSection("vanilla-item-price");
        if (configurationSection != null) {
            for (String vanilla : configurationSection.getKeys(false)) {
                vanillaPrices.put(Material.valueOf(vanilla.toUpperCase()), (float) configurationSection.getDouble(vanilla));
            }
        }
    }

    public void openGuiForPlayer(Player player) {
        player.closeInventory();
        Inventory inventory = Bukkit.createInventory(player, guiSize, "{CustomFishing_Sell}");
        for (Map.Entry<Integer, ItemStack> entry : guiItems.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue());
        }
        inventoryCache.put(player, inventory);
        player.openInventory(inventory);
        if (openKey != null) AdventureUtil.playerSound(player, soundSource, openKey, 1, 1);
    }

    @Override
    public void onOpenInventory(InventoryOpenEvent event) {
        final Player player = (Player) event.getPlayer();
        Inventory inventory = inventoryCache.get(player);
        if (inventory == null) return;
        if (inventory == event.getInventory()) {
            for (int slot : functionIconSlots) {
                inventory.setItem(slot, ItemStackUtil.getFromItem(sellIcon.cloneWithPrice(getTotalPrice(getPlayerItems(inventory)))));
            }
        }
    }

    @Override
    public void onClickInventory(InventoryClickEvent event) {
        final Player player = (Player) event.getView().getPlayer();
        Inventory inventory = inventoryCache.get(player);
        if (inventory == null) return;
        boolean update = true;
        if (inventory == event.getClickedInventory()) {
            int clickedSlot = event.getSlot();
            if (guiItems.containsKey(clickedSlot)) {
                event.setCancelled(true);
            }
            if (functionIconSlots.contains(clickedSlot)) {
                List<ItemStack> playerItems = getPlayerItems(inventory);
                float totalPrice = getTotalPrice(playerItems);
                if (totalPrice > 0) {

                    if (sellLimitation) {
                        Calendar calendar = Calendar.getInstance();
                        int currentDate = calendar.get(Calendar.DATE);
                        if (currentDate != date) {
                            date = currentDate;
                            todayEarning.clear();
                        }
                    }

                    double earnings = Optional.ofNullable(todayEarning.get(player.getName())).orElse(0d);
                    if (earnings + totalPrice > upperLimit) {
                        inventory.close();
                        AdventureUtil.playerMessage(player, MessageManager.prefix + MessageManager.reachSellLimit);
                        if (denyKey != null) AdventureUtil.playerSound(player, soundSource, denyKey, 1, 1);
                        return;
                    }

                    SellFishEvent sellFishEvent = new SellFishEvent(player, totalPrice);
                    Bukkit.getPluginManager().callEvent(sellFishEvent);
                    if (sellFishEvent.isCancelled()) {
                        return;
                    }

                    for (ItemStack playerItem : playerItems) {
                        if (playerItem == null || playerItem.getType() == Material.AIR) continue;
                        if (getSingleItemPrice(playerItem) == 0) continue;
                        playerItem.setAmount(0);
                    }

                    todayEarning.put(player.getName(), earnings + totalPrice);
                    doActions(player, sellFishEvent.getMoney(), upperLimit - earnings - totalPrice);
                    inventory.close();
                }
                else {
                    for (int slot : functionIconSlots) {
                        inventory.setItem(slot, ItemStackUtil.getFromItem(denyIcon));
                    }
                    update = false;
                    if (denyKey != null) AdventureUtil.playerSound(player, soundSource, denyKey, 1, 1);
                }
            }
        }
        if (update) {
            Bukkit.getScheduler().runTaskAsynchronously(CustomFishing.plugin, () -> {
                ItemStack icon = ItemStackUtil.getFromItem(sellIcon.cloneWithPrice(getTotalPrice(getPlayerItems(inventory))));
                for (int slot : functionIconSlots) {
                    inventory.setItem(slot, icon);
                }
            });
        }
    }

    @Override
    public void onCloseInventory(InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();
        Inventory inventory = inventoryCache.remove(player);
        if (inventory == null) return;
        if (event.getInventory() == inventory) {
            returnItems(getPlayerItems(event.getInventory()), player);
            if (closeKey != null) AdventureUtil.playerSound(player, soundSource, closeKey, 1, 1);
        }
    }

    private List<ItemStack> getPlayerItems(Inventory inventory) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < guiSize; i++) {
            if (guiItems.containsKey(i)) continue;
            items.add(inventory.getItem(i));
        }
        return items;
    }

    private void returnItems(List<ItemStack> itemStacks, Player player){
        Inventory inventory = player.getInventory();
        for (ItemStack stack : itemStacks) {
            if (stack == null || stack.getType() == Material.AIR) continue;
            inventory.addItem(stack);
        }
    }

    private float getTotalPrice(List<ItemStack> itemStacks){
        float totalPrice = 0;
        for (ItemStack stack : itemStacks) {
            if (stack == null || stack.getType() == Material.AIR) continue;
            float price = getSingleItemPrice(stack);
            price *= stack.getAmount();
            totalPrice += price;
        }
        return totalPrice;
    }

    private float getSingleItemPrice(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound fishMeta = nbtItem.getCompound("FishMeta");
        float price = 0;
        if (fishMeta != null) {
            float base = fishMeta.getFloat("base");
            float bonus = fishMeta.getFloat("bonus");
            float size = fishMeta.getFloat("size");
            Expression expression = new ExpressionBuilder(formula)
                    .variables("base", "bonus","size")
                    .build()
                    .setVariable("base", base)
                    .setVariable("bonus", bonus)
                    .setVariable("size", size);
            price = (float) expression.evaluate();
        }
        if (price == 0) {
            price = Optional.ofNullable(vanillaPrices.get(itemStack.getType())).orElse(0f);
        }
        return price;
    }

    private void doActions(Player player, float earnings, double remains) {
        if (titleNotification != null) AdventureUtil.playerTitle(
                player,
                titleNotification.replace("{money}", String.format("%.2f", earnings)).replace("{remains}", String.format("%.2f", remains)),
                subtitleNotification.replace("{money}", String.format("%.2f", earnings)).replace("{remains}", String.format("%.2f", remains)),
                titleIn * 50,
                titleStay * 50,
                titleOut * 50
        );
        if (msgNotification != null) {
            AdventureUtil.playerMessage(player, msgNotification.replace("{money}", String.format("%.2f", earnings)).replace("{remains}", String.format("%.2f", remains)));
        }
        if (actionbarNotification != null) {
            AdventureUtil.playerActionbar(player, actionbarNotification.replace("{money}", String.format("%.2f", earnings)).replace("{remains}", String.format("%.2f", remains)));
        }
        if (commands != null) {
            for (String cmd : commands) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", player.getName()).replace("{money}", String.format("%.2f", earnings)).replace("{remains}", String.format("%.2f", remains)));
            }
        }
        if (ConfigManager.logEarning) {
            AdventureUtil.consoleMessage("[CustomFishing] Log: " + player.getName() + " earns " + String.format("%.2f", earnings) + " from selling fish");
        }
        if (successKey != null) AdventureUtil.playerSound(player, soundSource, successKey, 1, 1);
        if (ConfigManager.vaultHook) {
            assert CustomFishing.plugin.getIntegrationManager().getVaultHook() != null;
            CustomFishing.plugin.getIntegrationManager().getVaultHook().getEconomy().depositPlayer(player, earnings);
        }
    }

    @Override
    public void onWindowTitlePacketSend(PacketContainer packet, Player player) {
        StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
        WrappedChatComponent component = wrappedChatComponentStructureModifier.getValues().get(0);
        if (component.getJson().equals("{\"text\":\"{CustomFishing_Sell}\"}")) {
            PlaceholderManager placeholderManager = CustomFishing.plugin.getIntegrationManager().getPlaceholderManager();
            String text = SellManager.title.replace("{player}", player.getName());
            if (placeholderManager != null) placeholderManager.parse(player, text);
            wrappedChatComponentStructureModifier.write(0,
                    WrappedChatComponent.fromJson(
                            GsonComponentSerializer.gson().serialize(
                                    MiniMessage.miniMessage().deserialize(
                                            ItemStackUtil.replaceLegacy(text)
                                    )
                            )
                    )
            );
        }
    }
}
