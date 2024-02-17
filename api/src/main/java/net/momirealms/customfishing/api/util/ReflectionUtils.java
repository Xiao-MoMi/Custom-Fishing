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
        // spigot map
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
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException e1) {
            // mojmap
            try {
                Class<?> bar = Class.forName("net.minecraft.network.protocol.game.ClientboundBossEventPacket");
                Field remove = bar.getDeclaredField("REMOVE_OPERATION");
                remove.setAccessible(true);
                removeBossBarPacket = remove.get(null);
                Class<?> packetBossClassF = Class.forName("net.minecraft.network.protocol.game.ClientboundBossEventPacket$UpdateProgressOperation");
                progressConstructor = packetBossClassF.getDeclaredConstructor(float.class);
                progressConstructor.setAccessible(true);
                Class<?> packetBossClassE = Class.forName("net.minecraft.network.protocol.game.ClientboundBossEventPacket$UpdateNameOperation");
                updateConstructor = packetBossClassE.getDeclaredConstructor(MinecraftReflection.getIChatBaseComponentClass());
                updateConstructor.setAccessible(true);
                iChatComponentMethod = MinecraftReflection.getChatSerializerClass().getMethod("fromJson", String.class);
                iChatComponentMethod.setAccessible(true);
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException e2) {
                LogUtils.severe("Error occurred when loading reflections", e2);
                e2.printStackTrace();
            }
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