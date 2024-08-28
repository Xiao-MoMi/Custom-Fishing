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

package net.momirealms.customfishing.api.mechanic.loot;

import net.momirealms.customfishing.api.mechanic.effect.LootBaseEffect;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.api.mechanic.misc.value.TextValue;
import net.momirealms.customfishing.api.mechanic.statistic.StatisticsKeys;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class LootImpl implements Loot {

    private final LootType type;
    private final boolean instantGame;
    private final boolean disableGame;
    private final boolean disableStatistics;
    private final boolean showInFinder;
    private final boolean preventGrabbing;
    private final String id;
    private final String nick;
    private final StatisticsKeys statisticsKeys;
    private final MathValue<Player> score;
    private final String[] groups;
    private final LootBaseEffect lootBaseEffect;
    private final MathValue<Player> toInventory;
    private final Map<String, TextValue<Player>> customData;

    public LootImpl(LootType type, boolean instantGame, boolean disableGame, boolean disableStatistics, boolean showInFinder, boolean preventGrabbing, String id, String nick, StatisticsKeys statisticsKeys, MathValue<Player> score, String[] groups, LootBaseEffect lootBaseEffect, MathValue<Player> toInventory, Map<String, TextValue<Player>> customData) {
        this.type = type;
        this.instantGame = instantGame;
        this.disableGame = disableGame;
        this.disableStatistics = disableStatistics;
        this.showInFinder = showInFinder;
        this.id = id;
        this.nick = nick;
        this.statisticsKeys = statisticsKeys;
        this.score = score;
        this.groups = groups;
        this.lootBaseEffect = lootBaseEffect;
        this.preventGrabbing = preventGrabbing;
        this.toInventory = toInventory;
        this.customData = customData;
    }

    @Override
    public boolean instantGame() {
        return instantGame;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public LootType type() {
        return type;
    }

    @NotNull
    @Override
    public String nick() {
        return nick;
    }

    @Override
    public StatisticsKeys statisticKey() {
        return statisticsKeys;
    }

    @Override
    public boolean showInFinder() {
        return showInFinder;
    }

    @Override
    public boolean preventGrabbing() {
        return preventGrabbing;
    }

    @Override
    public MathValue<Player> toInventory() {
        return toInventory;
    }

    @Override
    public MathValue<Player> score() {
        return score;
    }

    @Override
    public boolean disableGame() {
        return disableGame;
    }

    @Override
    public boolean disableStats() {
        return disableStatistics;
    }

    @Override
    public String[] lootGroup() {
        return groups;
    }

    @Override
    public LootBaseEffect baseEffect() {
        return lootBaseEffect;
    }

    @Override
    public Map<String, TextValue<Player>> customData() {
        return customData;
    }

    public static class BuilderImpl implements Builder {

        private LootType type = DEFAULT_TYPE;
        private boolean instantGame = Loot.DefaultProperties.DEFAULT_INSTANT_GAME;
        private boolean disableGame = Loot.DefaultProperties.DEFAULT_DISABLE_GAME;
        private boolean disableStatistics = Loot.DefaultProperties.DEFAULT_DISABLE_STATS;
        private boolean showInFinder = Loot.DefaultProperties.DEFAULT_SHOW_IN_FINDER;
        private boolean preventGrabbing = false;
        private String id = null;
        private String nick = "UNDEFINED";
        private StatisticsKeys statisticsKeys = null;
        private MathValue<Player> score = DEFAULT_SCORE;
        private String[] groups = new String[0];
        private LootBaseEffect lootBaseEffect = null;
        private MathValue<Player> toInventory = MathValue.plain(0);
        private Map<String, TextValue<Player>> customData = new LinkedHashMap<>();

        @Override
        public Builder type(LootType type) {
            this.type = type;
            return this;
        }
        @Override
        public Builder instantGame(boolean instantGame) {
            this.instantGame = instantGame;
            return this;
        }
        @Override
        public Builder disableGame(boolean disableGame) {
            this.disableGame = disableGame;
            return this;
        }
        @Override
        public Builder preventGrabbing(boolean preventGrabbing) {
            this.preventGrabbing = preventGrabbing;
            return this;
        }
        @Override
        public Builder disableStatistics(boolean disableStatistics) {
            this.disableStatistics = disableStatistics;
            return this;
        }
        @Override
        public Builder showInFinder(boolean showInFinder) {
            this.showInFinder = showInFinder;
            return this;
        }
        @Override
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        @Override
        public Builder nick(String nick) {
            this.nick = nick;
            return this;
        }
        @Override
        public Builder statisticsKeys(StatisticsKeys statisticsKeys) {
            this.statisticsKeys = statisticsKeys;
            return this;
        }
        @Override
        public Builder score(MathValue<Player> score) {
            this.score = score;
            return this;
        }
        @Override
        public Builder groups(String[] groups) {
            this.groups = groups;
            return this;
        }
        @Override
        public Builder lootBaseEffect(LootBaseEffect lootBaseEffect) {
            this.lootBaseEffect = lootBaseEffect;
            return this;
        }
        @Override
        public Builder customData(Map<String, TextValue<Player>> customData) {
            this.customData.putAll(customData);
            return this;
        }
        @Override
        public Builder toInventory(MathValue<Player> toInventory) {
            this.toInventory = toInventory;
            return this;
        }
        @Override
        public Loot build() {
            return new LootImpl(
                    type,
                    instantGame,
                    disableGame,
                    disableStatistics,
                    showInFinder,
                    preventGrabbing,
                    requireNonNull(id),
                    Optional.ofNullable(nick).orElse(id),
                    Optional.ofNullable(statisticsKeys).orElse(new StatisticsKeys(id, id)),
                    score,
                    groups,
                    requireNonNull(lootBaseEffect),
                    toInventory,
                    customData
            );
        }
    }
}
