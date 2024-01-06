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

package net.momirealms.customfishing.util;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ScoreComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.mechanic.hook.HookSetting;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.setting.CFConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Utility class for various item-related operations.
 */
public class ItemUtils {

    private ItemUtils() {}

    /**
     * Updates the lore of an NBTItem based on its custom NBT tags.
     *
     * @param nbtItem The NBTItem to update
     * @return The updated NBTItem
     */
    public static NBTItem updateNBTItemLore(NBTItem nbtItem) {
        NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
        if (cfCompound == null)
            return nbtItem;

        NBTCompound displayCompound = nbtItem.getOrCreateCompound("display");
        NBTList<String> lore = displayCompound.getStringList("Lore");
        lore.removeIf(it -> GsonComponentSerializer.gson().deserialize(it) instanceof ScoreComponent scoreComponent && scoreComponent.name().equals("cf"));

        if (cfCompound.hasTag("hook_id")) {
            String hookID = cfCompound.getString("hook_id");
            HookSetting setting = CustomFishingPlugin.get().getHookManager().getHookSetting(hookID);
            if (setting == null) {
                cfCompound.removeKey("hook_id");
                cfCompound.removeKey("hook_item");
                cfCompound.removeKey("hook_dur");
            } else {
                for (String newLore : setting.getLore()) {
                    ScoreComponent.Builder builder = Component.score().name("cf").objective("hook");
                    builder.append(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                            newLore.replace("{dur}", String.valueOf(cfCompound.getInteger("hook_dur")))
                                    .replace("{max}", String.valueOf(setting.getMaxDurability()))
                    ));
                    lore.add(GsonComponentSerializer.gson().serialize(builder.build()));
                }
            }
        }

