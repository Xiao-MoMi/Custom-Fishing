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
import net.momirealms.customfishing.fishing.requirements.RequirementInterface;
import net.momirealms.customfishing.object.action.ActionInterface;

public class Loot {

    public static Loot EMPTY = new Loot("null", "null", new MiniGameConfig[0], 0, false, 0d, false);

    protected final String key;
    protected final String nick;
    protected String group;
    protected boolean disableBar;
    protected final boolean showInFinder;
    protected ActionInterface[] successActions;
    protected ActionInterface[] failureActions;
    protected ActionInterface[] hookActions;
    protected ActionInterface[] consumeActions;
    protected RequirementInterface[] requirements;
    protected final MiniGameConfig[] fishingGames;
    protected final int weight;
    protected final double score;

    public Loot(String key, String nick, MiniGameConfig[] fishingGames, int weight, boolean showInFinder, double score, boolean disableBar) {
        this.key = key;
        this.nick = nick;
        this.weight = weight;
        this.showInFinder = showInFinder;
        this.score = score;
        this.fishingGames = fishingGames;
        this.disableBar = disableBar;
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

    public ActionInterface[] getSuccessActions() {
        return successActions;
    }

    public void setSuccessActions(ActionInterface[] successActions) {
        this.successActions = successActions;
    }

    public ActionInterface[] getFailureActions() {
        return failureActions;
    }

    public ActionInterface[] getConsumeActions() {
        return consumeActions;
    }

    public void setConsumeActions(ActionInterface[] consumeActions) {
        this.consumeActions = consumeActions;
    }

    public void setFailureActions(ActionInterface[] failureActions) {
        this.failureActions = failureActions;
    }

    public ActionInterface[] getHookActions() {
        return hookActions;
    }

    public void setHookActions(ActionInterface[] hookActions) {
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
}
