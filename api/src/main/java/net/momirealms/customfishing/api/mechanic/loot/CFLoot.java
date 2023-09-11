package net.momirealms.customfishing.api.mechanic.loot;

import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class CFLoot implements Loot {

    private final String id;
    private final LootType type;
    private String gameConfig;
    private final HashMap<ActionTrigger, Action[]> actionMap;
    private final HashMap<Integer, Action[]> successTimesActionMap;
    private String nick;
    private boolean showInFinder;
    private boolean disableGame;
    private boolean disableStats;
    private boolean instanceGame;
    private double score;
    private String[] lootGroup;

    public CFLoot(String id, LootType type) {
        this.id = id;
        this.type = type;
        this.actionMap = new HashMap<>();
        this.successTimesActionMap = new HashMap<>();
    }

    public static CFLoot of(String id, LootType type) {
        return new CFLoot(id, type);
    }

    public static class Builder {

        private final CFLoot loot;

        public Builder(String id, LootType type) {
            this.loot = new CFLoot(id, type);
        }

        public Builder nick(String nick) {
            this.loot.nick = nick;
            return this;
        }

        public Builder showInFinder(boolean show) {
            this.loot.showInFinder = show;
            return this;
        }

        public Builder instantGame(boolean instant) {
            this.loot.instanceGame = instant;
            return this;
        }

        public Builder gameConfig(String gameConfig) {
            this.loot.gameConfig = gameConfig;
            return this;
        }

        public Builder disableGames(boolean disable) {
            this.loot.disableGame = disable;
            return this;
        }

        public Builder disableStats(boolean disable) {
            this.loot.disableStats = disable;
            return this;
        }

        public Builder score(double score) {
            this.loot.score = score;
            return this;
        }

        public Builder lootGroup(String[] groups) {
            this.loot.lootGroup = groups;
            return this;
        }

        public Builder addActions(ActionTrigger trigger, Action[] actions) {
            this.loot.actionMap.put(trigger, actions);
            return this;
        }

        public Builder addActions(HashMap<ActionTrigger, Action[]> actionMap) {
            this.loot.actionMap.putAll(actionMap);
            return this;
        }

        public Builder addTimesActions(int times, Action[] actions) {
            this.loot.successTimesActionMap.put(times, actions);
            return this;
        }

        public Builder addTimesActions(HashMap<Integer, Action[]> actionMap) {
            this.loot.successTimesActionMap.putAll(actionMap);
            return this;
        }

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
    public Action[] getSuccessTimesActions(int times) {
        return successTimesActionMap.get(times);
    }

    @Override
    public HashMap<Integer, Action[]> getSuccessTimesActionMap() {
        return successTimesActionMap;
    }
}