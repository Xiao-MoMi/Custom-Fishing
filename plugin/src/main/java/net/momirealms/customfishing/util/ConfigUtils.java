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
 *
 */

package net.momirealms.customfishing.util;

import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.mechanic.loot.Modifier;
import net.momirealms.customfishing.api.util.LogUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConfigUtils {

    @SuppressWarnings("unchecked")
    public static ArrayList<String> stringListArgs(Object object) {
        ArrayList<String> list = new ArrayList<>();
        if (object instanceof String member) {
            list.add(member);
        } else if (object instanceof List<?> members) {
            list.addAll((Collection<? extends String>) members);
        }
        return list;
    }

    public static Pair<Integer, Integer> splitStringIntegerArgs(String value) {
        String[] split = value.split("~");
        return Pair.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    public static List<Pair<String, Double>> getWeights(List<String> list) {
        List<Pair<String, Double>> result = new ArrayList<>(list.size());
        for (String member : list) {
            String[] split = member.split(":",2);
            String key = split[0];
            result.add(Pair.of(key, Double.parseDouble(split[1])));
        }
        return result;
    }

    public static double getDoubleValue(Object arg) {
        if (arg instanceof Double d) {
            return d;
        } else if (arg instanceof Integer i) {
            return Double.valueOf(i);
        }
        return 0;
    }

    public static List<Pair<String, Modifier>> getModifiers(List<String> modList) {
        List<Pair<String, Modifier>> result = new ArrayList<>(modList.size());
        for (String member : modList) {
            String[] split = member.split(":",2);
            String key = split[0];
            result.add(Pair.of(key, getModifier(split[1])));
        }
        return result;
    }

    /**
     * Create a data file if not exists
     * @param file file path
     * @return yaml data
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

    public static Modifier getModifier(String text) {
        if (text.length() == 0) {
            throw new IllegalArgumentException("Weight format is invalid.");
        }
        switch (text.charAt(0)) {
            case '/' -> {
                double arg = Double.parseDouble(text.substring(1));
                return weight -> weight / arg;
            }
            case '*' -> {
                double arg = Double.parseDouble(text.substring(1));
                return weight -> weight * arg;
            }
            case '-' -> {
                double arg = Double.parseDouble(text.substring(1));
                return weight -> weight - arg;
            }
            case '%' -> {
                double arg = Double.parseDouble(text.substring(1));
                return weight -> weight % arg;
            }
            case '+' -> {
                double arg = Double.parseDouble(text.substring(1));
                return weight -> weight + arg;
            }
            default -> throw new IllegalArgumentException("Invalid weight: " + text);
        }
    }
}
