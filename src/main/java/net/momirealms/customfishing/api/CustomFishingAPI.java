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

package net.momirealms.customfishing.api;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.FishingCondition;
import net.momirealms.customfishing.fishing.competition.Competition;
import net.momirealms.customfishing.fishing.loot.DroppedItem;
import net.momirealms.customfishing.fishing.loot.Item;
import net.momirealms.customfishing.fishing.loot.Loot;
import net.momirealms.customfishing.manager.ConfigManager;
import net.momirealms.customfishing.util.ItemStackUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class CustomFishingAPI {

    /**
     * get plugin instance
     * @return plugin instance
     */
    public static CustomFishing getPluginInstance() {
        return CustomFishing.getInstance();
    }

    /**
     * Is there a competition ongoing
     * @return is or not
     */
    public static boolean isCompetitionGoingOn() {
        return Competition.getCurrentCompetition() != null;
    }

    /**
     * Get the current competition
     * return null if there's no competition
     * @return competition
     */
    @Nullable
    public static Competition getCurrentCompetition() {
        return Competition.getCurrentCompetition();
    }

    /**
     * get a fish's size
     * @return size
     */
    public static float getFishSize(ItemStack fish) {
        return CustomFishing.getInstance().getFishingManager().getSize(fish);
    }

    /**
     * get an item's price
     * @param itemStack item to sell
     * @return price
     */
    public static double getItemPrice(ItemStack itemStack) {
        return CustomFishing.getInstance().getSellManager().getSingleItemPrice(itemStack);
    }

    /**
     * If an item exists in item library
     * @param type type
     * @param key key
     * @return exist
     */
    public static boolean doesItemExist(String type, String key) {
        return switch (type) {
            case "loot" -> CustomFishing.getInstance().getLootManager().hasLoot(key);
            case "rod" -> CustomFishing.getInstance().getEffectManager().getRodItem(key) != null;
            case "bait" -> CustomFishing.getInstance().getEffectManager().getBaitItem(key) != null;
            case "util" -> CustomFishing.getInstance().getEffectManager().getUtilItem(key) != null;
            default -> false;
        };
    }

    /**
     * If a world allow new fishing
     * @param world world
     * @return allow or not
     */
    public static boolean isFishingWorld(World world) {
        return ConfigManager.getWorldsList().contains(world.getName());
    }

    /**
     * Get all the possible loots for a certain player at a certain location
     * @param location location
     * @param player player
     * @return loots
     */
    public static List<Loot> getLootsAt(Location location, Player player) {
        return CustomFishing.getInstance().getFishingManager().getPossibleLootList(new FishingCondition(location, player, null), false, CustomFishing.getInstance().getLootManager().getAllLoots());
    }

    /**
     * Get all the possible loots at a certain location
     * @param location location
     * @return loots
     */
    public static List<Loot> getLootsAt(Location location) {
        return CustomFishing.getInstance().getFishingManager().getPossibleLootList(new FishingCondition(location, null, null), false, CustomFishing.getInstance().getLootManager().getAllLoots());
    }

    /**
     * Get a loot from Loot Manager
     * @param id id
     * @return loot
     */
    public static Loot getLootByID(String id) {
        return CustomFishing.getInstance().getLootManager().getLoot(id);
    }

    /**
     * get items directly from item library
     * return AIR if the loot does not exist
     * @param id item_id
     * @return itemStack
     */
    @NotNull
    public static ItemStack getLootItemByID(String id) {
        return CustomFishing.getInstance().getIntegrationManager().build(id);
    }

    /**
     * get items obtained by fishing
     * return AIR if the loot does not exist
     * @param id item_id
     * @param player player
     * @return itemStack
     */
    @NotNull
    public static ItemStack getLootItemByID(String id, @Nullable Player player) {
        Loot loot = CustomFishing.getInstance().getLootManager().getLoot(id);
        if (!(loot instanceof DroppedItem droppedItem)) return new ItemStack(Material.AIR);
        return CustomFishing.getInstance().getFishingManager().getCustomFishingLootItemStack(droppedItem, player);
    }

    /**
     * get rods directly from item library
     * return null if the rod does not exist
     * @param id rod_id
     * @return itemStack
     */
    public static ItemStack getRodItemByID(String id) {
        Item item = CustomFishing.getInstance().getEffectManager().getRodItem(id);
        return item == null ? null : ItemStackUtils.getFromItem(item);
    }

    /**
     * get baits directly from item library
     * return null if the bait does not exist
     * @param id bait_id
     * @return itemStack
     */
    public static ItemStack getBaitItemByID(String id) {
        Item item = CustomFishing.getInstance().getEffectManager().getBaitItem(id);
        return item == null ? null : ItemStackUtils.getFromItem(item);
    }

    /**
     * get utils directly from item library
     * return null if the util does not exist
     * @param id util_id
     * @return itemStack
     */
    public static ItemStack getUtilItemByID(String id) {
        Item item = CustomFishing.getInstance().getEffectManager().getUtilItem(id);
        return item == null ? null : ItemStackUtils.getFromItem(item);
    }

    /**
     * get the catch amount of a certain loot
     * return -1 if player's data is not loaded
     * @param id loot id
     * @param uuid uuid
     * @return amount
     */
    public static int getCertainLootCatchAmount(String id, UUID uuid) {
        return CustomFishing.getInstance().getStatisticsManager().getFishAmount(uuid, id);
    }

    /**
     * If the item is CustomFishing item
     * @param itemStack itemStack
     * @return is or not
     */
    public static boolean isCustomFishingItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return false;
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound nbtCompound = nbtItem.getCompound("CustomFishing");
        return nbtCompound != null;
    }

    /**
     * Add CustomFishing tag to an item
     * @param itemStack itemStack
     * @param type type
     * @param id id
     */
    public static void addCustomFishingTagToItem(ItemStack itemStack, String type, String id) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return;
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound nbtCompound = nbtItem.addCompound("CustomFishing");
        nbtCompound.setString("type", type);
        nbtCompound.setString("id", id);
        itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
    }

    /**
     * Get a player's earnings
     * @param player player
     * @return earnings
     */
    public static double getTodayEarning(Player player) {
        return CustomFishing.getInstance().getSellManager().getTodayEarning(player);
    }

    /**
     * return null if the itemStack is not a CustomFishing rod
     * @param itemStack itemStack
     * @return rod id
     */
    public static String getRodID(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return null;
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound nbtCompound = nbtItem.getCompound("CustomFishing");
        if (nbtCompound == null) return null;
        String type = nbtCompound.getString("type");
        if (!type.equals("rod")) return null;
        return nbtCompound.getString("id");
    }

    /**
     * return null if the itemStack is not a CustomFishing util
     * @param itemStack itemStack
     * @return util id
     */
    public static String getUtilID(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return null;
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound nbtCompound = nbtItem.getCompound("CustomFishing");
        if (nbtCompound == null) return null;
        String type = nbtCompound.getString("type");
        if (!type.equals("util")) return null;
        return nbtCompound.getString("id");
    }

    /**
     * return null if the itemStack is not a CustomFishing bait
     * @param itemStack itemStack
     * @return bait id
     */
    public static String getBaitID(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return null;
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound nbtCompound = nbtItem.getCompound("CustomFishing");
        if (nbtCompound == null) return null;
        String type = nbtCompound.getString("type");
        if (!type.equals("bait")) return null;
        return nbtCompound.getString("id");
    }

    /**
     * return null if the itemStack is not a CustomFishing loot
     * @param itemStack itemStack
     * @return loot id
     */
    public static String getLootID(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return null;
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound nbtCompound = nbtItem.getCompound("CustomFishing");
        if (nbtCompound == null) return null;
        String type = nbtCompound.getString("type");
        if (!type.equals("loot")) return null;
        return nbtCompound.getString("id");
    }
}
