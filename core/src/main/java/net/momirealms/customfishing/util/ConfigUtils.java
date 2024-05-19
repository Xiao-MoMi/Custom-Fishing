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

import net.momirealms.customfishing.api.mechanic.misc.placeholder.BukkitPlaceholderManager;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for configuration-related operations.
 */
public class ConfigUtils {

    private ConfigUtils() {}


    /**
     * Splits a string into a pair of integers using the "~" delimiter.
     *
     * @param value The input string
     * @return A Pair of integers
     */
    public static Pair<Integer, Integer> splitStringIntegerArgs(String value, String regex) {
        String[] split = value.split(regex);
        return Pair.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    /**
     * Converts a list of strings in the format "key:value" into a list of Pairs with keys and doubles.
     *
     * @param list The input list of strings
     * @return A list of Pairs containing keys and doubles
     */
    public static List<Pair<String, Double>> getWeights(List<String> list) {
        List<Pair<String, Double>> result = new ArrayList<>(list.size());
        for (String member : list) {
            String[] split = member.split(":",2);
            String key = split[0];
            result.add(Pair.of(key, Double.parseDouble(split[1])));
        }
        return result;
    }



    /**
     * Retrieves a list of enchantment pairs from a configuration section.
     *
     * @param section The configuration section to extract enchantment data from.
     * @return A list of enchantment pairs.
     */
    @NotNull
    public static List<Pair<String, Short>> getEnchantmentPair(ConfigurationSection section) {
        List<Pair<String, Short>> list = new ArrayList<>();
        if (section == null) return list;
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            if (entry.getValue() instanceof Integer integer) {
                list.add(Pair.of(entry.getKey(), Short.parseShort(String.valueOf(Math.max(1, Math.min(Short.MAX_VALUE, integer))))));
            }
        }
        return list;
    }

    public static List<Pair<Integer, MathValue>> getEnchantAmountPair(ConfigurationSection section) {
        List<Pair<Integer, MathValue>> list = new ArrayList<>();
        if (section == null) return list;
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            list.add(Pair.of(Integer.parseInt(entry.getKey()), getValue(entry.getValue())));
        }
        return list;
    }

    public static List<Pair<Pair<String, Short>, MathValue>> getEnchantPoolPair(ConfigurationSection section) {
        List<Pair<Pair<String, Short>, MathValue>> list = new ArrayList<>();
        if (section == null) return list;
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            list.add(Pair.of(getEnchantmentPair(entry.getKey()), getValue(entry.getValue())));
        }
        return list;
    }

    public static Pair<String, Short> getEnchantmentPair(String value) {
        int last = value.lastIndexOf(":");
        if (last == -1 || last == 0 || last == value.length() - 1) {
            throw new IllegalArgumentException("Invalid format of the input enchantment");
        }
        return Pair.of(value.substring(0, last), Short.parseShort(value.substring(last + 1)));
    }

    /**
     * Retrieves a list of enchantment tuples from a configuration section.
     *
     * @param section The configuration section to extract enchantment data from.
     * @return A list of enchantment tuples.
     */
    @NotNull
    public static List<Tuple<Double, String, Short>> getEnchantmentTuple(ConfigurationSection section) {
        List<Tuple<Double, String, Short>> list = new ArrayList<>();
        if (section == null) return list;
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection inner) {
                Tuple<Double, String, Short> tuple = Tuple.of(
                        inner.getDouble("chance"),
                        inner.getString("enchant"),
                        Short.valueOf(String.valueOf(inner.getInt("level")))
                );
                list.add(tuple);
            }
        }
        return list;
    }

    public static String getString(Object o) {
        if (o instanceof String s) {
            return s;
        } else if (o instanceof Integer i) {
            return String.valueOf(i);
        } else if (o instanceof Double d) {
            return String.valueOf(d);
        }
        throw new IllegalArgumentException("Illegal string format: " + o);
    }

    /**
     * Reads data from a YAML configuration file and creates it if it doesn't exist.
     *
     * @param file The file path
     * @return The YamlConfiguration
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static YamlConfiguration readData(File file) {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.warn("Failed to generate data files!</red>");
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Parses a WeightModifier from a string representation.
     *
     * @param text The input string
     * @return A WeightModifier based on the provided text
     * @throws IllegalArgumentException if the weight format is invalid
     */
    public static WeightModifier getModifier(String text) {
        if (text.length() == 0) {
            throw new IllegalArgumentException("Weight format is invalid.");
        }
        switch (text.charAt(0)) {
            case '/' -> {
                double arg = Double.parseDouble(text.substring(1));
                return (player, weight) -> weight / arg;
            }
            case '*' -> {
                double arg = Double.parseDouble(text.substring(1));
                return (player, weight) -> weight * arg;
            }
            case '-' -> {
                double arg = Double.parseDouble(text.substring(1));
                return (player, weight) -> weight - arg;
            }
            case '%' -> {
                double arg = Double.parseDouble(text.substring(1));
                return (player, weight) -> weight % arg;
            }
            case '+' -> {
                double arg = Double.parseDouble(text.substring(1));
                return (player, weight) -> weight + arg;
            }
            case '=' -> {
                String formula = text.substring(1);
                return (player, weight) -> getExpressionValue(player, formula, Map.of("{0}", String.valueOf(weight)));
            }
            default -> throw new IllegalArgumentException("Invalid weight: " + text);
        }
    }

    public static double getExpressionValue(Player player, String formula, Map<String, String> vars) {
        formula = BukkitPlaceholderManager.getInstance().parse(player, formula, vars);
        return new ExpressionBuilder(formula).build().evaluate();
    }

    public static ArrayList<String> getReadableSection(Map<String, Object> map) {
        ArrayList<String> list = new ArrayList<>();
        mapToReadableStringList(map, list, 0, false);
        return list;
    }

    @SuppressWarnings("unchecked")
    public static void mapToReadableStringList(Map<String, Object> map, List<String> readableList, int loop_times, boolean isMapList) {
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object nbt = entry.getValue();
            if (nbt instanceof List<?> list) {
                if (isMapList && first) {
                    first = false;
                    readableList.add("  ".repeat(loop_times - 1) + "<white>- <gold>" + entry.getKey() + ":");
                } else {
                    readableList.add("  ".repeat(loop_times) + "<gold>" + entry.getKey() + ":");
                }
                for (Object value : list) {
                    if (value instanceof Map<?,?> nbtMap) {
                        mapToReadableStringList((Map<String, Object>) nbtMap, readableList, loop_times + 2, true);
                    } else {
                        readableList.add("  ".repeat(loop_times + 1) + "<white>- " + value);
                    }
                }
            } else if (nbt instanceof Map<?,?> innerMap) {
                if (isMapList && first) {
                    first = false;
                    readableList.add("  ".repeat(loop_times - 1) + "<white>- <gold>" + entry.getKey() + ":");
                } else {
                    readableList.add("  ".repeat(loop_times) + "<gold>" + entry.getKey() + ":");
                }
                mapToReadableStringList((Map<String, Object>) innerMap, readableList, loop_times + 1, false);
            } else {
                if (isMapList && first) {
                    first = false;
                    readableList.add("  ".repeat(loop_times - 1) + "<white>- <gold>" + entry.getKey() + ": <white>" + nbt.toString());
                } else {
                    readableList.add("  ".repeat(loop_times) + "<gold>" + entry.getKey() + ": <white>" + nbt.toString());
                }
            }
        }
    }
}
