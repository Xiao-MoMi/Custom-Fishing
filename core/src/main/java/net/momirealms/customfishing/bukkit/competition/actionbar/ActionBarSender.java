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

package net.momirealms.customfishing.bukkit.competition.actionbar;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.competition.info.ActionBarConfigImpl;
import net.momirealms.customfishing.api.scheduler.CancellableTask;
import net.momirealms.customfishing.bukkit.competition.Competition;
import net.momirealms.customfishing.mechanic.misc.DynamicText;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Manages and updates ActionBar messages for a specific player in a competition context.
 */
public class ActionBarSender {

    private final Player player;
    private int refreshTimer;
    private int switchTimer;
    private int counter;
    private final DynamicText[] texts;
    private CancellableTask senderTask;
    private final ActionBarConfigImpl config;
    private boolean isShown;
    private final Competition competition;
    private final HashMap<String, String> privatePlaceholders;

    /**
     * Creates a new ActionBarSender instance for a player.
     *
     * @param player      The player to manage ActionBar messages for.
     * @param config      The configuration for ActionBar messages.
     * @param competition The competition associated with this ActionBarSender.
     */
    public ActionBarSender(Player player, ActionBarConfigImpl config, Competition competition) {
        this.player = player;
        this.config = config;
        this.isShown = false;
        this.competition = competition;
        this.privatePlaceholders = new HashMap<>();
        this.privatePlaceholders.put("{player}", player.getName());
        this.updatePrivatePlaceholders();

        String[] str = config.texts();
        texts = new DynamicText[str.length];
        for (int i = 0; i < str.length; i++) {
            texts[i] = new DynamicText(player, str[i]);
            texts[i].update(privatePlaceholders);
        }
    }

    /**
     * Updates private placeholders used in ActionBar messages.
     */
    @SuppressWarnings("DuplicatedCode")
    private void updatePrivatePlaceholders() {
        this.privatePlaceholders.put("{score}", String.format("%.2f", competition.getRanking().getPlayerScore(player.getName())));
        int rank = competition.getRanking().getPlayerRank(player.getName());
        this.privatePlaceholders.put("{rank}", rank != -1 ? String.valueOf(rank) : CFLocale.MSG_No_Rank);
        this.privatePlaceholders.putAll(competition.getCachedPlaceholders());
    }

    /**
     * Shows the ActionBar message to the player.
     */
    public void show() {
        this.isShown = true;
        senderTask = BukkitCustomFishingPlugin.get().getScheduler().runTaskAsyncTimer(() -> {
            switchTimer++;
            if (switchTimer > config.switchInterval()) {
                switchTimer = 0;
                counter++;
            }
            if (refreshTimer < config.refreshRate()){
                refreshTimer++;
            } else {
                refreshTimer = 0;
                DynamicText text = texts[counter % (texts.length)];
                updatePrivatePlaceholders();
                text.update(privatePlaceholders);
                AdventureHelper.getInstance().sendActionbar(
                        player,
                        text.getLatestValue()
                );
            }
        }, 50, 50, TimeUnit.MILLISECONDS);
    }

    /**
     * Hides the ActionBar message from the player.
     */
    public void hide() {
        if (senderTask != null && !senderTask.isCancelled())
            senderTask.cancel();
        this.isShown = false;
    }

    /**
     * Checks if the ActionBar message is currently visible to the player.
     *
     * @return True if the ActionBar message is visible, false otherwise.
     */
    public boolean isVisible() {
        return this.isShown;
    }

    /**
     * Gets the ActionBar configuration.
     *
     * @return The ActionBar configuration.
     */
    public ActionBarConfigImpl getConfig() {
        return config;
    }
}
