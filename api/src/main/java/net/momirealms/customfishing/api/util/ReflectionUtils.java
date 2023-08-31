package net.momirealms.customfishing.api.util;

import com.comphenix.protocol.utility.MinecraftReflection;
import net.momirealms.customfishing.api.CustomFishingPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtils {

    public static Object removeBossBarPacket;
    public static Constructor<?> progressConstructor;
    public static Constructor<?> updateConstructor;
    public static Method iChatComponentMethod;
    public static Method gsonDeserializeMethod;
    public static Object gsonInstance;
    public static Class<?> componentClass;
    public static Class<?> bukkitClass;

    public static void load() {
        try {
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
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException exception) {
            LogUtils.severe("Error occurred when loading reflections", exception);
            exception.printStackTrace();
            return;
        }
        if (CustomFishingPlugin.get().getVersionManager().isSpigot()) return;
        try {
            componentClass = Class.forName("net;kyori;adventure;text;Component".replace(";", "."));
            bukkitClass = Class.forName("org;bukkit;Bukkit".replace(";", "."));
            Class<?> gsonComponentSerializerClass = Class.forName("net;kyori;adventure;text;serializer;gson;GsonComponentSerializer".replace(";", "."));
            Class<?> gsonComponentSerializerImplClass = Class.forName("net;kyori;adventure;text;serializer;gson;GsonComponentSerializerImpl".replace(";", "."));
            Method gsonMethod = gsonComponentSerializerClass.getMethod("gson");
            gsonInstance = gsonMethod.invoke(null);
            gsonDeserializeMethod = gsonComponentSerializerImplClass.getMethod("deserialize", String.class);
            gsonDeserializeMethod.setAccessible(true);
        } catch (ClassNotFoundException exception) {
            LogUtils.severe("Error occurred when loading reflections", exception);
            exception.printStackTrace();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}