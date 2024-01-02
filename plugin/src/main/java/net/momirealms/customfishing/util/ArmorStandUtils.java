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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.customfishing.CustomFishingPluginImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for managing armor stands and sending related packets.
 */
public class ArmorStandUtils {

    private ArmorStandUtils() {}

    /**
     * Creates a destroy packet for removing an armor stand entity.
     *
     * @param id The ID of the armor stand entity to destroy
     * @return The PacketContainer representing the destroy packet
     */
    public static PacketContainer getDestroyPacket(int id) {
        PacketContainer destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        destroyPacket.getIntLists().write(0, List.of(id));
        return destroyPacket;
    }

    /**
     * Creates a spawn packet for an armor stand entity at the specified location.
     *
     * @param id       The ID of the armor stand entity to spawn
     * @param location The location where the armor stand entity should be spawned
     * @return The PacketContainer representing the spawn packet
     */
    public static PacketContainer getSpawnPacket(int id, Location location) {
        PacketContainer entityPacket = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        entityPacket.getModifier().write(0, id);
        entityPacket.getModifier().write(1, UUID.randomUUID());
        entityPacket.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
        entityPacket.getDoubles().write(0, location.getX());
        entityPacket.getDoubles().write(1, location.getY());
        entityPacket.getDoubles().write(2, location.getZ());
        entityPacket.getBytes().write(0, (byte) ((location.getYaw() % 360) * 128 / 180));
        return entityPacket;
    }

    /**
     * Creates a metadata packet for updating the metadata of an armor stand entity.
     *
     * @param id The ID of the armor stand entity
     * @return The PacketContainer representing the metadata packet
     */
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

    /**
     * Sets the value list in a PacketContainer's DataWatcher from a WrappedDataWatcher.
     *
     * @param metaPacket       The PacketContainer representing the metadata packet
     * @param wrappedDataWatcher The WrappedDataWatcher containing the value list
     */
    @SuppressWarnings("DuplicatedCode")
    private static void setValueList(PacketContainer metaPacket, WrappedDataWatcher wrappedDataWatcher) {
        List<WrappedDataValue> wrappedDataValueList = Lists.newArrayList();
        wrappedDataWatcher.getWatchableObjects().stream().filter(Objects::nonNull).forEach(entry -> {
            final WrappedDataWatcher.WrappedDataWatcherObject dataWatcherObject = entry.getWatcherObject();
            wrappedDataValueList.add(new WrappedDataValue(dataWatcherObject.getIndex(), dataWatcherObject.getSerializer(), entry.getRawValue()));
        });
        metaPacket.getDataValueCollectionModifier().write(0, wrappedDataValueList);
    }

    /**
     * Creates a metadata packet for updating the metadata of an armor stand entity with a custom Component.
     *
     * @param id        The ID of the armor stand entity
     * @param component The Component to set as metadata
     * @return The PacketContainer representing the metadata packet
     */
    public static PacketContainer getMetaPacket(int id, Component component) {
        PacketContainer metaPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        metaPacket.getIntegers().write(0, id);
        if (CustomFishingPlugin.get().getVersionManager().isVersionNewerThan1_19_R2()) {
            WrappedDataWatcher wrappedDataWatcher = createDataWatcher(component);
            setValueList(metaPacket, wrappedDataWatcher);
        } else {
            metaPacket.getWatchableCollectionModifier().write(0, createDataWatcher(component).getWatchableObjects());
        }
        return metaPacket;
    }

    /**
     * Creates a DataWatcher for an invisible armor stand entity.
     *
     * @return The created DataWatcher
     */
    public static WrappedDataWatcher createDataWatcher() {
        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();
        WrappedDataWatcher.Serializer serializer1 = WrappedDataWatcher.Registry.get(Boolean.class);
        WrappedDataWatcher.Serializer serializer2 = WrappedDataWatcher.Registry.get(Byte.class);
        wrappedDataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, serializer1), false);
        byte flag = 0x20;
        wrappedDataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, serializer2), flag);
        return wrappedDataWatcher;
    }

    /**
     * Creates a DataWatcher for an invisible armor stand entity with a custom Component.
     *
     * @param component The Component to set in the DataWatcher
     * @return The created DataWatcher
     */
    public static WrappedDataWatcher createDataWatcher(Component component) {
        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();
        WrappedDataWatcher.Serializer serializer1 = WrappedDataWatcher.Registry.get(Boolean.class);
        WrappedDataWatcher.Serializer serializer2 = WrappedDataWatcher.Registry.get(Byte.class);
        wrappedDataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true)), Optional.of(WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(component)).getHandle()));
        wrappedDataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, serializer1), true);
        wrappedDataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, serializer2), (byte) 0x01);
        byte flag = 0x20;
        wrappedDataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, serializer2), flag);
        return wrappedDataWatcher;
    }

    /**
     * Creates an equipment packet for equipping an armor stand with an ItemStack.
     *
     * @param id        The ID of the armor stand entity
     * @param itemStack The ItemStack to equip
     * @return The PacketContainer representing the equipment packet
     */
    public static PacketContainer getEquipPacket(int id, ItemStack itemStack) {
        PacketContainer equipPacket = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
        equipPacket.getIntegers().write(0, id);
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> pairs = new ArrayList<>();
        pairs.add(new Pair<>(EnumWrappers.ItemSlot.HEAD, itemStack));
        equipPacket.getSlotStackPairLists().write(0, pairs);
        return equipPacket;
    }

    /**
     * Sends a fake armor stand entity with item on head to a player at the specified location.
     *
     * @param player    The player to send the entity to
     * @param location  The location where the entity should appear
     * @param itemStack The ItemStack to represent the entity
     * @param seconds      The duration (in seconds) the entity should be displayed
     */
    public static void sendFakeItem(Player player, Location location, ItemStack itemStack, int seconds) {
        int id = new Random().nextInt(Integer.MAX_VALUE);
        CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, getSpawnPacket(id, location.clone().subtract(0,1,0)));
        CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, getMetaPacket(id));
        CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, getEquipPacket(id, itemStack));
        CustomFishingPlugin.get().getScheduler().runTaskAsyncLater(() -> CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, getDestroyPacket(id)), seconds * 50L, TimeUnit.MILLISECONDS);
    }

    /**
     * Sends a hologram (armor stand with custom text) to a player at the specified location.
     *
     * @param player    The player to send the hologram to
     * @param location  The location where the hologram should appear
     * @param component The Component representing the hologram's text
     * @param seconds      The duration (in seconds) the hologram should be displayed
     */
    public static void sendHologram(Player player, Location location, Component component, int seconds) {
        int id = new Random().nextInt(Integer.MAX_VALUE);
        CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, getSpawnPacket(id, location.clone().subtract(0,1,0)));
        CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, getMetaPacket(id, component));
        CustomFishingPlugin.get().getScheduler().runTaskAsyncLater(() -> CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, getDestroyPacket(id)), seconds * 50L, TimeUnit.MILLISECONDS);
    }
}