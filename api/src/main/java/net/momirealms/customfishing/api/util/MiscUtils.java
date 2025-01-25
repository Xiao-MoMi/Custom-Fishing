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

package net.momirealms.customfishing.api.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiscUtils {

    public static List<String> getAsStringList(Object o) {
        List<String> list = new ArrayList<>();
        if (o instanceof List<?>) {
            for (Object object : (List<?>) o) {
                list.add(object.toString());
            }
        } else if (o instanceof String) {
            list.add((String) o);
        } else {
            list.add(o.toString());
        }
        return list;
    }

    public static List<Float> getAsFloatList(Object o) {
        List<Float> list = new ArrayList<>();
        if (o instanceof List<?>) {
            for (Object object : (List<?>) o) {
                if (object instanceof Number) {
                    list.add(((Number) object).floatValue());
                } else if (object instanceof String) {
                    try {
                        list.add(Float.parseFloat((String) object));
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Cannot convert " + object + " to float");
                    }
                } else {
                    throw new RuntimeException("Cannot convert " + object + " to float");
                }
            }
        } else if (o instanceof Float) {
            list.add((Float) o);
        } else if (o instanceof String) {
            try {
                list.add(Float.parseFloat((String) o));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Cannot convert " + o + " to float");
            }
        } else {
            throw new RuntimeException("Cannot convert " + o + " to float");
        }
        return list;
    }

    public static int getAsInt(Object o) {
        if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof String) {
            try {
                return Integer.parseInt((String) o);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Cannot convert " + o + " to int");
            }
        } else if (o instanceof Boolean) {
            return (Boolean) o ? 1 : 0;
        } else if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        throw new RuntimeException("Cannot convert " + o + " to int");
    }

    public static double getAsDouble(Object o) {
        if (o instanceof Double) {
            return (Double) o;
        } else if (o instanceof String) {
            try {
                return Double.parseDouble((String) o);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Cannot convert " + o + " to double");
            }
        } else if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        throw new RuntimeException("Cannot convert " + o + " to double");
    }

    public static float getAsFloat(Object o) {
        if (o instanceof Float) {
            return (Float) o;
        } else if (o instanceof String) {
            try {
                return Float.parseFloat((String) o);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Cannot convert " + o + " to float");
            }
        } else if (o instanceof Number) {
            return ((Number) o).floatValue();
        }
        throw new RuntimeException("Cannot convert " + o + " to float");
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> deepCopyList(List<T> originalList, Map<String, String> replacements) {
        List<T> copiedList = new ArrayList<>();
        for (T item : originalList) {
            if (item instanceof List) {
                copiedList.add((T) deepCopyList((List<?>) item, replacements));
            } else if (item instanceof Map) {
                copiedList.add((T) deepCopyMap((Map<String, Object>) item, replacements));
            } else if (item instanceof String s) {
                copiedList.add((T) replacements.getOrDefault(s, s));
            } else {
                copiedList.add(item);
            }
        }
        return copiedList;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> deepCopyMap(Map<String, Object> originalMap, Map<String, String> replacements) {
        Map<String, Object> copiedMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : originalMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof List) {
                copiedMap.put(key, deepCopyList((List<?>) value, replacements));
            } else if (value instanceof Map) {
                copiedMap.put(key, deepCopyMap((Map<String, Object>) value, replacements));
            } else if (value instanceof String s) {
                copiedMap.put(key, replacements.getOrDefault(s, s));
            } else {
                copiedMap.put(key, value);
            }
        }
        return copiedMap;
    }
}
