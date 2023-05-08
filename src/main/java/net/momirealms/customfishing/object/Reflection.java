package net.momirealms.customfishing.object;

import net.momirealms.customfishing.CustomFishing;

public class Reflection {

    public static Class<?> textComponentClass;
    public static Class<?> componentClass;
    public static Class<?> bukkitClass;
    public static Class<?> textColorClass;
    public static Class<?> keyClass;
    public static Class<?> textDecorationClass;
    public static Class<?> textDecorationStateClass;

    public static void load() {
        if (CustomFishing.getInstance().getVersionHelper().isSpigot()) return;
        try {
            textComponentClass = Class.forName("net;kyori;adventure;text;TextComponent".replace(";", "."));
            componentClass = Class.forName("net;kyori;adventure;text;Component".replace(";", "."));
            bukkitClass = Class.forName("org;bukkit;Bukkit".replace(";", "."));
            textColorClass = Class.forName("net;kyori;adventure;text;format;TextColor".replace(";", "."));
            keyClass = Class.forName("net;kyori;adventure;key;Key".replace(";", "."));
            textDecorationClass = Class.forName("net;kyori;adventure;text;format;TextDecoration".replace(";", "."));
            textDecorationStateClass = Class.forName("net;kyori;adventure;text;format;TextDecoration$State".replace(";", "."));
        } catch (ClassNotFoundException ignored) {
        }
    }
}