package net.momirealms.customfishing.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.object.Function;

public class WindowPacketListener extends PacketAdapter {

    private final Function function;

    public WindowPacketListener(Function function) {
        super(CustomFishing.plugin, PacketType.Play.Server.OPEN_WINDOW);
        this.function = function;
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        function.onWindowTitlePacketSend(event.getPacket(), event.getPlayer());
    }
}
