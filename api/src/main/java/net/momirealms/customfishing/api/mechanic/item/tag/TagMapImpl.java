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

package net.momirealms.customfishing.api.mechanic.item.tag;

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.api.mechanic.misc.value.TextValue;
import net.momirealms.customfishing.common.util.Pair;
import org.bukkit.entity.Player;

import java.util.*;

import static net.momirealms.customfishing.api.util.TagUtils.toTypeAndData;
import static net.momirealms.customfishing.common.util.ArrayUtils.splitValue;

@SuppressWarnings("unchecked")
public class TagMapImpl implements TagMap {

    private final Map<String, Object> convertedMap = new HashMap<>();

    public TagMapImpl(Map<String, Object> inputMap) {
        this.analyze(inputMap, convertedMap);
    }

    private void analyze(Map<String, Object> inputMap, Map<String, Object> outPutMap) {
        for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> inner) {
                Map<String, Object> inputInnerMap = (Map<String, Object>) inner;
                HashMap<String, Object> outputInnerMap = new HashMap<>();
                outPutMap.put(key, outputInnerMap);
                analyze(inputInnerMap, outputInnerMap);
            } else if (value instanceof List<?> list) {
                Object first = list.get(0);
                ArrayList<Object> outputList = new ArrayList<>();
                if (first instanceof Map<?, ?>) {
                    for (Object o : list) {
                        Map<String, Object> inputListMap = (Map<String, Object>) o;
                        outputList.add(TagMap.of(inputListMap));
                    }
                } else if (first instanceof String) {
                    for (Object o : list) {
                        String str = (String) o;
                        Pair<TagValueType, String> pair = toTypeAndData(str);
                        switch (pair.left()) {
                            case STRING -> {
                                TextValue<Player> textValue = TextValue.auto(pair.right());
                                outputList.add((ValueProvider) textValue::render);
                            }
                            case BYTE -> {
                                MathValue<Player> mathValue = MathValue.auto(pair.right());
                                outputList.add((ValueProvider) context -> (byte) mathValue.evaluate(context));
                            }
                            case SHORT -> {
                                MathValue<Player> mathValue = MathValue.auto(pair.right());
                                outputList.add((ValueProvider) context -> (short) mathValue.evaluate(context));
                            }
                            case INT -> {
                                MathValue<Player> mathValue = MathValue.auto(pair.right());
                                outputList.add((ValueProvider) context -> (int) mathValue.evaluate(context));
                            }
                            case LONG -> {
                                MathValue<Player> mathValue = MathValue.auto(pair.right());
                                outputList.add((ValueProvider) context -> (long) mathValue.evaluate(context));
                            }
                            case FLOAT -> {
                                MathValue<Player> mathValue = MathValue.auto(pair.right());
                                outputList.add((ValueProvider) context -> (float) mathValue.evaluate(context));
                            }
                            case DOUBLE -> {
                                MathValue<Player> mathValue = MathValue.auto(pair.right());
                                outputList.add((ValueProvider) context -> (double) mathValue.evaluate(context));
                            }
                        }
                    }
                } else {
                    outputList.addAll(list);
                }
                outPutMap.put(key, outputList);
            } else if (value instanceof String str) {
                Pair<TagValueType, String> pair = toTypeAndData(str);
                switch (pair.left()) {
                    case INTARRAY -> {
                        String[] split = splitValue(str);
                        int[] array = Arrays.stream(split).mapToInt(Integer::parseInt).toArray();
                        outPutMap.put(pair.right(), array);
                    }
                    case BYTEARRAY -> {
                        String[] split = splitValue(str);
                        byte[] bytes = new byte[split.length];
                        for (int i = 0; i < split.length; i++){
                            bytes[i] = Byte.parseByte(split[i]);
                        }
                        outPutMap.put(pair.right(), bytes);
                    }
                    case STRING -> {
                        TextValue<Player> textValue = TextValue.auto(pair.right());
                        outPutMap.put(key, (ValueProvider) textValue::render);
                    }
                    case BYTE -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        outPutMap.put(key, (ValueProvider) context -> (byte) mathValue.evaluate(context));
                    }
                    case SHORT -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        outPutMap.put(key, (ValueProvider) context -> (short) mathValue.evaluate(context));
                    }
                    case INT -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        outPutMap.put(key, (ValueProvider) context -> (int) mathValue.evaluate(context));
                    }
                    case LONG -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        outPutMap.put(key, (ValueProvider) context -> (long) mathValue.evaluate(context));
                    }
                    case FLOAT -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        outPutMap.put(key, (ValueProvider) context -> (float) mathValue.evaluate(context));
                    }
                    case DOUBLE -> {
                        MathValue<Player> mathValue = MathValue.auto(pair.right());
                        outPutMap.put(key, (ValueProvider) context -> (double) mathValue.evaluate(context));
                    }
                }
            } else {
                outPutMap.put(key, value);
            }
        }
    }

    @Override
    public Map<String, Object> apply(Context<Player> context) {
        HashMap<String, Object> output = new HashMap<>();
        setMapValue(convertedMap, output, context);
        return output;
    }

    private void setMapValue(Map<String, Object> inputMap, Map<String, Object> outPutMap, Context<Player> context) {
        for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> inner) {
                Map<String, Object> inputInnerMap = (Map<String, Object>) inner;
                HashMap<String, Object> outputInnerMap = new HashMap<>();
                outPutMap.put(key, outputInnerMap);
                setMapValue(inputInnerMap, outputInnerMap, context);
            } else if (value instanceof List<?> list) {
                ArrayList<Object> convertedList = new ArrayList<>();
                Object first = list.get(0);
                if (first instanceof TagMap) {
                    for (Object o : list) {
                        TagMap map = (TagMap) o;
                        convertedList.add(map.apply(context));
                    }
                } else if (first instanceof ValueProvider) {
                    for (Object o : list) {
                        ValueProvider pd = (ValueProvider) o;
                        convertedList.add(pd.apply(context));
                    }
                } else {
                    convertedList.addAll(list);
                }
                outPutMap.put(key, convertedList);
            } else if (value instanceof ValueProvider provider) {
                outPutMap.put(key, provider.apply(context));
            } else {
                outPutMap.put(key, value);
            }
        }
    }

    @FunctionalInterface
    public interface ValueProvider {
        Object apply(Context<Player> context);
    }
}
