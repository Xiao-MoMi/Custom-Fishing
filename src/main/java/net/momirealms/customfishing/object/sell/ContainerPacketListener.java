package net.momirealms.customfishing.object.sell;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.integration.papi.PlaceholderManager;
import net.momirealms.customfishing.manager.SellManager;
import net.momirealms.customfishing.util.ItemStackUtil;
import org.bukkit.entity.Player;

public class ContainerPacketListener extends PacketAdapter {

    public ContainerPacketListener() {
        super(CustomFishing.plugin, PacketType.Play.Server.OPEN_WINDOW);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
        WrappedChatComponent component = wrappedChatComponentStructureModifier.getValues().get(0);
        if (component.getJson().equals("{\"text\":\"{CustomFishing}\"}")) {
            PlaceholderManager placeholderManager = CustomFishing.plugin.getIntegrationManager().getPlaceholderManager();
            Player player = event.getPlayer();
            String text = SellManager.title.replace("{player}", player.getName());
            if (placeholderManager != null) placeholderManager.parse(player, text);
            wrappedChatComponentStructureModifier.write(0,
                WrappedChatComponent.fromJson(
                    GsonComponentSerializer.gson().serialize(
                        MiniMessage.miniMessage().deserialize(
                                ItemStackUtil.replaceLegacy(text)
                        )
                    )
                )
            );
        }
    }
}
