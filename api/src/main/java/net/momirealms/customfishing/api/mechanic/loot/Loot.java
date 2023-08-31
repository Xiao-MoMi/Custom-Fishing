package net.momirealms.customfishing.api.mechanic.loot;

import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.game.GameConfig;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public interface Loot {

    boolean instanceGame();

    /**
     * get the loot id
     * @return id
     */
    String getID();

    /**
     * get the loot type
     * @return type
     */
    LootType getType();

    /**
     * nick would be display.name or key name if not set (MiniMessage format)
     * @return nick
     */
    @NotNull
    String getNick();

    /**
     * if the loot can be seen from the finder
     * @return show in finder or not
     */
    boolean showInFinder();

    /**
     * get the score in competition
     * @return score
     */
    double getScore();

    /**
     * if the game is disabled
     * @return disabled or not
     */
    boolean disableGame();

    /**
     * if the statistics is disabled
     * @return disabled or not
     */
    boolean disableStats();

    /**
     * Get the game config
     * @return game config
     */
    GameConfig getGameConfig();

    /**
     * get actions triggered by certain events
     * @return actions
     */
    Action[] getActions(ActionTrigger actionTrigger);

    /**
     * get actions when succeeding in fishing for certain times
     * @param times times
     * @return actions
     */
    Action[] getSuccessTimesActions(int times);

    HashMap<Integer, Action[]> getSuccessTimesActionMap();
}
