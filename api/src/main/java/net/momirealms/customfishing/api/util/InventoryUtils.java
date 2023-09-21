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

package net.momirealms.customfishing.api.util;

import net.kyori.adventure.text.Component;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class for working with Bukkit Inventories and item stacks.
 */
public class InventoryUtils {

    private InventoryUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * Create a custom inventory with a specified size and title component.
     *
     * @param inventoryHolder The holder of the inventory.
     * @param size            The size of the inventory.
     * @param component       The title component of the inventory.
     * @return The created Inventory instance.
     */
    public static Inventory createInventory(InventoryHolder inventoryHolder, int size, Component component) {
        try {
            boolean isSpigot = CustomFishingPlugin.get().getVersionManager().isSpigot();
            Method createInvMethod = ReflectionUtils.bukkitClass.getMethod(
                    "createInventory",
                    InventoryHolder.class,
                    int.class,
                    isSpigot ? String.class : ReflectionUtils.componentClass
            );
            return (Inventory) createInvMethod.invoke(
                    null,
                    inventoryHolder,
                    size,
                    isSpigot ? CustomFishingPlugin.get().getAdventure().componentToLegacy(component) : CustomFishingPlugin.get().getAdventure().shadedComponentToPaperComponent(component)
            );
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Create a custom inventory with a specified type and title component.
     *
     * @param inventoryHolder The holder of the inventory.
     * @param type            The type of the inventory.
     * @param component       The title component of the inventory.
     * @return The created Inventory instance.
     */
    public static Inventory createInventory(InventoryHolder inventoryHolder, InventoryType type, Component component) {
        try {
            boolean isSpigot = CustomFishingPlugin.get().getVersionManager().isSpigot();
            Method createInvMethod = ReflectionUtils.bukkitClass.getMethod(
                    "createInventory",
                    InventoryHolder.class,
                    InventoryType.class,
                    isSpigot ? String.class : ReflectionUtils.componentClass
            );
            return (Inventory) createInvMethod.invoke(
                    null,
                    inventoryHolder,
                    type,
                    isSpigot ? CustomFishingPlugin.get().getAdventure().componentToLegacy(component) : CustomFishingPlugin.get().getAdventure().shadedComponentToPaperComponent(component)
            );
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Serialize an array of ItemStacks to a Base64-encoded string.
     *
     * @param contents The ItemStack array to serialize.
     * @return The Base64-encoded string representing the serialized ItemStacks.
     */
    public static @NotNull String stacksToBase64(ItemStack[] contents) {
        if (contents.length == 0) {
            return "";
        }
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(contents.length);
            for (ItemStack itemStack : contents) {
                dataOutput.writeObject(itemStack);
            }
            dataOutput.close();
            byte[] byteArr = outputStream.toByteArray();
            outputStream.close();
            return Base64Coder.encodeLines(byteArr);
        } catch (IOException e) {
            LogUtils.warn("Encoding error", e);
        }
        return "";
    }

    /**
     * Deserialize an ItemStack array from a Base64-encoded string.
     *
     * @param base64 The Base64-encoded string representing the serialized ItemStacks.
     * @return An array of ItemStacks deserialized from the input string.
     */
    @Nullable
    public static ItemStack[] getInventoryItems(String base64) {
        ItemStack[] itemStacks = null;
        try {
            itemStacks = stacksFromBase64(base64);
        } catch (IllegalArgumentException exception) {
            exception.printStackTrace();
        }
        return itemStacks;
    }

    private static ItemStack[] stacksFromBase64(String data) {
        if (data == null || data.equals("")) return new ItemStack[]{};

        ByteArrayInputStream inputStream;
        try {
            inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        } catch (IllegalArgumentException e) {
            return new ItemStack[]{};
        }
        BukkitObjectInputStream dataInput = null;
        ItemStack[] stacks = null;
        try {
            dataInput = new BukkitObjectInputStream(inputStream);
            stacks = new ItemStack[dataInput.readInt()];
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (stacks == null) return new ItemStack[]{};
        for (int i = 0; i < stacks.length; i++) {
            try {
                stacks[i] = (ItemStack) dataInput.readObject();
            } catch (IOException | ClassNotFoundException | NullPointerException e) {
                try {
                    dataInput.close();
                } catch (IOException exception) {
                    LogUtils.severe("Failed to read fishing bag data");
                }
                return null;
            }
        }
        try {
            dataInput.close();
        } catch (IOException ignored) {
        }
        return stacks;
    }
}
