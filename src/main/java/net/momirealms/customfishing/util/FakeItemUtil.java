package net.momirealms.customfishing.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.collect.Lists;
import net.momirealms.customfishing.CustomFishing;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class FakeItemUtil {

    public static PacketContainer getDestroyPacket(int id) {
        PacketContainer destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        destroyPacket.getIntLists().write(0, List.of(id));
        return destroyPacket;
    }

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

    public static PacketContainer getMetaPacket(int id, ItemStack itemStack) {
        PacketContainer metaPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        metaPacket.getIntegers().write(0, id);
        if (CustomFishing.version.equals("v1_19_R2")) {
            WrappedDataWatcher wrappedDataWatcher = createDataWatcher(itemStack);
            List<WrappedDataValue> wrappedDataValueList = Lists.newArrayList();
            wrappedDataWatcher.getWatchableObjects().stream().filter(Objects::nonNull).forEach(entry -> {
                final WrappedDataWatcher.WrappedDataWatcherObject dataWatcherObject = entry.getWatcherObject();
                wrappedDataValueList.add(new WrappedDataValue(dataWatcherObject.getIndex(), dataWatcherObject.getSerializer(), entry.getRawValue()));
            });
            metaPacket.getDataValueCollectionModifier().write(0, wrappedDataValueList);
        } else {
            metaPacket.getWatchableCollectionModifier().write(0, createDataWatcher(itemStack).getWatchableObjects());
        }
        return metaPacket;
    }

    public static PacketContainer getTpPacket(int id, Location location) {
        PacketContainer tpPacket = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
        tpPacket.getModifier().write(0, id);
        tpPacket.getDoubles().write(0, location.getX());
        tpPacket.getDoubles().write(1, location.getY() - 0.5);
        tpPacket.getDoubles().write(2, location.getZ());
        return tpPacket;
    }

    public static PacketContainer getVelocity(int id, Vector vector) {
        PacketContainer entityPacket = new PacketContainer(PacketType.Play.Server.ENTITY_VELOCITY);
        entityPacket.getModifier().write(0, id);
        entityPacket.getIntegers().write(1, (int) (vector.getX() * 8000));
        entityPacket.getIntegers().write(2, (int) (vector.getY() * 8000));
        entityPacket.getIntegers().write(3, (int) (vector.getZ() * 8000));
        return entityPacket;
    }

    public static WrappedDataWatcher createDataWatcher(ItemStack itemStack) {
        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();
        wrappedDataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(8, WrappedDataWatcher.Registry.getItemStackSerializer(false)), itemStack);
        wrappedDataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true);
        return wrappedDataWatcher;
    }
}
