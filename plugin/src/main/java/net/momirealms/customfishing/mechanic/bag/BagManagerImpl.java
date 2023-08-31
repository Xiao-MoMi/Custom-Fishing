package net.momirealms.customfishing.mechanic.bag;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ScoreComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.customfishing.CustomFishingPluginImpl;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.manager.BagManager;
import net.momirealms.customfishing.api.mechanic.bag.FishingBagHolder;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.compatibility.papi.PlaceholderManagerImpl;
import net.momirealms.customfishing.setting.Config;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BagManagerImpl implements BagManager {

    private final CustomFishingPlugin plugin;
    private final ConcurrentHashMap<UUID, FishingBagHolder> bagMap;
    private final WindowPacketListener windowPacketListener;

    public BagManagerImpl(CustomFishingPluginImpl plugin) {
        this.plugin = plugin;
        this.bagMap = new ConcurrentHashMap<>();
        this.windowPacketListener = new WindowPacketListener();
    }

    @Override
    public boolean isBagEnabled() {
        return Config.enableFishingBag;
    }

    public void load() {
        CustomFishingPluginImpl.getProtocolManager().addPacketListener(windowPacketListener);
    }

    public void unload() {
        CustomFishingPluginImpl.getProtocolManager().removePacketListener(windowPacketListener);
    }

    public void disable() {
        unload();
    }

    @Override
    public Inventory getOnlineBagInventory(UUID uuid) {
        var onlinePlayer = plugin.getStorageManager().getOnlineUser(uuid);
        if (onlinePlayer == null) {
            LogUtils.warn("Player " + uuid + "'s bag data is not loaded.");
            return null;
        }
        return onlinePlayer.getHolder().getInventory();
    }

    public static class WindowPacketListener extends PacketAdapter {

        public WindowPacketListener() {
            super(CustomFishingPlugin.getInstance(), PacketType.Play.Server.OPEN_WINDOW);
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            final PacketContainer packet = event.getPacket();
            StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
            WrappedChatComponent component = wrappedChatComponentStructureModifier.getValues().get(0);
            String windowTitleJson = component.getJson();
            Component titleComponent = GsonComponentSerializer.gson().deserialize(windowTitleJson);
            if (titleComponent instanceof ScoreComponent scoreComponent && scoreComponent.name().equals("bag")) {
                HashMap<String, String> placeholders = new HashMap<>();
                String uuidStr = scoreComponent.objective();
                UUID uuid = UUID.fromString(uuidStr);
                placeholders.put("{uuid}", uuidStr);
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                placeholders.put("{player}", Optional.ofNullable(offlinePlayer.getName()).orElse(uuidStr));
                wrappedChatComponentStructureModifier.write(0,
                WrappedChatComponent.fromJson(
                GsonComponentSerializer.gson().serialize(
                AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                PlaceholderManagerImpl.getInstance().parse(offlinePlayer, Config.bagTitle, placeholders)
                ))));
            }
        }
    }
}
