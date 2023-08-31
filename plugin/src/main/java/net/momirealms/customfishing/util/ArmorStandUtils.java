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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import com.google.common.collect.Lists;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.customfishing.CustomFishingPluginImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ArmorStandUtils {

    public static PacketContainer getDestroyPacket(int id) {
        PacketContainer destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        destroyPacket.getIntLists().write(0, List.of(id));
        return destroyPacket;
    }

    public static PacketContainer getSpawnPacket(int id, Location location) {
        PacketContainer entityPacket = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        entityPacket.getModifier().write(0, id);
        entityPacket.getModifier().write(1, UUID.randomUUID());
        entityPacket.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
        entityPacket.getDoubles().write(0, location.getX());
        entityPacket.getDoubles().write(1, location.getY());
        entityPacket.getDoubles().write(2, location.getZ());
        return entityPacket;
    }

    public static PacketContainer getMetaPacket(int id) {
        PacketContainer metaPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        metaPacket.getIntegers().write(0, id);
        if (CustomFishingPlugin.get().getVersionManager().isVersionNewerThan1_19_R2()) {
            WrappedDataWatcher wrappedDataWatcher = createDataWatcher();
            setValueList(metaPacket, wrappedDataWatcher);
        } else {
            metaPacket.getWatchableCollectionModifier().write(0, createDataWatcher().getWatchableObjects());
        }
        return metaPacket;
    }

    private static void setValueList(PacketContainer metaPacket, WrappedDataWatcher wrappedDataWatcher) {
        List<WrappedDataValue> wrappedDataValueList = Lists.newArrayList();
        wrappedDataWatcher.getWatchableObjects().stream().filter(Objects::nonNull).forEach(entry -> {
            final WrappedDataWatcher.WrappedDataWatcherObject dataWatcherObject = entry.getWatcherObject();
            wrappedDataValueList.add(new WrappedDataValue(dataWatcherObject.getIndex(), dataWatcherObject.getSerializer(), entry.getRawValue()));
        });
        metaPacket.getDataValueCollectionModifier().write(0, wrappedDataValueList);
    }

    public static PacketContainer getMetaPacket(int id, String text) {
        PacketContainer metaPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        metaPacket.getIntegers().write(0, id);
        if (CustomFishingPlugin.get().getVersionManager().isVersionNewerThan1_19_R2()) {
            WrappedDataWatcher wrappedDataWatcher = createDataWatcher(text);
            setValueList(metaPacket, wrappedDataWatcher);
        } else {
            metaPacket.getWatchableCollectionModifier().write(0, createDataWatcher(text).getWatchableObjects());
        }
        return metaPacket;
    }

    public static WrappedDataWatcher createDataWatcher() {
        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();
        WrappedDataWatcher.Serializer serializer1 = WrappedDataWatcher.Registry.get(Boolean.class);
        WrappedDataWatcher.Serializer serializer2 = WrappedDataWatcher.Registry.get(Byte.class);
        wrappedDataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, serializer1), false);
        byte flag = 0x20;
        wrappedDataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, serializer2), flag);
        return wrappedDataWatcher;
    }

    public static WrappedDataWatcher createDataWatcher(String text) {
        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();
        WrappedDataWatcher.Serializer serializer1 = WrappedDataWatcher.Registry.get(Boolean.class);
        WrappedDataWatcher.Serializer serializer2 = WrappedDataWatcher.Registry.get(Byte.class);
        wrappedDataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true)), Optional.of(WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize(text))).getHandle()));
        wrappedDataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, serializer1), true);
        wrappedDataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, serializer2), (byte) 0x01);
        byte flag = 0x20;
        wrappedDataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, serializer2), flag);
        return wrappedDataWatcher;
    }

    public static PacketContainer getEquipPacket(int id, ItemStack itemStack) {
        PacketContainer equipPacket = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
        equipPacket.getIntegers().write(0, id);
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> pairs = new ArrayList<>();
        pairs.add(new Pair<>(EnumWrappers.ItemSlot.HEAD, itemStack));
        equipPacket.getSlotStackPairLists().write(0, pairs);
        return equipPacket;
    }

    public static void sendAnimationToPlayer(Player player, Location location, ItemStack itemStack, int time) {
        int id = new Random().nextInt(Integer.MAX_VALUE);
        CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, getSpawnPacket(id, location.clone().subtract(0,1,0)));
        CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, getMetaPacket(id));
        CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, getEquipPacket(id, itemStack));
        CustomFishingPlugin.get().getScheduler().runTaskAsyncLater(() -> CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, getDestroyPacket(id)), time * 50L, TimeUnit.MILLISECONDS);
    }
}