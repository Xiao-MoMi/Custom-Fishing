package net.momirealms.customfishing.bukkit.util;

import com.saicone.rtag.RtagMirror;
import com.saicone.rtag.item.ItemObject;
import com.saicone.rtag.tag.TagCompound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class ItemStackUtils {

    private ItemStackUtils() {}

    public static ItemStack fromBase64(String base64) {
        if (base64 == null || base64.isEmpty())
            return new ItemStack(Material.AIR);
        ByteArrayInputStream inputStream;
        try {
            inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
        } catch (IllegalArgumentException e) {
            return new ItemStack(Material.AIR);
        }
        ItemStack stack = null;
        try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            stack = (ItemStack) dataInput.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return stack;
    }

    public static String toBase64(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return "";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeObject(itemStack);
            byte[] byteArr = outputStream.toByteArray();
            dataOutput.close();
            outputStream.close();
            return Base64Coder.encodeLines(byteArr);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Map<String, Object> toReadableMap(ItemStack item) {
        return toMap(item);
    }

    private static Map<String, Object> toMap(ItemStack object) {
        return TagCompound.getValue(RtagMirror.INSTANCE, toCompound(object));
    }

    private static Object toCompound(ItemStack object) {
        if (object == null) {
            return null;
        } else {
            Object compound = extract(object);
            return TagCompound.isTagCompound(compound) ? compound : null;
        }
    }

    private static Object extract(ItemStack object) {
        return ItemObject.save(ItemObject.asNMSCopy(object));
    }
}
