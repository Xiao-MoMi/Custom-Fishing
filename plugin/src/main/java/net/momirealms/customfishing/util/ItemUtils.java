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
import net.momirealms.customfishing.api.mechanic.hook.HookSetting;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.setting.CFConfig;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtils {

    public static NBTItem updateNBTItemLore(NBTItem nbtItem) {
        NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
        if (cfCompound == null)
            return nbtItem;

        boolean hasLoreUpdate = cfCompound.hasTag("hook_id") || cfCompound.hasTag("max_dur");
        if (!hasLoreUpdate) return nbtItem;

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

    public static void updateItemLore(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return;
        NBTItem nbtItem = updateNBTItemLore(new NBTItem(itemStack));
        itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
    }

    public static void reduceHookDurability(ItemStack itemStack, boolean updateLore) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return;
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
        if (cfCompound != null && cfCompound.hasTag("hook_dur")) {
            int hookDur = cfCompound.getInteger("hook_dur");
            if (hookDur > 0) {
                cfCompound.setInteger("hook_dur", hookDur - 1);
            } else if (hookDur != -1) {
                cfCompound.removeKey("hook_id");
                cfCompound.removeKey("hook_dur");
                cfCompound.removeKey("hook_id");
            }
        }
        if (updateLore) updateNBTItemLore(nbtItem);
        itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
    }

    public static void loseDurability(ItemStack itemStack, int amount, boolean updateLore) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return;
        int unBreakingLevel = itemStack.getEnchantmentLevel(Enchantment.DURABILITY);
        if (Math.random() > (double) 1 / (unBreakingLevel + 1)) {
            return;
        }
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.getByte("Unbreakable") == 1) {
            return;
        }
        NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
        if (cfCompound != null && cfCompound.hasTag("max_dur")) {
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
            int damage = nbtItem.getInteger("Damage") + amount;
            if (damage > itemStack.getType().getMaxDurability()) {
                itemStack.setAmount(0);
            } else {
                nbtItem.setInteger("Damage", damage);
                itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
            }
        }
    }

    public static void addDurability(ItemStack itemStack, int amount, boolean updateLore) {
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

    public static int getDurability(ItemStack itemStack) {
        if (!(itemStack.getItemMeta() instanceof Damageable damageable))
            return -1;
        if (damageable.isUnbreakable())
            return -1;
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
        if (cfCompound != null && cfCompound.hasTag("max_dur")) {
            return cfCompound.getInteger("cur_dur");
        } else {
            return itemStack.getType().getMaxDurability() - damageable.getDamage();
        }
    }

    public static int giveCertainAmountOfItem(Player player, ItemStack itemStack, int amount) {
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
}
