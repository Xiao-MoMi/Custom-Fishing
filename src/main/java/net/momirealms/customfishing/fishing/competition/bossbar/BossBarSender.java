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

package net.momirealms.customfishing.fishing.competition.bossbar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.competition.Competition;
import net.momirealms.customfishing.object.DynamicText;
import net.momirealms.customfishing.object.Reflection;
import net.momirealms.customfishing.util.AdventureUtils;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BossBarSender {

    private final Player player;
    private int timer_1;
    private int timer_2;
    private int counter;
    private final int size;
    private final DynamicText[] texts;
    private DynamicText text;
    private ScheduledFuture<?> senderTask;
    private final UUID uuid;
    private boolean force;
    private final BossBarConfig config;
    private boolean isShown;
    private boolean hasClaimedJoin;

    public void setText(int position) {
        this.text = texts[position];
        this.force = true;
    }

    public BossBarSender(Player player, BossBarConfig config) {
        String[] str = config.getText();
        this.size = str.length;
        texts = new DynamicText[str.length];
        for (int i = 0; i < str.length; i++) {
            texts[i] = new DynamicText(player, str[i]);
        }
        text = texts[0];
        this.player = player;
        this.uuid = UUID.randomUUID();
        this.config = config;
        this.isShown = false;
    }

    public void show() {
        this.isShown = true;
        CustomFishing.getProtocolManager().sendServerPacket(player, getCreatePacket());
        senderTask = CustomFishing.getInstance().getScheduler().runTaskTimerAsync(() -> {
            if (size != 1) {
                timer_2++;
                if (timer_2 > config.getInterval()) {
                    timer_2 = 0;
                    counter++;
                    if (counter == size) {
                        counter = 0;
                    }
                    setText(counter);
                }
            }
            if (timer_1 < config.getRate()){
                timer_1++;
            } else {
                timer_1 = 0;
                if (text.update() || force) {
                    force = false;
                    CustomFishing.getProtocolManager().sendServerPacket(player, getUpdatePacket());
                    CustomFishing.getProtocolManager().sendServerPacket(player, getProgressPacket());
                }
            }
        }, 50, 50, TimeUnit.MILLISECONDS);
    }

    public boolean isVisible() {
        return this.isShown;
    }

    public BossBarConfig getConfig() {
        return config;
    }

    public void hide() {
        sendRemovePacket();
        if (senderTask != null && !senderTask.isCancelled()) senderTask.cancel(false);
        this.isShown = false;
    }

    private PacketContainer getUpdatePacket() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.BOSS);
        packet.getModifier().write(0, uuid);
        try {
            Object chatComponent = Reflection.iChatComponentMethod.invoke(null, GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize(AdventureUtils.replaceLegacy(text.getLatestValue()))));
            Object updatePacket = Reflection.updateConstructor.newInstance(chatComponent);
            packet.getModifier().write(1, updatePacket);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
        return packet;
    }

    private PacketContainer getProgressPacket() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.BOSS);
        packet.getModifier().write(0, uuid);
        try {
            Object updatePacket = Reflection.progressConstructor.newInstance(Competition.currentCompetition.getProgress());
            packet.getModifier().write(1, updatePacket);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
        return packet;
    }

    private PacketContainer getCreatePacket() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.BOSS);
        packet.getModifier().write(0, uuid);
        InternalStructure internalStructure = packet.getStructures().read(1);
        internalStructure.getChatComponents().write(0, WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize(text.getLatestValue()))));
        internalStructure.getFloat().write(0, Competition.currentCompetition.getProgress());
        internalStructure.getEnumModifier(BarColor.class, 2).write(0, config.getColor());
        internalStructure.getEnumModifier(BossBarOverlay.class, 3).write(0, config.getOverlay());
        internalStructure.getModifier().write(4, false);
        internalStructure.getModifier().write(5, false);
        internalStructure.getModifier().write(6, false);
        return packet;
    }

    private void sendRemovePacket() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.BOSS);
        packet.getModifier().write(0, uuid);
        packet.getModifier().write(1, Reflection.removeBossBarPacket);
        CustomFishing.getProtocolManager().sendServerPacket(player, packet);
    }

    public boolean hasClaimedJoin() {
        return hasClaimedJoin;
    }

    public void setHasClaimedJoinReward(boolean hasClaimedJoin) {
        this.hasClaimedJoin = hasClaimedJoin;
    }
}
