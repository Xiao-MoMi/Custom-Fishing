package net.momirealms.customfishing.util;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NBTUtil {

    public static NBTItem getNBTItem(Map<String, Object> nbt, ItemStack itemStack){
        NBTItem nbtItem = new NBTItem(itemStack);
        setTags(nbt, nbtItem);
        return nbtItem;
    }

    public static void setTags(Map<String, Object> map, NBTCompound nbtCompound) {
        for (String key : map.keySet()) {
            if (map.get(key) instanceof MemorySection memorySection){
                NBTCompound newCompound = nbtCompound.addCompound(key);
                setTags(memorySection.getValues(false), newCompound);
            }
            else if (map.get(key) instanceof List list){
                for (Object o : list) {
                    if (o instanceof String value) {
                        setListValue(key, value, nbtCompound);
                    }
                }
            }
            else if (map.get(key) instanceof String value) {
                setSingleValue(key, value, nbtCompound);
            }
        }
    }

    private static void setListValue(String key, String value, NBTCompound nbtCompound) {
        if (value.startsWith("(String) ")) {
            nbtCompound.getStringList(key).add(value.substring(9));
        } else if (value.startsWith("(UUID) ")) {
            nbtCompound.getUUIDList(key).add(UUID.fromString(value.substring(7)));
        } else if (value.startsWith("(Double) ")) {
            nbtCompound.getDoubleList(key).add(Double.valueOf(value.substring(9)));
        } else if (value.startsWith("(Long) ")) {
            nbtCompound.getLongList(key).add(Long.valueOf(value.substring(7)));
        } else if (value.startsWith("(Float) ")) {
            nbtCompound.getFloatList(key).add(Float.valueOf(value.substring(8)));
        } else if (value.startsWith("(Int) ")) {
            nbtCompound.getIntegerList(key).add(Integer.valueOf(value.substring(6)));
        } else if (value.startsWith("(IntArray) ")) {
            String[] split = value.substring(11).replace("[","").replace("]","").replaceAll("\\s", "").split(",");
            int[] array = Arrays.stream(split).mapToInt(Integer::parseInt).toArray();
            nbtCompound.getIntArrayList(key).add(array);
        }
    }

    private static void setSingleValue(String key, String value, NBTCompound nbtCompound) {
        if (value.startsWith("(Int) ")){
            nbtCompound.setInteger(key, Integer.valueOf(value.substring(6)));
        } else if (value.startsWith("(String) ")){
            nbtCompound.setString(key, value.substring(9));
        } else if (value.startsWith("(Long) ")){
            nbtCompound.setLong(key, Long.valueOf(value.substring(7)));
        } else if (value.startsWith("(Float) ")){
            nbtCompound.setFloat(key, Float.valueOf(value.substring(8)));
        } else if (value.startsWith("(Double) ")){
            nbtCompound.setDouble(key, Double.valueOf(value.substring(9)));
        } else if (value.startsWith("(Short) ")){
            nbtCompound.setShort(key, Short.valueOf(value.substring(8)));
        } else if (value.startsWith("(Boolean) ")){
            nbtCompound.setBoolean(key, Boolean.valueOf(value.substring(10)));
        } else if (value.startsWith("(UUID) ")){
            nbtCompound.setUUID(key, UUID.fromString(value.substring(7)));
        } else if (value.startsWith("(Byte) ")){
            nbtCompound.setByte(key, Byte.valueOf(value.substring(7)));
        } else if (value.startsWith("(ByteArray) ")){
            String[] split = value.substring(12).replace("[","").replace("]","").replaceAll("\\s", "").split(",");
            byte[] bytes = new byte[split.length];
            for (int i = 0; i < split.length; i++){
                bytes[i] = Byte.parseByte(split[i]);
            }
            nbtCompound.setByteArray(key, bytes);
        } else if (value.startsWith("(IntArray) ")){
            String[] split = value.substring(11).replace("[","").replace("]","").replaceAll("\\s", "").split(",");
            int[] array = Arrays.stream(split).mapToInt(Integer::parseInt).toArray();
            nbtCompound.setIntArray(key, array);
        }
    }
}
