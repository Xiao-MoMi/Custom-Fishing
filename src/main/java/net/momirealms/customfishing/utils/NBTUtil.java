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

package net.momirealms.customfishing.utils;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class NBTUtil {

    public static ItemStack addIdentifier(ItemStack itemStack, String type, String id){
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.addCompound("CustomFishing");
        nbtItem.getCompound("CustomFishing").setString("type", type);
        nbtItem.getCompound("CustomFishing").setString("id", id);
        return nbtItem.getItem();
    }

    public static NBTItem getNBTItem(Map<String,Object> nbt, ItemStack itemStack){
        NBTItem nbtItem = new NBTItem(itemStack);
        setTags(nbt, nbtItem);
        return nbtItem;
    }

    public static void setTags(Map<String,Object> map, NBTCompound nbtCompound){
        map.keySet().forEach(key -> {
            if (map.get(key) instanceof MemorySection map2){
                nbtCompound.addCompound(key);
                setTags(map2.getValues(false), nbtCompound.getCompound(key));
            }
            else if (map.get(key) instanceof Map<?,?> map1){
                nbtCompound.addCompound(key);
                setTags((Map<String, Object>) map1, nbtCompound.getCompound(key));
            }
            else if (map.get(key) instanceof List list){
                for (Object o : list) {
                    if (o instanceof String s) {
                        if (s.startsWith("(String) ")) {
                            nbtCompound.getStringList(key).add(s.substring(9));
                        } else if (s.startsWith("(UUID) ")) {
                            nbtCompound.getUUIDList(key).add(UUID.fromString(s.substring(7)));
                        } else if (s.startsWith("(Double) ")) {
                            nbtCompound.getDoubleList(key).add(Double.valueOf(s.substring(9)));
                        } else if (s.startsWith("(Long) ")) {
                            nbtCompound.getLongList(key).add(Long.valueOf(s.substring(7)));
                        } else if (s.startsWith("(Float) ")) {
                            nbtCompound.getFloatList(key).add(Float.valueOf(s.substring(8)));
                        } else if (s.startsWith("(Int) ")) {
                            nbtCompound.getIntegerList(key).add(Integer.valueOf(s.substring(6)));
                        } else if (s.startsWith("(IntArray) ")){
                            String[] split = s.substring(11).replace("[","").replace("]","").replaceAll("\\s", "").split(",");
                            int[] array = Arrays.stream(split).mapToInt(Integer::parseInt).toArray();
                            nbtCompound.getIntArrayList(key).add(array);
                        }
                    }
                    else if (o instanceof Map map1) {
                        setTags(map1, nbtCompound.getCompoundList(key).addCompound());
                    }
                }
            }
            else {
                if (map.get(key) instanceof String string){
                    if (string.startsWith("(Int) ")){
                        nbtCompound.setInteger(key, Integer.valueOf(string.substring(6)));
                    }else if (string.startsWith("(String) ")){
                        nbtCompound.setString(key, string.substring(9));
                    }else if (string.startsWith("(Long) ")){
                        nbtCompound.setLong(key, Long.valueOf(string.substring(7)));
                    }else if (string.startsWith("(Float) ")){
                        nbtCompound.setFloat(key, Float.valueOf(string.substring(8)));
                    }else if (string.startsWith("(Double) ")){
                        nbtCompound.setDouble(key, Double.valueOf(string.substring(9)));
                    }else if (string.startsWith("(Short) ")){
                        nbtCompound.setShort(key, Short.valueOf(string.substring(8)));
                    }else if (string.startsWith("(Boolean) ")){
                        nbtCompound.setBoolean(key, Boolean.valueOf(string.substring(10)));
                    }else if (string.startsWith("(UUID) ")){
                        nbtCompound.setUUID(key, UUID.fromString(string.substring(7)));
                    }else if (string.startsWith("(Byte) ")){
                        nbtCompound.setByte(key, Byte.valueOf(string.substring(7)));
                    }else if (string.startsWith("(ByteArray) ")){
                        String[] split = string.substring(12).replace("[","").replace("]","").replaceAll("\\s", "").split(",");
                        byte[] bytes = new byte[split.length];
                        for (int i = 0; i < split.length; i++){
                            bytes[i] = Byte.parseByte(split[i]);
                        }
                        nbtCompound.setByteArray(key, bytes);
                    }else if (string.startsWith("(IntArray) ")){
                        String[] split = string.substring(11).replace("[","").replace("]","").replaceAll("\\s", "").split(",");
                        int[] array = Arrays.stream(split).mapToInt(Integer::parseInt).toArray();
                        nbtCompound.setIntArray(key, array);
                    }
                }
            }
        });
    }
}