        if (cfCompound.hasTag("max_dur")) {
            int max = cfCompound.getInteger("max_dur");
            int current = cfCompound.getInteger("cur_dur");
            for (String newLore : CFConfig.durabilityLore) {
                ScoreComponent.Builder builder = Component.score().name("cf").objective("durability");
                builder.append(AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                        newLore.replace("{dur}", String.valueOf(current))
                                .replace("{max}", String.valueOf(max))
                ));
                lore.add(GsonComponentSerializer.gson().serialize(builder.build()));
            }
        }
        return nbtItem;
    }

    /**
     * Updates the lore of an ItemStack based on its custom NBT tags.
     *
     * @param itemStack The ItemStack to update
     */
    public static void updateItemLore(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return;
        NBTItem nbtItem = updateNBTItemLore(new NBTItem(itemStack));
        itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
    }

    /**
     * Reduces the durability of a fishing hook item.
     *
     * @param rod  The fishing rod ItemStack
     * @param updateLore Whether to update the lore after reducing durability
     */
    public static void decreaseHookDurability(ItemStack rod, int amount, boolean updateLore) {
        if (rod == null || rod.getType() != Material.FISHING_ROD)
            return;
        NBTItem nbtItem = new NBTItem(rod);
        NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
        if (cfCompound != null && cfCompound.hasTag("hook_dur")) {
            int hookDur = cfCompound.getInteger("hook_dur");
            if (hookDur != -1) {
                hookDur = Math.max(0, hookDur - amount);
                if (hookDur > 0) {
                    cfCompound.setInteger("hook_dur", hookDur);
                } else {
                    cfCompound.removeKey("hook_id");
                    cfCompound.removeKey("hook_dur");
                    cfCompound.removeKey("hook_item");
                }
            }
        }
        if (updateLore) updateNBTItemLore(nbtItem);
        rod.setItemMeta(nbtItem.getItem().getItemMeta());
    }

    /**
     * Increases the durability of a fishing hook by a specified amount and optionally updates its lore.
     *
     * @param rod   The fishing rod ItemStack to modify.
     * @param amount      The amount by which to increase the durability.
     * @param updateLore  Whether to update the lore of the fishing rod.
     */
    public static void increaseHookDurability(ItemStack rod, int amount, boolean updateLore) {
        if (rod == null || rod.getType() != Material.FISHING_ROD)
            return;
        NBTItem nbtItem = new NBTItem(rod);
        NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
        if (cfCompound != null && cfCompound.hasTag("hook_dur")) {
            int hookDur = cfCompound.getInteger("hook_dur");
            if (hookDur != -1) {
                String id = cfCompound.getString("hook_id");
                HookSetting setting = CustomFishingPlugin.get().getHookManager().getHookSetting(id);
                if (setting == null) {
                    cfCompound.removeKey("hook_id");
                    cfCompound.removeKey("hook_dur");
                    cfCompound.removeKey("hook_item");
                } else {
                    hookDur = Math.min(setting.getMaxDurability(), hookDur + amount);
                    cfCompound.setInteger("hook_dur", hookDur);
                }
            }
        }
        if (updateLore) updateNBTItemLore(nbtItem);
        rod.setItemMeta(nbtItem.getItem().getItemMeta());
    }

    /**
     * Sets the durability of a fishing hook to a specific amount and optionally updates its lore.
     *
     * @param rod         The fishing rod ItemStack to modify.
     * @param amount      The new durability value to set.
     * @param updateLore  Whether to update the lore of the fishing rod.
     */
    public static void setHookDurability(ItemStack rod, int amount, boolean updateLore) {
        if (rod == null || rod.getType() != Material.FISHING_ROD)
            return;
        NBTItem nbtItem = new NBTItem(rod);
        NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
        if (cfCompound != null && cfCompound.hasTag("hook_dur")) {
            int hookDur = cfCompound.getInteger("hook_dur");
            if (hookDur != -1) {
                String id = cfCompound.getString("hook_id");
                HookSetting setting = CustomFishingPlugin.get().getHookManager().getHookSetting(id);
                if (setting == null) {
                    cfCompound.removeKey("hook_id");
                    cfCompound.removeKey("hook_dur");
                    cfCompound.removeKey("hook_item");
                } else {
                    hookDur = Math.min(setting.getMaxDurability(), amount);
                    cfCompound.setInteger("hook_dur", hookDur);
                }
            }
        }
        if (updateLore) updateNBTItemLore(nbtItem);
        rod.setItemMeta(nbtItem.getItem().getItemMeta());
    }

    /**
     * Decreases the durability of an item and updates its lore.
     *
     * @param itemStack  The ItemStack to reduce durability for
     * @param amount     The amount by which to reduce durability
     * @param updateLore Whether to update the lore after reducing durability
     */
    public static void decreaseDurability(Player player, ItemStack itemStack, int amount, boolean updateLore) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return;
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
        if (cfCompound != null && cfCompound.hasTag("max_dur")) {
            int unBreakingLevel = itemStack.getEnchantmentLevel(Enchantment.DURABILITY);
            if (Math.random() > (double) 1 / (unBreakingLevel + 1)) {
                return;
            }
            if (nbtItem.getByte("Unbreakable") == 1) {
                return;
            }
            int max = cfCompound.getInteger("max_dur");
            int current = cfCompound.getInteger("cur_dur") - amount;
            cfCompound.setInteger("cur_dur", current);
            int damage = (int) (itemStack.getType().getMaxDurability() * (1 - ((double) current / max)));
            nbtItem.setInteger("Damage", damage);
            if (current > 0) {
                if (updateLore) updateNBTItemLore(nbtItem);
                itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
            } else {
                itemStack.setAmount(0);
            }
        } else {
            ItemMeta previousMeta = itemStack.getItemMeta().clone();
            PlayerItemDamageEvent itemDamageEvent = new PlayerItemDamageEvent(player, itemStack, amount, amount);
            Bukkit.getPluginManager().callEvent(itemDamageEvent);
            if (!itemStack.getItemMeta().equals(previousMeta) || itemDamageEvent.isCancelled()) {
                return;
            }
            int unBreakingLevel = itemStack.getEnchantmentLevel(Enchantment.DURABILITY);
            if (Math.random() > (double) 1 / (unBreakingLevel + 1)) {
                return;
            }
            if (nbtItem.getByte("Unbreakable") == 1) {
                return;
            }
            int damage = nbtItem.getInteger("Damage") + amount;
            if (damage > itemStack.getType().getMaxDurability()) {
                itemStack.setAmount(0);
            } else {
                nbtItem.setInteger("Damage", damage);
                if (updateLore) updateNBTItemLore(nbtItem);
                itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
            }
        }
    }

    /**
     * Increases the durability of an item and updates its lore.
     *
     * @param itemStack  The ItemStack to increase durability for
     * @param amount     The amount by which to increase durability
     * @param updateLore Whether to update the lore after increasing durability
     */
    public static void increaseDurability(ItemStack itemStack, int amount, boolean updateLore) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return;
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.getByte("Unbreakable") == 1) {
            return;
        }
        NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
        if (cfCompound != null && cfCompound.hasTag("max_dur")) {
            int max = cfCompound.getInteger("max_dur");
            int current = Math.min(max, cfCompound.getInteger("cur_dur") + amount);
            cfCompound.setInteger("cur_dur", current);
            int damage = (int) (itemStack.getType().getMaxDurability() * (1 - ((double) current / max)));
            nbtItem.setInteger("Damage", damage);
            if (updateLore) updateNBTItemLore(nbtItem);
        } else {
            int damage = Math.max(nbtItem.getInteger("Damage") - amount, 0);
            nbtItem.setInteger("Damage", damage);
        }
        itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
    }

    /**
     * Sets the durability of an item and updates its lore.
     *
     * @param itemStack  The ItemStack to set durability for
     * @param amount     The new durability value
     * @param updateLore Whether to update the lore after setting durability
     */
    public static void setDurability(ItemStack itemStack, int amount, boolean updateLore) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return;
        if (amount <= 0) {
            itemStack.setAmount(0);
            return;
        }
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.getByte("Unbreakable") == 1) {
            return;
        }
        NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
        if (cfCompound != null && cfCompound.hasTag("max_dur")) {
            int max = cfCompound.getInteger("max_dur");
            amount = Math.min(amount, max);
            cfCompound.setInteger("cur_dur", amount);
            int damage = (int) (itemStack.getType().getMaxDurability() * (1 - ((double) amount / max)));
            nbtItem.setInteger("Damage", damage);
            if (updateLore) updateNBTItemLore(nbtItem);
        } else {
            nbtItem.setInteger("Damage", itemStack.getType().getMaxDurability() - amount);
        }
        itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
    }

    /**
     * Retrieves the current durability of an item.
     *
     * @param itemStack The ItemStack to get durability from
     * @return The current durability value
     */
    public static Pair<Integer, Integer> getCustomDurability(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return Pair.of(0, 0);
        if (itemStack.getItemMeta() instanceof Damageable damageable && damageable.isUnbreakable())
            return Pair.of(-1, -1);
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
        if (cfCompound != null && cfCompound.hasTag("max_dur")) {
            return Pair.of(cfCompound.getInteger("max_dur"), cfCompound.getInteger("cur_dur"));
        } else {
            return Pair.of(0, 0);
        }
    }

    /**
     * Gives a certain amount of an item to a player, handling stacking and item drops.
     *
     * @param player     The player to give the item to
     * @param itemStack  The ItemStack to give
     * @param amount     The amount of items to give
     * @return The actual amount of items given
     */
    public static int giveItem(Player player, ItemStack itemStack, int amount) {
        PlayerInventory inventory = player.getInventory();
        ItemMeta meta = itemStack.getItemMeta();
        int maxStackSize = itemStack.getMaxStackSize();

        if (amount > maxStackSize * 100) {
            LogUtils.warn("Detected too many items spawning. Lowering the amount to " + (maxStackSize * 100));
            amount = maxStackSize * 100;
        }

        int actualAmount = amount;

        for (ItemStack other : inventory.getStorageContents()) {
            if (other != null) {
                if (other.getType() == itemStack.getType() && other.getItemMeta().equals(meta)) {
                    if (other.getAmount() < maxStackSize) {
                        int delta = maxStackSize - other.getAmount();
                        if (amount > delta) {
                            other.setAmount(maxStackSize);
                            amount -= delta;
                        } else {
                            other.setAmount(amount + other.getAmount());
                            return actualAmount;
                        }
                    }
                }
            }
        }

        if (amount > 0) {
            for (ItemStack other : inventory.getStorageContents()) {
                if (other == null) {
                    if (amount > maxStackSize) {
                        amount -= maxStackSize;
                        ItemStack cloned = itemStack.clone();
                        cloned.setAmount(maxStackSize);
                        inventory.addItem(cloned);
                    } else {
                        ItemStack cloned = itemStack.clone();
                        cloned.setAmount(amount);
                        inventory.addItem(cloned);
                        return actualAmount;
                    }
                }
            }
        }

        if (amount > 0) {
            for (int i = 0; i < amount / maxStackSize; i++) {
                ItemStack cloned = itemStack.clone();
                cloned.setAmount(maxStackSize);
                player.getWorld().dropItem(player.getLocation(), cloned);
            }
            int left = amount % maxStackSize;
            if (left != 0) {
                ItemStack cloned = itemStack.clone();
                cloned.setAmount(left);
                player.getWorld().dropItem(player.getLocation(), cloned);
            }
        }

        return actualAmount;
    }

    public static ItemStack removeOwner(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return itemStack;
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.hasTag("owner")) {
            nbtItem.removeKey("owner");
            return nbtItem.getItem();
        }
        return itemStack;
    }

    /**
     * @return the amount of items that can't be put in the inventory
     */
    public static int putLootsToBag(Inventory inventory, ItemStack itemStack, int amount) {
        itemStack = removeOwner(itemStack.clone());
        ItemMeta meta = itemStack.getItemMeta();
        int maxStackSize = itemStack.getMaxStackSize();
        for (ItemStack other : inventory.getStorageContents()) {
            if (other != null) {
                if (other.getType() == itemStack.getType() && other.getItemMeta().equals(meta)) {
                    if (other.getAmount() < maxStackSize) {
                        int delta = maxStackSize - other.getAmount();
                        if (amount > delta) {
                            other.setAmount(maxStackSize);
                            amount -= delta;
                        } else {
                            other.setAmount(amount + other.getAmount());
                            return 0;
                        }
                    }
                }
            }
        }

        if (amount > 0) {
            for (ItemStack other : inventory.getStorageContents()) {
                if (other == null) {
                    if (amount > maxStackSize) {
                        amount -= maxStackSize;
                        ItemStack cloned = itemStack.clone();
                        cloned.setAmount(maxStackSize);
                        inventory.addItem(cloned);
                    } else {
                        ItemStack cloned = itemStack.clone();
                        cloned.setAmount(amount);
                        inventory.addItem(cloned);
                        return 0;
                    }
                }
            }
        }

        return amount;
    }
}
