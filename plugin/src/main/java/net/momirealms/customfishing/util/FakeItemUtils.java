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
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.collect.Lists;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Utility class for managing fake item entities using PacketContainers.
 */
public class FakeItemUtils {

    private FakeItemUtils() {}

    /**
     * Creates a destroy packet for removing a fake item entity.
     *
     * @param id The ID of the fake item entity to destroy
     * @return The PacketContainer representing the destroy packet
     */
    public static PacketContainer getDestroyPacket(int id) {
        PacketContainer destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        destroyPacket.getIntLists().write(0, List.of(id));
        return destroyPacket;
    }

    /**
     * Creates a spawn packet for a fake item entity at the specified location.
     *
     * @param id       The ID of the fake item entity to spawn
     * @param location The location where the fake item entity should be spawned
     * @return The PacketContainer representing the spawn packet
     */
    public static PacketContainer getSpawnPacket(int id, Location location) {
        PacketContainer entityPacket = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        entityPacket.getModifier().write(0, id);
        entityPacket.getModifier().write(1, UUID.randomUUID());
        entityPacket.getEntityTypeModifier().write(0, EntityType.DROPPED_ITEM);
        entityPacket.getDoubles().write(0, location.getX());
        entityPacket.getDoubles().write(1, location.getY() - 0.5);
        entityPacket.getDoubles().write(2, location.getZ());
        return entityPacket;
    }

    /**
     * Creates a metadata packet for updating the metadata of a fake item entity.
     *
     * @param id         The ID of the fake item entity
     * @param itemStack  The ItemStack to update the metadata with
     * @return The PacketContainer representing the metadata packet
     */
    public static PacketContainer getMetaPacket(int id, ItemStack itemStack) {
        PacketContainer metaPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        metaPacket.getIntegers().write(0, id);
        if (CustomFishingPlugin.getInstance().getVersionManager().isVersionNewerThan1_19_R2()) {
            WrappedDataWatcher wrappedDataWatcher = createDataWatcher(itemStack);
            setValueList(metaPacket, wrappedDataWatcher);
        } else {
            metaPacket.getWatchableCollectionModifier().write(0, createDataWatcher(itemStack).getWatchableObjects());
        }
        return metaPacket;
    }

    /**
     * Creates a teleport packet for moving a fake item entity to the specified location.
     *
     * @param id       The ID of the fake item entity to teleport
     * @param location The location to teleport the fake item entity to
     * @return The PacketContainer representing the teleport packet
     */
    public static PacketContainer getTpPacket(int id, Location location) {
        PacketContainer tpPacket = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
        tpPacket.getModifier().write(0, id);
        tpPacket.getDoubles().write(0, location.getX());
        tpPacket.getDoubles().write(1, location.getY() - 0.5);
        tpPacket.getDoubles().write(2, location.getZ());
        return tpPacket;
    }

    /**
     * Creates a velocity packet for applying velocity to a fake item entity.
     *
     * @param id     The ID of the fake item entity
     * @param vector The velocity vector to apply
     * @return The PacketContainer representing the velocity packet
     */
    public static PacketContainer getVelocityPacket(int id, Vector vector) {
        PacketContainer entityPacket = new PacketContainer(PacketType.Play.Server.ENTITY_VELOCITY);
        entityPacket.getModifier().write(0, id);
        entityPacket.getIntegers().write(1, (int) (vector.getX() * 8000));
        entityPacket.getIntegers().write(2, (int) (vector.getY() * 8000));
        entityPacket.getIntegers().write(3, (int) (vector.getZ() * 8000));
        return entityPacket;
    }

    /**
     * Creates a DataWatcher for a given ItemStack.
     *
     * @param itemStack The ItemStack to create the DataWatcher for
     * @return The created DataWatcher
     */
    public static WrappedDataWatcher createDataWatcher(ItemStack itemStack) {
        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();
        wrappedDataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(8, WrappedDataWatcher.Registry.getItemStackSerializer(false)), itemStack);
        wrappedDataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true);
        return wrappedDataWatcher;
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
}
