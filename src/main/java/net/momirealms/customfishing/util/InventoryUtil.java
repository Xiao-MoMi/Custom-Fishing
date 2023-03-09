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

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class InventoryUtil {

    /**
     * Converts itemStacks to base64
     * @param contents items
     * @return base64
     */
    public static @NotNull String toBase64(ItemStack[] contents) {
        boolean convert = false;
        for (ItemStack content : contents) {
            if (content != null) {
                convert = true;
                break;
            }
        }
        if (convert) {
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
                throw new RuntimeException("[CustomFishing] Data save error", e);
            }
        }
        return "";
    }

    /**
     * Get itemStacks from base64
     * @param base64 base64
     * @return itemStacks
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
            }
            catch (IOException | ClassNotFoundException | NullPointerException e) {
                try {
                    dataInput.close();
                } catch (IOException exception) {
                    AdventureUtil.consoleMessage("<red>[CustomFishing] Error! Failed to read fishing bag data");
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
