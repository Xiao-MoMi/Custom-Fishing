package net.momirealms.customfishing.manager;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.data.PlayerBagData;
import net.momirealms.customfishing.data.storage.DataStorageInterface;
import net.momirealms.customfishing.data.storage.FileStorageImpl;
import net.momirealms.customfishing.listener.InventoryListener;
import net.momirealms.customfishing.listener.SimpleListener;
import net.momirealms.customfishing.listener.WindowPacketListener;
import net.momirealms.customfishing.object.Function;
import net.momirealms.customfishing.util.AdventureUtil;
import net.momirealms.customfishing.util.ItemStackUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BagDataManager extends Function {

    public static HashMap<UUID, PlayerBagData> dataCache;
    public static HashSet<PlayerBagData> tempCache;
    private final DataStorageInterface dataStorageInterface;
    private final InventoryListener inventoryListener;
    private final WindowPacketListener windowPacketListener;
    private final SimpleListener simpleListener;

    public BagDataManager() {
        dataCache = new HashMap<>();
        tempCache = new HashSet<>();
        this.dataStorageInterface = new FileStorageImpl();
        this.inventoryListener = new InventoryListener(this);
        this.windowPacketListener = new WindowPacketListener(this);
        this.simpleListener = new SimpleListener(this);
    }

    @Override
    public void load() {
        if (!ConfigManager.enableFishingBag) return;
        Bukkit.getPluginManager().registerEvents(inventoryListener, CustomFishing.plugin);
        Bukkit.getPluginManager().registerEvents(simpleListener, CustomFishing.plugin);
        CustomFishing.protocolManager.addPacketListener(windowPacketListener);
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(inventoryListener);
        HandlerList.unregisterAll(simpleListener);
        CustomFishing.protocolManager.removePacketListener(windowPacketListener);
        for (PlayerBagData playerBagData : dataCache.values()) {
            dataStorageInterface.save(playerBagData);
        }
    }

    public void openFishingBag(Player viewer, OfflinePlayer ownerOffline) {
        Player owner = ownerOffline.getPlayer();
        if (owner == null) {
            Inventory inventory = dataStorageInterface.load(ownerOffline);
            PlayerBagData playerBagData = new PlayerBagData(ownerOffline, inventory);
            tempCache.add(playerBagData);
            viewer.openInventory(inventory);
        }
        else {
            PlayerBagData playerBagData = dataCache.get(owner.getUniqueId());
            if (playerBagData == null) {
                AdventureUtil.consoleMessage("<red>[CustomFishing] Unexpected data for " + owner.getName());
                tryOpen(owner, viewer, readData(owner));
            }
            else {
                tryOpen(owner, viewer, playerBagData);
            }
        }
    }

    @Override
    public void onQuit(Player player) {
        PlayerBagData playerBagData = dataCache.remove(player.getUniqueId());
        if (playerBagData != null) {
            dataStorageInterface.save(playerBagData);
        }
    }

    @Override
    public void onJoin(Player player) {
        readData(player);
    }

    public PlayerBagData readData(Player player) {
        Inventory inventory = dataStorageInterface.load(player);
        PlayerBagData playerBagData = new PlayerBagData(player, inventory);
        dataCache.put(player.getUniqueId(), playerBagData);
        return playerBagData;
    }

    @Override
    public void onWindowTitlePacketSend(PacketContainer packet, Player receiver) {
        StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
        WrappedChatComponent component = wrappedChatComponentStructureModifier.getValues().get(0);
        String windowTitleJson = component.getJson();
        if (windowTitleJson.startsWith("{\"text\":\"{CustomFishing_Bag_")) {
            String player = windowTitleJson.substring(28, windowTitleJson.length() - 3);
            String text = ConfigManager.fishingBagTitle.replace("{player}", player);
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

    @Override
    public void onClickInventory(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        PlayerBagData playerBagData = dataCache.get(player.getUniqueId());
        if (playerBagData == null) return;
        if (playerBagData.getInventory() == event.getInventory()) {
            ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null || currentItem.getType() == Material.AIR) return;
            NBTItem nbtItem = new NBTItem(currentItem);
            if (!nbtItem.hasKey("CustomFishing") && !ConfigManager.bagWhiteListItems.contains(currentItem.getType())) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onCloseInventory(InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        PlayerBagData playerBagData = dataCache.get(player.getUniqueId());
        if (playerBagData != null) {
            if (inventory == playerBagData.getInventory()) {
                for (ItemStack itemStack : event.getInventory().getContents()) {
                    if (itemStack == null || itemStack.getType() == Material.AIR) continue;
                    NBTItem nbtItem = new NBTItem(itemStack);
                    if (nbtItem.hasKey("CustomFishing") || ConfigManager.bagWhiteListItems.contains(itemStack.getType())) continue;
                    player.getInventory().addItem(itemStack.clone());
                    itemStack.setAmount(0);
                }
                return;
            }
            for (PlayerBagData temp : tempCache) {
                if (temp.getInventory() == inventory) {
                    tempCache.remove(temp);
                    dataStorageInterface.save(temp);
                }
            }
        }
    }

    public void tryOpen(Player owner, Player viewer, PlayerBagData playerBagData) {
        Inventory inventory = playerBagData.getInventory();
        int size = 1;
        for (int i = 6; i > 1; i--) {
            if (owner.hasPermission("fishingbag.rows." + i)) {
                size = i;
                break;
            }
        }
        if (size * 9 != inventory.getSize()) {
            ItemStack[] itemStacks = playerBagData.getInventory().getContents();
            Inventory newInv = Bukkit.createInventory(null, size * 9, "{CustomFishing_Bag_" + owner.getName() + "}");
            newInv.setContents(itemStacks);
            playerBagData.setInventory(newInv);
            viewer.openInventory(newInv);
        }
        else {
            viewer.openInventory(inventory);
        }
    }
}