/*
 *  Copyright (C) <2024> <XiaoMoMi>
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
import net.momirealms.customfishing.api.mechanic.item.ItemEditor;
import net.momirealms.customfishing.api.mechanic.item.tag.TagMap;
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

import static net.momirealms.customfishing.api.util.TagUtils.toTypeAndData;
import static net.momirealms.customfishing.common.util.ArrayUtils.splitValue;

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

    private static void sectionToMap(Section section, Map<String, Object> outPut) {
        for (Map.Entry<String, Object> entry : section.getStringRouteMappedValues(false).entrySet()) {
            if (entry.getValue() instanceof Section inner) {
                HashMap<String, Object> map = new HashMap<>();
                outPut.put(entry.getKey(), map);
                sectionToMap(inner, map);
            } else {
                outPut.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @SuppressWarnings({"unchecked", "UnstableApiUsage"})
    public static void sectionToComponentEditor(Section section, List<ItemEditor> itemEditors) {
        for (Map.Entry<String, Object> entry : section.getStringRouteMappedValues(false).entrySet()) {
            String component = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Section inner) {
                Map<String, Object> innerMap = new HashMap<>();
                sectionToMap(inner, innerMap);
                TagMap tagMap = TagMap.of(innerMap);
                itemEditors.add(((item, context) -> {
                    item.setComponent(component, tagMap.apply(context));
                }));
            } else if (value instanceof List<?> list) {
                Object first = list.get(0);
                if (first instanceof Map<?,?>) {
                    ArrayList<TagMap> output = new ArrayList<>();
                    for (Object o : list) {
                        Map<String, Object> innerMap = (Map<String, Object>) o;
                        TagMap tagMap = TagMap.of(innerMap);
                        output.add(tagMap);
                    }
                    itemEditors.add(((item, context) -> {
                        List<Map<String, Object>> maps = output.stream().map(unparsed -> unparsed.apply(context)).toList();
                        item.setComponent(component, maps);
                    }));
                } else if (first instanceof String str) {
                    Pair<TagValueType, String> pair = toTypeAndData(str);
                    switch (pair.left()) {
                        case INT -> {
                            List<MathValue<Player>> values = new ArrayList<>();
                            for (Object o : list) {
                                values.add(MathValue.auto(toTypeAndData((String) o).right()));
                            }
                            itemEditors.add(((item, context) -> {
                                List<Integer> integers = values.stream().map(unparsed -> (int) unparsed.evaluate(context)).toList();
                                item.setComponent(component, integers);
                            }));
                        }
                        case BYTE -> {
                            List<MathValue<Player>> values = new ArrayList<>();
                            for (Object o : list) {
                                values.add(MathValue.auto(toTypeAndData((String) o).right()));
                            }
                            itemEditors.add(((item, context) -> {
                                List<Byte> bytes = values.stream().map(unparsed -> (byte) unparsed.evaluate(context)).toList();
                                item.setComponent(component, bytes);
                            }));
                        }
                        case LONG -> {
                            List<MathValue<Player>> values = new ArrayList<>();
                            for (Object o : list) {
                                values.add(MathValue.auto(toTypeAndData((String) o).right()));
                            }
                            itemEditors.add(((item, context) -> {
                                List<Long> longs = values.stream().map(unparsed -> (long) unparsed.evaluate(context)).toList();
                                item.setComponent(component, longs);
                            }));
                        }
                        case FLOAT -> {
                            List<MathValue<Player>> values = new ArrayList<>();
                            for (Object o : list) {
                                values.add(MathValue.auto(toTypeAndData((String) o).right()));
                            }
                            itemEditors.add(((item, context) -> {
                                List<Float> floats = values.stream().map(unparsed -> (float) unparsed.evaluate(context)).toList();
                                item.setComponent(component, floats);
                            }));
                        }
                        case DOUBLE -> {
                            List<MathValue<Player>> values = new ArrayList<>();
                            for (Object o : list) {
                                values.add(MathValue.auto(toTypeAndData((String) o).right()));
                            }
                            itemEditors.add(((item, context) -> {
                                List<Double> doubles = values.stream().map(unparsed -> (double) unparsed.evaluate(context)).toList();
                                item.setComponent(component, doubles);
                            }));
                        }
                        case STRING -> {
                            List<TextValue<Player>> values = new ArrayList<>();
                            for (Object o : list) {
                                values.add(TextValue.auto(toTypeAndData((String) o).right()));
                            }
                            itemEditors.add(((item, context) -> {
                                List<String> texts = values.stream().map(unparsed -> unparsed.render(context)).toList();
                                item.setComponent(component, texts);
                            }));
                        }
                    }

                } else {
                    itemEditors.add(((item, context) -> {
                        item.setComponent(component, list);
                    }));
                }
            } else if (value instanceof String str) {
                Pair<TagValueType, String> pair = toTypeAndData(str);
                switch (pair.left()) {
                    case INT -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        itemEditors.add(((item, context) -> {
                            item.setComponent(component, (int) mathValue.evaluate(context));
                        }));
                    }
                    case BYTE -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        itemEditors.add(((item, context) -> {
                            item.setComponent(component, (byte) mathValue.evaluate(context));
                        }));
                    }
                    case FLOAT -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        itemEditors.add(((item, context) -> {
                            item.setComponent(component, (float) mathValue.evaluate(context));
                        }));
                    }
                    case LONG -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        itemEditors.add(((item, context) -> {
                            item.setComponent(component, (long) mathValue.evaluate(context));
                        }));
                    }
                    case SHORT -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        itemEditors.add(((item, context) -> {
                            item.setComponent(component, (short) mathValue.evaluate(context));
                        }));
                    }
                    case DOUBLE -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        itemEditors.add(((item, context) -> {
                            item.setComponent(component, (double) mathValue.evaluate(context));
                        }));
                    }
                    case STRING -> {
                        TextValue<Player> textValue = TextValue.auto(pair.right());
                        itemEditors.add(((item, context) -> {
                            item.setComponent(component, textValue.render(context));
                        }));
                    }
                    case INTARRAY -> {
                        String[] split = splitValue(str);
                        int[] array = Arrays.stream(split).mapToInt(Integer::parseInt).toArray();
                        itemEditors.add(((item, context) -> {
                            item.setComponent(component, array);
                        }));
                    }
                    case BYTEARRAY -> {
                        String[] split = splitValue(str);
                        byte[] bytes = new byte[split.length];
                        for (int i = 0; i < split.length; i++){
                            bytes[i] = Byte.parseByte(split[i]);
                        }
                        itemEditors.add(((item, context) -> {
                            item.setComponent(component, bytes);
                        }));
                    }
                }
            } else {
                itemEditors.add(((item, context) -> {
                    item.setComponent(component, value);
                }));
            }
        }
    }

    // ugly codes, remaining improvements
    @SuppressWarnings("unchecked")
    public static void sectionToTagEditor(Section section, List<ItemEditor> itemEditors, String... route) {
        for (Map.Entry<String, Object> entry : section.getStringRouteMappedValues(false).entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey();
            String[] currentRoute = ArrayUtils.appendElementToArray(route, key);
            if (value instanceof Section inner) {
                sectionToTagEditor(inner, itemEditors, currentRoute);
            } else if (value instanceof List<?> list) {
                Object first = list.get(0);
                if (first instanceof Map<?, ?>) {
                    List<TagMap> maps = new ArrayList<>();
                    for (Object o : list) {
                        Map<String, Object> map = (Map<String, Object>) o;
                        maps.add(TagMap.of(map));
                    }
                    itemEditors.add(((item, context) -> {
                        List<Map<String, Object>> parsed = maps.stream().map(render -> render.apply(context)).toList();
                        item.set(parsed, (Object[]) currentRoute);
                    }));
                } else {
                    if (first instanceof String str) {
                        Pair<TagValueType, String> pair = toTypeAndData(str);
                        switch (pair.left()) {
                            case INT -> {
                                List<MathValue<Player>> values = new ArrayList<>();
                                for (Object o : list) {
                                    values.add(MathValue.auto(toTypeAndData((String) o).right()));
                                }
                                itemEditors.add(((item, context) -> {
                                    List<Integer> integers = values.stream().map(unparsed -> (int) unparsed.evaluate(context)).toList();
                                    item.set(integers, (Object[]) currentRoute);
                                }));
                            }
                            case BYTE -> {
                                List<MathValue<Player>> values = new ArrayList<>();
                                for (Object o : list) {
                                    values.add(MathValue.auto(toTypeAndData((String) o).right()));
                                }
                                itemEditors.add(((item, context) -> {
                                    List<Byte> bytes = values.stream().map(unparsed -> (byte) unparsed.evaluate(context)).toList();
                                    item.set(bytes, (Object[]) currentRoute);
                                }));
                            }
                            case LONG -> {
                                List<MathValue<Player>> values = new ArrayList<>();
                                for (Object o : list) {
                                    values.add(MathValue.auto(toTypeAndData((String) o).right()));
                                }
                                itemEditors.add(((item, context) -> {
                                    List<Long> longs = values.stream().map(unparsed -> (long) unparsed.evaluate(context)).toList();
                                    item.set(longs, (Object[]) currentRoute);
                                }));
                            }
                            case FLOAT -> {
                                List<MathValue<Player>> values = new ArrayList<>();
                                for (Object o : list) {
                                    values.add(MathValue.auto(toTypeAndData((String) o).right()));
                                }
                                itemEditors.add(((item, context) -> {
                                    List<Float> floats = values.stream().map(unparsed -> (float) unparsed.evaluate(context)).toList();
                                    item.set(floats, (Object[]) currentRoute);
                                }));
                            }
                            case DOUBLE -> {
                                List<MathValue<Player>> values = new ArrayList<>();
                                for (Object o : list) {
                                    values.add(MathValue.auto(toTypeAndData((String) o).right()));
                                }
                                itemEditors.add(((item, context) -> {
                                    List<Double> doubles = values.stream().map(unparsed -> (double) unparsed.evaluate(context)).toList();
                                    item.set(doubles, (Object[]) currentRoute);
                                }));
                            }
                            case STRING -> {
                                List<TextValue<Player>> values = new ArrayList<>();
                                for (Object o : list) {
                                    values.add(TextValue.auto(toTypeAndData((String) o).right()));
                                }
                                itemEditors.add(((item, context) -> {
                                    List<String> texts = values.stream().map(unparsed -> unparsed.render(context)).toList();
                                    item.set(texts, (Object[]) currentRoute);
                                }));
                            }
                        }
                    } else {
                        itemEditors.add(((item, context) -> {
                            item.set(list, (Object[]) currentRoute);
                        }));
                    }
                }
            } else if (value instanceof String str) {
                Pair<TagValueType, String> pair = toTypeAndData(str);
                switch (pair.left()) {
                    case INT -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        itemEditors.add(((item, context) -> {
                            item.set((int) mathValue.evaluate(context), (Object[]) currentRoute);
                        }));
                    }
                    case BYTE -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        itemEditors.add(((item, context) -> {
                            item.set((byte) mathValue.evaluate(context), (Object[]) currentRoute);
                        }));
                    }
                    case LONG -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        itemEditors.add(((item, context) -> {
                            item.set((long) mathValue.evaluate(context), (Object[]) currentRoute);
                        }));
                    }
                    case SHORT -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        itemEditors.add(((item, context) -> {
                            item.set((short) mathValue.evaluate(context), (Object[]) currentRoute);
                        }));
                    }
                    case DOUBLE -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        itemEditors.add(((item, context) -> {
                            item.set((double) mathValue.evaluate(context), (Object[]) currentRoute);
                        }));
                    }
                    case FLOAT -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        itemEditors.add(((item, context) -> {
                            item.set((float) mathValue.evaluate(context), (Object[]) currentRoute);
                        }));
                    }
                    case STRING -> {
                        TextValue<Player> textValue = TextValue.auto(pair.right());
                        itemEditors.add(((item, context) -> {
                            item.set(textValue.render(context), (Object[]) currentRoute);
                        }));
                    }
                    case INTARRAY -> {
                        String[] split = splitValue(str);
                        int[] array = Arrays.stream(split).mapToInt(Integer::parseInt).toArray();
                        itemEditors.add(((item, context) -> {
                            item.set(array, (Object[]) currentRoute);
                        }));
                    }
                    case BYTEARRAY -> {
                        String[] split = splitValue(str);
                        byte[] bytes = new byte[split.length];
                        for (int i = 0; i < split.length; i++){
                            bytes[i] = Byte.parseByte(split[i]);
                        }
                        itemEditors.add(((item, context) -> {
                            item.set(bytes, (Object[]) currentRoute);
                        }));
                    }
                }
            } else {
                itemEditors.add(((item, context) -> {
                    item.set(value, (Object[]) currentRoute);
                }));
            }
        }
    }
}
