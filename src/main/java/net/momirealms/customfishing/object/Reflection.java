package net.momirealms.customfishing.object;

import com.comphenix.protocol.utility.MinecraftReflection;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.util.AdventureUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Reflection {

    public static Class<?> textComponentClass;
    public static Class<?> componentClass;
    public static Class<?> bukkitClass;
    public static Class<?> textColorClass;
    public static Class<?> keyClass;
    public static Class<?> textDecorationClass;
    public static Class<?> textDecorationStateClass;
    public static Object removeBossBarPacket;
    public static Constructor<?> progressConstructor;
    public static Constructor<?> updateConstructor;
    public static Method iChatComponentMethod;

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

            Class<?> bar = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutBoss");
            Field remove = bar.getDeclaredField("f");
            remove.setAccessible(true);
            removeBossBarPacket = remove.get(null);

            Class<?> packetBossClassF = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutBoss$f");
            progressConstructor = packetBossClassF.getDeclaredConstructor(float.class);
            progressConstructor.setAccessible(true);

            Class<?> packetBossClassE = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutBoss$e");
            updateConstructor = packetBossClassE.getDeclaredConstructor(MinecraftReflection.getIChatBaseComponentClass());
            updateConstructor.setAccessible(true);

            iChatComponentMethod = MinecraftReflection.getChatSerializerClass().getMethod("a", String.class);
            iChatComponentMethod.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException ignored) {
            AdventureUtils.consoleMessage("<red>[CustomFishing] Error occurred when loading reflections");
        }
    }
}