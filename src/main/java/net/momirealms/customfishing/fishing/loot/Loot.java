package net.momirealms.customfishing.fishing.loot;

import net.momirealms.customfishing.fishing.MiniGameConfig;
import net.momirealms.customfishing.fishing.action.Action;
import net.momirealms.customfishing.fishing.requirements.RequirementInterface;

import java.util.HashMap;

public interface Loot {

    MiniGameConfig[] getFishingGames();

    String getKey();

    String getNick();

    String getGroup();

    boolean isShowInFinder();

    Action[] getSuccessActions();

    public Action[] getFailureActions();

    public Action[] getConsumeActions();

    public Action[] getHookActions();

    public int getWeight();

    public double getScore();

    public RequirementInterface[] getRequirements();

    public boolean isDisableBar();

    HashMap<Integer, Action[]> getSuccessTimesActions();

    boolean isDisableStats();
}
