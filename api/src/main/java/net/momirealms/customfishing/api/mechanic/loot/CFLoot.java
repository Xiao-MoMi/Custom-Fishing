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

package net.momirealms.customfishing.api.mechanic.loot;

import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.effect.BaseEffect;
import net.momirealms.customfishing.api.mechanic.statistic.StatisticsKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class CFLoot implements Loot {

    private final String id;
    private final LootType type;
    private final HashMap<ActionTrigger, Action[]> actionMap;
    private final HashMap<Integer, Action[]> successTimesActionMap;
    private String nick;
    private boolean showInFinder;
    private boolean disableGame;
    private boolean disableGlobalAction;
    private boolean disableStats;
    private boolean instanceGame;
    private double score;
    private String[] lootGroup;
    private StatisticsKey statisticsKey;
    private BaseEffect effect;

    public CFLoot(String id, LootType type) {
        this.id = id;
        this.type = type;
        this.actionMap = new HashMap<>();
        this.successTimesActionMap = new HashMap<>();
    }

    public static Builder builder(String id, LootType type) {
        return new Builder(id, type);
    }

    /**
     * Builder class for CFLoot.
     */
    public static class Builder {

        private final CFLoot loot;

        public Builder(String id, LootType type) {
            this.loot = new CFLoot(id, type);
        }

        /**
         * Set the file path for this loot.
         *
         * @param path file path
         * @return The builder.
         */
        public Builder filePath(String path) {
            return this;
        }

        /**
         * Set the nickname for this loot.
         *
         * @param nick The nickname.
         * @return The builder.
         */
        public Builder nick(String nick) {
            this.loot.nick = nick;
            return this;
        }

        /**
         * Set whether this loot should be shown in the finder.
         *
         * @param show True if it should be shown, false otherwise.
         * @return The builder.
         */
        public Builder showInFinder(boolean show) {
            this.loot.showInFinder = show;
            return this;
        }

        /**
         * Set whether this loot should have an instance game.
         *
         * @param instant True if it should be an instance game, false otherwise.
         * @return The builder.
         */
        public Builder instantGame(boolean instant) {
            this.loot.instanceGame = instant;
            return this;
        }

        /**
         * Set whether games are disabled for this loot.
         *
         * @param disable True if games are disabled, false otherwise.
         * @return The builder.
         */
        public Builder disableGames(boolean disable) {
            this.loot.disableGame = disable;
            return this;
        }

        /**
         * Set whether statistics are disabled for this loot.
         *
         * @param disable True if statistics are disabled, false otherwise.
         * @return The builder.
         */
        public Builder disableStats(boolean disable) {
            this.loot.disableStats = disable;
            return this;
        }

        /**
         * Set whether global actions are disabled for this loot.
         *
         * @param disable True if statistics are disabled, false otherwise.
         * @return The builder.
         */
        public Builder disableGlobalActions(boolean disable) {
            this.loot.disableGlobalAction = disable;
            return this;
        }

        /**
         * Set the score for this loot.
         *
         * @param score The score.
         * @return The builder.
         */
        public Builder score(double score) {
            this.loot.score = score;
            return this;
        }

        /**
         * Set the loot group for this loot.
         *
         * @param groups The loot group.
         * @return The builder.
         */
        public Builder lootGroup(String[] groups) {
            this.loot.lootGroup = groups;
            return this;
        }

        /**
         * Set the statistics key for this loot
         *
         * @param statisticsKey statistics key
         * @return The builder.
         */
        public Builder statsKey(StatisticsKey statisticsKey) {
            this.loot.statisticsKey = statisticsKey;
            return this;
        }

        /**
         * Set the effects for the loot
         *
         * @param effect effect
         * @return The builder.
         */
        public Builder baseEffect(BaseEffect effect) {
            this.loot.effect = effect;
            return this;
        }

        /**
         * Add actions triggered by a specific trigger.
         *
         * @param trigger The trigger for the actions.
         * @param actions The actions to add.
         * @return The builder.
         */
        public Builder addActions(ActionTrigger trigger, Action[] actions) {
            this.loot.actionMap.put(trigger, actions);
            return this;
        }

        /**
         * Add actions triggered by multiple triggers.
         *
         * @param actionMap A map of triggers to actions.
         * @return The builder.
         */
        public Builder addActions(HashMap<ActionTrigger, Action[]> actionMap) {
            this.loot.actionMap.putAll(actionMap);
            return this;
        }

        /**
         * Add actions triggered by the number of successes.
         *
         * @param times   The number of successes for triggering the actions.
         * @param actions The actions to add.
         * @return The builder.
         */
        public Builder addTimesActions(int times, Action[] actions) {
            this.loot.successTimesActionMap.put(times, actions);
            return this;
        }

        /**
         * Add actions triggered by multiple numbers of successes.
         *
         * @param actionMap A map of numbers of successes to actions.
         * @return The builder.
         */
        public Builder addTimesActions(HashMap<Integer, Action[]> actionMap) {
            this.loot.successTimesActionMap.putAll(actionMap);
            return this;
        }

        /**
         * Build the CFLoot object.
         *
         * @return The built CFLoot object.
         */
        public CFLoot build() {
            return loot;
        }
    }

    @Override
    public boolean instanceGame() {
        return this.instanceGame;
    }

    @Override
    public String getID() {
        return this.id;
    }

    @Override
    public LootType getType() {
        return this.type;
    }

    @Override
    public @NotNull String getNick() {
        return this.nick;
    }

    @Override
    public StatisticsKey getStatisticKey() {
        return this.statisticsKey;
    }

    @Override
    public boolean showInFinder() {
        return this.showInFinder;
    }

    @Override
    public double getScore() {
        return this.score;
    }

    @Override
    public boolean disableGame() {
        return this.disableGame;
    }

    @Override
    public boolean disableStats() {
        return this.disableStats;
    }

    @Override
    public boolean disableGlobalAction() {
        return this.disableGlobalAction;
    }

    @Override
    public String[] getLootGroup() {
        return lootGroup;
    }

    @Override
    public Action[] getActions(ActionTrigger actionTrigger) {
        return actionMap.get(actionTrigger);
    }

    @Override
    public void triggerActions(ActionTrigger actionTrigger, Condition condition) {
        Action[] actions = getActions(actionTrigger);
        if (actions != null) {
            for (Action action : actions) {
                action.trigger(condition);
            }
        }
    }

    @Override
    public BaseEffect getBaseEffect() {
        return effect;
    }

    @Override
    public Action[] getSuccessTimesActions(int times) {
        return successTimesActionMap.get(times);
    }

    @Override
    public HashMap<Integer, Action[]> getSuccessTimesActionMap() {
        return successTimesActionMap;
    }
}