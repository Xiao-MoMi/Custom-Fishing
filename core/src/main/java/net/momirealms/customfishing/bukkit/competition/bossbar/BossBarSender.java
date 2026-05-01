/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.bukkit.competition.bossbar;

import net.kyori.adventure.bossbar.BossBar;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.competition.info.BossBarConfig;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.misc.value.DynamicText;
import net.momirealms.customfishing.bukkit.competition.Competition;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.locale.MessageConstants;
import net.momirealms.customfishing.common.locale.TranslationManager;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import net.momirealms.sparrow.heart.SparrowHeart;
import net.momirealms.sparrow.heart.feature.bossbar.BossBarColor;
import net.momirealms.sparrow.heart.feature.bossbar.BossBarOverlay;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BossBarSender {
    private final Player player;
    private int refreshTimer;
    private int switchTimer;
    private int counter;
    private final DynamicText[] texts;
    private SchedulerTask senderTask;
    private final BossBar bossBar;
    private final BossBarConfig config;
    private boolean isShown;
    private final Competition competition;
    private final Context<Player> privateContext;
    private final UUID uuid;

    public BossBarSender(Player player, BossBarConfig config, Competition competition) {
        this.player = player;
        this.config = config;
        this.isShown = false;
        this.competition = competition;
        this.privateContext = Context.player(player);
        this.updatePrivatePlaceholders();
        String[] str = config.texts();
        this.texts = new DynamicText[str.length];
        for (int i = 0; i < str.length; i++) {
            this.texts[i] = new DynamicText(player, str[i]);
            this.texts[i].update(privateContext.placeholderMap());
        }
        bossBar = BossBar.bossBar(
                AdventureHelper.miniMessage(texts[0].getLatestValue()),
                competition.getProgress(),
                config.color(),
                config.overlay(),
                Set.of()
        );

        this.uuid = UUID.randomUUID();
    }

    @SuppressWarnings("DuplicatedCode")
    private void updatePrivatePlaceholders() {
        double score = competition.getRanking().getPlayerScore(player.getName());
        this.privateContext.arg(ContextKeys.SCORE_FORMATTED, score == 0 ? TranslationManager.miniMessageTranslation(MessageConstants.COMPETITION_NO_SCORE.build().key()) : String.format("%.2f", competition.getGoal().isReversed() ? -score : score));
        this.privateContext.arg(ContextKeys.SCORE, competition.getGoal().isReversed() ? -score : score);
        int rank = competition.getRanking().getPlayerRank(player.getName());
        this.privateContext.arg(ContextKeys.RANK, rank != -1 ? String.valueOf(rank) : TranslationManager.miniMessageTranslation(MessageConstants.COMPETITION_NO_RANK.build().key()));
        this.privateContext.combine(competition.getPublicContext());
    }

    public void show() {
        this.isShown = true;
        SparrowHeart.getInstance().createBossBar(
                player,
                uuid,
                AdventureHelper.componentToJson(AdventureHelper.miniMessage(texts[0].getLatestValue())),
                BossBarColor.valueOf(config.color().name()),
                BossBarOverlay.valueOf(config.overlay().name()),
                competition.getProgress(),
                false, false, false
        );
        this.refreshTimer = config.refreshRate();
        this.senderTask = BukkitCustomFishingPlugin.getInstance().getScheduler().asyncRepeating(() -> {
            switchTimer++;
            boolean forceUpdate = false;
            if (switchTimer > config.switchInterval()) {
                switchTimer = 0;
                counter++;
                forceUpdate= true;
            }
            if (forceUpdate || refreshTimer >= config.refreshRate()) {
                refreshTimer = 0;
                DynamicText text = texts[counter % (texts.length)];
                updatePrivatePlaceholders();
                if (text.update(privateContext.placeholderMap()) || forceUpdate) {
                    SparrowHeart.getInstance().updateBossBarName(player, uuid, AdventureHelper.componentToJson(AdventureHelper.miniMessage(text.getLatestValue())));
                }
                SparrowHeart.getInstance().updateBossBarProgress(player, uuid, competition.getProgress());
            } else {
                refreshTimer++;
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
        SparrowHeart.getInstance().removeBossBar(player, uuid);
        if (senderTask != null) senderTask.cancel();
        this.isShown = false;
    }
}
