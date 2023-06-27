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

package net.momirealms.customfishing.fishing.loot;

import net.momirealms.customfishing.fishing.MiniGameConfig;
import net.momirealms.customfishing.fishing.action.Action;
import net.momirealms.customfishing.fishing.requirements.RequirementInterface;

import java.util.HashMap;

public class LootImpl implements Loot {

    public static LootImpl EMPTY = new LootImpl("null", "null", new MiniGameConfig[0], 0, false, 0d, false, true);

    protected final String key;
    protected final String nick;
    protected String group;
    protected boolean disableStats;
    protected boolean disableBar;
    protected final boolean showInFinder;
    protected Action[] successActions;
    protected Action[] failureActions;
    protected Action[] hookActions;
    protected Action[] consumeActions;
    protected HashMap<Integer, Action[]> successTimesActions;
    protected RequirementInterface[] requirements;
    protected final MiniGameConfig[] fishingGames;
    protected final int weight;
    protected final double score;

    public LootImpl(String key, String nick, MiniGameConfig[] fishingGames, int weight, boolean showInFinder, double score, boolean disableBar, boolean disableStats) {
        this.key = key;
        this.nick = nick;
        this.weight = weight;
        this.showInFinder = showInFinder;
        this.score = score;
        this.fishingGames = fishingGames;
        this.disableBar = disableBar;
        this.disableStats = disableStats;
    }

    public MiniGameConfig[] getFishingGames() {
        return fishingGames;
    }

    public String getKey() {
        return key;
    }

    public String getNick() {
        return nick;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isShowInFinder() {
        return showInFinder;
    }

    public Action[] getSuccessActions() {
        return successActions;
    }

    public void setSuccessActions(Action[] successActions) {
        this.successActions = successActions;
    }

    public Action[] getFailureActions() {
        return failureActions;
    }

    public Action[] getConsumeActions() {
        return consumeActions;
    }

    public void setConsumeActions(Action[] consumeActions) {
        this.consumeActions = consumeActions;
    }

    public void setFailureActions(Action[] failureActions) {
        this.failureActions = failureActions;
    }

    public Action[] getHookActions() {
        return hookActions;
    }

    public void setHookActions(Action[] hookActions) {
        this.hookActions = hookActions;
    }

    public int getWeight() {
        return weight;
    }

    public double getScore() {
        return score;
    }

    public RequirementInterface[] getRequirements() {
        return requirements;
    }

    public void setRequirements(RequirementInterface[] requirements) {
        this.requirements = requirements;
    }

    public boolean isDisableBar() {
        return disableBar;
    }

    public HashMap<Integer, Action[]> getSuccessTimesActions() {
        return successTimesActions;
    }

    public void setSuccessTimesActions(HashMap<Integer, Action[]> successTimesActions) {
        this.successTimesActions = successTimesActions;
    }

    public boolean isDisableStats() {
        return disableStats;
    }
}
