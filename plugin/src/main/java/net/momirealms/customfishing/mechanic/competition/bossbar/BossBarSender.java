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

package net.momirealms.customfishing.mechanic.competition.bossbar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.customfishing.CustomFishingPluginImpl;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.competition.BossBarConfig;
import net.momirealms.customfishing.api.scheduler.CancellableTask;
import net.momirealms.customfishing.api.util.ReflectionUtils;
import net.momirealms.customfishing.mechanic.competition.Competition;
import net.momirealms.customfishing.mechanic.misc.DynamicText;
import net.momirealms.customfishing.setting.CFLocale;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Manages and updates boss bars for a specific player in a competition context.
 */
public class BossBarSender {

    private final Player player;
    private int refreshTimer;
    private int switchTimer;
    private int counter;
    private final DynamicText[] texts;
    private CancellableTask senderTask;
    private final UUID uuid;
    private final BossBarConfig config;
    private boolean isShown;
    private final Competition competition;
    private final HashMap<String, String> privatePlaceholders;

    /**
     * Creates a new BossBarSender instance for a player.
     *
     * @param player      The player to manage the boss bar for.
     * @param config      The configuration for the boss bar.
     * @param competition The competition associated with this boss bar.
     */
    public BossBarSender(Player player, BossBarConfig config, Competition competition) {
        this.player = player;
        this.uuid = UUID.randomUUID();
        this.config = config;
        this.isShown = false;
        this.competition = competition;
        this.privatePlaceholders = new HashMap<>();
        this.privatePlaceholders.put("{player}", player.getName());
        this.updatePrivatePlaceholders();

        String[] str = config.getTexts();
        texts = new DynamicText[str.length];
        for (int i = 0; i < str.length; i++) {
            texts[i] = new DynamicText(player, str[i]);
            texts[i].update(privatePlaceholders);
        }
    }

    /**
     * Updates private placeholders used in boss bar messages.
     */
    @SuppressWarnings("DuplicatedCode")
    private void updatePrivatePlaceholders() {
        this.privatePlaceholders.put("{score}", String.format("%.2f", competition.getRanking().getPlayerScore(player.getName())));
        int rank = competition.getRanking().getPlayerRank(player.getName());
        this.privatePlaceholders.put("{rank}", rank != -1 ? String.valueOf(rank) : CFLocale.MSG_No_Rank);
        this.privatePlaceholders.putAll(competition.getCachedPlaceholders());
    }

    /**
     * Shows the boss bar to the player.
     */
    public void show() {
        this.isShown = true;
        CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, getCreatePacket());
        senderTask = CustomFishingPlugin.get().getScheduler().runTaskAsyncTimer(() -> {
            switchTimer++;
            if (switchTimer > config.getSwitchInterval()) {
                switchTimer = 0;
                counter++;
            }
            if (refreshTimer < config.getRefreshRate()){
                refreshTimer++;
            } else {
                refreshTimer = 0;
                DynamicText text = texts[counter % (texts.length)];
                updatePrivatePlaceholders();
                if (text.update(privatePlaceholders)) {
                    CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, getUpdatePacket(text));
                }
                CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, getProgressPacket());
            }
        }, 50, 50, TimeUnit.MILLISECONDS);
    }

    /**
     * Checks if the boss bar is currently visible to the player.
     *
     * @return True if the boss bar is visible, false otherwise.
     */
    public boolean isVisible() {
        return this.isShown;
    }

    /**
     * Gets the boss bar configuration.
     *
     * @return The boss bar configuration.
     */
    public BossBarConfig getConfig() {
        return config;
    }

    /**
     * Hides the boss bar from the player.
     */
    public void hide() {
        CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, getRemovePacket());
        if (senderTask != null && !senderTask.isCancelled()) senderTask.cancel();
        this.isShown = false;
    }

    private PacketContainer getUpdatePacket(DynamicText text) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.BOSS);
        packet.getModifier().write(0, uuid);
        try {
            Object chatComponent = ReflectionUtils.iChatComponentMethod.invoke(null,
            GsonComponentSerializer.gson().serialize(
            AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
            text.getLatestValue()
            )));
            Object updatePacket = ReflectionUtils.updateConstructor.newInstance(chatComponent);
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
            Object updatePacket = ReflectionUtils.progressConstructor.newInstance(competition.getProgress());
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
        internalStructure.getChatComponents().write(0, WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize(texts[0].getLatestValue()))));
        internalStructure.getFloat().write(0, competition.getProgress());
        internalStructure.getEnumModifier(BarColor.class, 2).write(0, config.getColor());
        internalStructure.getEnumModifier(BossBarConfig.Overlay.class, 3).write(0, config.getOverlay());
        internalStructure.getModifier().write(4, false);
        internalStructure.getModifier().write(5, false);
        internalStructure.getModifier().write(6, false);
        return packet;
    }

    private PacketContainer getRemovePacket() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.BOSS);
        packet.getModifier().write(0, uuid);
        packet.getModifier().write(1, ReflectionUtils.removeBossBarPacket);
        return packet;
    }
}
