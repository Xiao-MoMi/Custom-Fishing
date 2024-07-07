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

package net.momirealms.customfishing.bukkit.util;

import com.saicone.rtag.item.ItemTagStream;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.mechanic.item.tag.TagEditor;
import net.momirealms.customfishing.api.mechanic.item.tag.TagListType;
import net.momirealms.customfishing.api.mechanic.item.tag.TagValueType;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.api.mechanic.misc.value.TextValue;
import net.momirealms.customfishing.common.util.ArrayUtils;
import net.momirealms.customfishing.common.util.Pair;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

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

    public static Map<String, Object> itemStackToMap(ItemStack itemStack) {
        Map<String, Object> map = ItemTagStream.INSTANCE.toMap(itemStack);
        map.remove("rtagDataVersion");
        map.remove("count");
        map.remove("id");
        map.put("material", itemStack.getType().name().toLowerCase(Locale.ENGLISH));
        map.put("amount", itemStack.getAmount());
        Object tag = map.remove("tags");
        if (tag != null) {
            map.put("nbt", tag);
        }
        return map;
    }

    // TODO Improve the map parser and refactor this method to make it more readable
    public static void sectionToEditor(Section section, List<TagEditor> tagEditors, String... route) {
        for (Map.Entry<String, Object> entry : section.getStringRouteMappedValues(false).entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey();
            String[] currentRoute = ArrayUtils.appendElementToArray(route, key);
            if (value instanceof Section inner) {
                sectionToEditor(inner, tagEditors, currentRoute);
            } else if (value instanceof List<?> list) {
                TagListType type = getListType(list);
                if (type == TagListType.TAG) {
//                    List<TagMap> maps = new ArrayList<>();
//                    for (Object o : list) {
//                        Map<String, Object> map = (Map<String, Object>) o;
//
//                    }
                    tagEditors.add(((item, context) -> {
//                        List<Map<String, Object>> parsed = maps.stream().map(render -> render.apply(context)).toList();
                        item.set(list, (Object[]) currentRoute);
                    }));
                } else {
                    Object first = list.get(0);
                    if (first instanceof String str) {
                        Pair<TagValueType, String> pair = toTypeAndData(str);
                        switch (pair.left()) {
                            case INT -> {
                                List<MathValue<Player>> values = new ArrayList<>();
                                for (Object o : list) {
                                    values.add(MathValue.auto(toTypeAndData((String) o).right()));
                                }
                                tagEditors.add(((item, context) -> {
                                    List<Integer> integers = values.stream().map(unparsed -> (int) unparsed.evaluate(context)).toList();
                                    item.set(integers, (Object[]) currentRoute);
                                }));
                            }
                            case BYTE -> {
                                List<MathValue<Player>> values = new ArrayList<>();
                                for (Object o : list) {
                                    values.add(MathValue.auto(toTypeAndData((String) o).right()));
                                }
                                tagEditors.add(((item, context) -> {
                                    List<Byte> bytes = values.stream().map(unparsed -> (byte) unparsed.evaluate(context)).toList();
                                    item.set(bytes, (Object[]) currentRoute);
                                }));
                            }
                            case LONG -> {
                                List<MathValue<Player>> values = new ArrayList<>();
                                for (Object o : list) {
                                    values.add(MathValue.auto(toTypeAndData((String) o).right()));
                                }
                                tagEditors.add(((item, context) -> {
                                    List<Long> longs = values.stream().map(unparsed -> (long) unparsed.evaluate(context)).toList();
                                    item.set(longs, (Object[]) currentRoute);
                                }));
                            }
                            case FLOAT -> {
                                List<MathValue<Player>> values = new ArrayList<>();
                                for (Object o : list) {
                                    values.add(MathValue.auto(toTypeAndData((String) o).right()));
                                }
                                tagEditors.add(((item, context) -> {
                                    List<Float> floats = values.stream().map(unparsed -> (float) unparsed.evaluate(context)).toList();
                                    item.set(floats, (Object[]) currentRoute);
                                }));
                            }
                            case DOUBLE -> {
                                List<MathValue<Player>> values = new ArrayList<>();
                                for (Object o : list) {
                                    values.add(MathValue.auto(toTypeAndData((String) o).right()));
                                }
                                tagEditors.add(((item, context) -> {
                                    List<Double> doubles = values.stream().map(unparsed -> (double) unparsed.evaluate(context)).toList();
                                    item.set(doubles, (Object[]) currentRoute);
                                }));
                            }
                            case STRING -> {
                                List<TextValue<Player>> values = new ArrayList<>();
                                for (Object o : list) {
                                    values.add(TextValue.auto(toTypeAndData((String) o).right()));
                                }
                                tagEditors.add(((item, context) -> {
                                    List<String> texts = values.stream().map(unparsed -> unparsed.render(context)).toList();
                                    item.set(texts, (Object[]) currentRoute);
                                }));
                            }
                        }
                    } else {
                        tagEditors.add(((item, context) -> {
                            item.set(list, (Object[]) currentRoute);
                        }));
                    }
                }
            } else if (value instanceof String str) {
                Pair<TagValueType, String> pair = toTypeAndData(str);
                switch (pair.left()) {
                    case INT -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        tagEditors.add(((item, context) -> {
                            item.set((int) mathValue.evaluate(context), (Object[]) currentRoute);
                        }));
                    }
                    case BYTE -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        tagEditors.add(((item, context) -> {
                            item.set((byte) mathValue.evaluate(context), (Object[]) currentRoute);
                        }));
                    }
                    case LONG -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        tagEditors.add(((item, context) -> {
                            item.set((long) mathValue.evaluate(context), (Object[]) currentRoute);
                        }));
                    }
                    case SHORT -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        tagEditors.add(((item, context) -> {
                            item.set((short) mathValue.evaluate(context), (Object[]) currentRoute);
                        }));
                    }
                    case DOUBLE -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        tagEditors.add(((item, context) -> {
                            item.set((double) mathValue.evaluate(context), (Object[]) currentRoute);
                        }));
                    }
                    case FLOAT -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        tagEditors.add(((item, context) -> {
                            item.set((float) mathValue.evaluate(context), (Object[]) currentRoute);
                        }));
                    }
                    case STRING -> {
                        TextValue<Player> textValue = TextValue.auto(pair.right());
                        tagEditors.add(((item, context) -> {
                            item.set(textValue.render(context), (Object[]) currentRoute);
                        }));
                    }
                    case INTARRAY -> {
                        String[] split = splitValue(str);
                        int[] array = Arrays.stream(split).mapToInt(Integer::parseInt).toArray();
                        tagEditors.add(((item, context) -> {
                            item.set(array, (Object[]) currentRoute);
                        }));
                    }
                    case BYTEARRAY -> {
                        String[] split = splitValue(str);
                        byte[] bytes = new byte[split.length];
                        for (int i = 0; i < split.length; i++){
                            bytes[i] = Byte.parseByte(split[i]);
                        }
                        tagEditors.add(((item, context) -> {
                            item.set(bytes, (Object[]) currentRoute);
                        }));
                    }
                }
            } else {
                tagEditors.add(((item, context) -> {
                    item.set(value, (Object[]) currentRoute);
                }));
            }
        }
    }

    private static TagListType getListType(List<?> list) {
        Object o = list.get(0);
        if (o instanceof Map<?, ?> map) {
            return TagListType.TAG;
        } else {
            return TagListType.VALUE;
        }
    }

    public static Pair<TagValueType, String> toTypeAndData(String str) {
        String[] parts = str.split("\\s+", 2);
        if (parts.length == 1) {
            return Pair.of(TagValueType.STRING, parts[0]);
        }
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid value format: " + str);
        }
        TagValueType type = TagValueType.valueOf(parts[0].substring(1, parts[0].length() - 1).toUpperCase(Locale.ENGLISH));
        String data = parts[1];
        return Pair.of(type, data);
    }

    private static String[] splitValue(String value) {
        return value.substring(value.indexOf('[') + 1, value.lastIndexOf(']'))
                .replaceAll("\\s", "")
                .split(",");
    }
}
