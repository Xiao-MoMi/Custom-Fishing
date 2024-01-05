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

package net.momirealms.customfishing.mechanic.hook;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.manager.HookManager;
import net.momirealms.customfishing.api.manager.RequirementManager;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.effect.EffectCarrier;
import net.momirealms.customfishing.api.mechanic.hook.HookSetting;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.mechanic.item.ItemManagerImpl;
import net.momirealms.customfishing.util.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class HookManagerImpl implements Listener, HookManager {

    private final CustomFishingPlugin plugin;
    private final HashMap<String, HookSetting> hookSettingMap;

    public HookManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.hookSettingMap = new HashMap<>();
    }

    public void load() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        loadConfig();
    }

    public void unload() {
        HandlerList.unregisterAll(this);
        hookSettingMap.clear();
    }

    public void disable() {
        unload();
    }

    /**
     * Loads configuration files for the specified types.
     */
    @SuppressWarnings("DuplicatedCode")
    private void loadConfig() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (String type : List.of("hook")) {
            File typeFolder = new File(plugin.getDataFolder() + File.separator + "contents" + File.separator + type);
            if (!typeFolder.exists()) {
                if (!typeFolder.mkdirs()) return;
                plugin.saveResource("contents" + File.separator + type + File.separator + "default.yml", false);
            }
            fileDeque.push(typeFolder);
            while (!fileDeque.isEmpty()) {
                File file = fileDeque.pop();
                File[] files = file.listFiles();
                if (files == null) continue;
                for (File subFile : files) {
                    if (subFile.isDirectory()) {
                        fileDeque.push(subFile);
                    } else if (subFile.isFile() && subFile.getName().endsWith(".yml")) {
                        this.loadSingleFile(subFile);
                    }
                }
            }
        }
    }

    /**
     * Loads data from a single configuration file.
     *
     * @param file The configuration file to load.
     */
    private void loadSingleFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map.Entry<String, Object> entry : config.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection section) {
                if (!section.contains("max-durability")) {
                    LogUtils.warn("Please set max-durability to hook: " + entry.getKey());
                    continue;
                }
                var setting = new HookSetting.Builder(entry.getKey())
                        .durability(section.getInt("max-durability", 16))
                        .lore(section.getStringList("lore-on-rod").stream().map(it -> "<!i>" + it).toList())
                        .build();
                hookSettingMap.put(entry.getKey(), setting);
            }
        }
    }

    /**
     * Get the hook setting by its ID.
     *
     * @param id The ID of the hook setting to retrieve.
     * @return The hook setting with the given ID, or null if not found.
     */
    @Nullable
    @Override
    public HookSetting getHookSetting(String id) {
        return hookSettingMap.get(id);
    }

    /**
     * Decreases the durability of a fishing hook by a specified amount and optionally updates its lore.
     *
     * @param rod         The fishing rod ItemStack to modify.
     * @param amount      The amount by which to decrease the durability.
     * @param updateLore  Whether to update the lore of the fishing rod.
     */
    @Override
    public void decreaseHookDurability(ItemStack rod, int amount, boolean updateLore) {
        ItemUtils.decreaseHookDurability(rod, amount, updateLore);
    }

    /**
     * Increases the durability of a fishing hook by a specified amount and optionally updates its lore.
     *
     * @param rod   The fishing rod ItemStack to modify.
     * @param amount      The amount by which to increase the durability.
     * @param updateLore  Whether to update the lore of the fishing rod.
     */
    @Override
    public void increaseHookDurability(ItemStack rod, int amount, boolean updateLore) {
        ItemUtils.increaseHookDurability(rod, amount, updateLore);
    }

    /**
     * Sets the durability of a fishing hook to a specific amount and optionally updates its lore.
     *
     * @param rod         The fishing rod ItemStack to modify.
     * @param amount      The new durability value to set.
     * @param updateLore  Whether to update the lore of the fishing rod.
     */
    @Override
    public void setHookDurability(ItemStack rod, int amount, boolean updateLore) {
        ItemUtils.setHookDurability(rod, amount, updateLore);
    }

    /**
     * Equips a fishing hook on a fishing rod.
     *
     * @param rod  The fishing rod ItemStack.
     * @param hook The fishing hook ItemStack.
     * @return True if the hook was successfully equipped, false otherwise.
     */
    @Override
    public boolean equipHookOnRod(ItemStack rod, ItemStack hook) {
        if (rod == null || hook == null || hook.getType() == Material.AIR || hook.getAmount() != 1)
            return false;
        if (rod.getType() != Material.FISHING_ROD)
            return false;

        String hookID = plugin.getItemManager().getAnyPluginItemID(hook);
        HookSetting setting = getHookSetting(hookID);
        if (setting == null)
            return false;

        var curDurability = ItemUtils.getCustomDurability(hook);
        if (curDurability.left() == 0)
            return false;

        NBTItem rodNBTItem = new NBTItem(rod);
        NBTCompound cfCompound = rodNBTItem.getOrCreateCompound("CustomFishing");

        cfCompound.setString("hook_id", hookID);
        cfCompound.setItemStack("hook_item", hook);
        cfCompound.setInteger("hook_dur", curDurability.right());

        ItemUtils.updateNBTItemLore(rodNBTItem);
        rod.setItemMeta(rodNBTItem.getItem().getItemMeta());
        return true;
    }

    /**
     * Removes the fishing hook from a fishing rod.
     *
     * @param rod The fishing rod ItemStack.
     * @return The removed fishing hook ItemStack, or null if no hook was found.
     */
    @Override
    public ItemStack removeHookFromRod(ItemStack rod) {
        if (rod == null || rod.getType() != Material.FISHING_ROD)
            return null;

        NBTItem rodNBTItem = new NBTItem(rod);
        NBTCompound cfCompound = rodNBTItem.getCompound("CustomFishing");
        if (cfCompound == null)
            return null;

        ItemStack hook = cfCompound.getItemStack("hook_item");
        if (hook != null) {
            cfCompound.removeKey("hook_item");
            cfCompound.removeKey("hook_id");
            cfCompound.removeKey("hook_dur");
            ItemUtils.updateNBTItemLore(rodNBTItem);
            rod.setItemMeta(rodNBTItem.getItem().getItemMeta());
        }

        return hook;
    }

    /**
     * Handles the event when a player clicks on a fishing rod in their inventory.
     *
     * @param event The InventoryClickEvent to handle.
     */
    @EventHandler
    @SuppressWarnings("deprecation")
    public void onDragDrop(InventoryClickEvent event) {
        if (event.isCancelled())
            return;
        final Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() != player.getInventory())
            return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.FISHING_ROD)
            return;
        if (player.getGameMode() != GameMode.SURVIVAL)
            return;
        if (plugin.getFishingManager().hasPlayerCastHook(player.getUniqueId()))
            return;

        ItemStack cursor = event.getCursor();
        if (cursor == null || cursor.getType() == Material.AIR) {
            if (event.getClick() == ClickType.RIGHT) {
                NBTItem nbtItem = new NBTItem(clicked);
                NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
                if (cfCompound == null)
                    return;
                if (cfCompound.hasTag("hook_id")) {
                    event.setCancelled(true);
                    ItemStack hook = cfCompound.getItemStack("hook_item");
                    ItemUtils.setDurability(hook, cfCompound.getInteger("hook_dur"), true);
                    cfCompound.removeKey("hook_id");
                    cfCompound.removeKey("hook_item");
                    cfCompound.removeKey("hook_dur");
                    event.setCursor(hook);
                    ItemUtils.updateNBTItemLore(nbtItem);
                    clicked.setItemMeta(nbtItem.getItem().getItemMeta());
                }
            }
            return;
        }

        String hookID = plugin.getItemManager().getAnyPluginItemID(cursor);
        HookSetting setting = getHookSetting(hookID);
        if (setting == null)
            return;

        var cursorDurability = ItemUtils.getCustomDurability(cursor);
        if (cursorDurability.left() == 0) {
            if (plugin.getItemManager().getBuildableItem("hook", hookID) instanceof ItemManagerImpl.CFBuilder cfBuilder) {
                ItemStack itemStack = cfBuilder.build(player, new HashMap<>());
                var pair = ItemUtils.getCustomDurability(itemStack);
                cursorDurability = pair;
                NBTItem nbtItem = new NBTItem(cursor);
                NBTCompound compound = nbtItem.getOrCreateCompound("CustomFishing");
                compound.setInteger("max_dur", pair.left());
                compound.setInteger("cur_dur", pair.right());
                compound.setString("type", "hook");
                compound.setString("id", hookID);
                cursor.setItemMeta(nbtItem.getItem().getItemMeta());
            } else {
                return;
            }
        }

        Condition condition = new Condition(player, new HashMap<>());
        condition.insertArg("{rod}", plugin.getItemManager().getAnyPluginItemID(clicked));
        EffectCarrier effectCarrier = plugin.getEffectManager().getEffectCarrier("hook", hookID);
        if (effectCarrier != null) {
            if (!RequirementManager.isRequirementMet(condition, effectCarrier.getRequirements())) {
                return;
            }
        }

        event.setCancelled(true);

        NBTItem rodNBTItem = new NBTItem(clicked);
        NBTCompound cfCompound = rodNBTItem.getOrCreateCompound("CustomFishing");
        String previousHookID = cfCompound.getString("hook_id");

        ItemStack clonedHook = cursor.clone();
        clonedHook.setAmount(1);
        cursor.setAmount(cursor.getAmount() - 1);

        if (previousHookID != null && !previousHookID.equals("")) {
            int previousHookDurability = cfCompound.getInteger("hook_dur");
            ItemStack previousItemStack = cfCompound.getItemStack("hook_item");
            ItemUtils.setDurability(previousItemStack, previousHookDurability, true);
            if (cursor.getAmount() == 0) {
                event.setCursor(previousItemStack);
            } else {
                ItemUtils.giveItem(player, previousItemStack, 1);
            }
        }

        cfCompound.setString("hook_id", hookID);
        cfCompound.setItemStack("hook_item", clonedHook);
        cfCompound.setInteger("hook_dur", cursorDurability.right());

        ItemUtils.updateNBTItemLore(rodNBTItem);
        clicked.setItemMeta(rodNBTItem.getItem().getItemMeta());
    }
}
