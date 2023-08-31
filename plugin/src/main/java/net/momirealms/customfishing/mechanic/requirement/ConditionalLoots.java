package net.momirealms.customfishing.mechanic.requirement;

import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.loot.Modifier;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;

import java.util.HashMap;
import java.util.List;

public class ConditionalLoots {

    private final List<Pair<String, Modifier>> modifierList;
    private final HashMap<String, ConditionalLoots> subLoots;
    private final Requirement[] requirements;

    public ConditionalLoots(
            Requirement[] requirements,
            List<Pair<String, Modifier>> modifierList,
            HashMap<String, ConditionalLoots> subLoots
    ) {
        this.modifierList = modifierList;
        this.requirements = requirements;
        this.subLoots = subLoots;
    }

    synchronized public void combine(HashMap<String, Double> weightMap) {
        for (Pair<String, Modifier> modifierPair : this.modifierList) {
            double previous = weightMap.getOrDefault(modifierPair.left(), 0d);
            weightMap.put(modifierPair.left(), modifierPair.right().modify(previous));
        }
    }

    public boolean isConditionsMet(Condition condition) {
        for (Requirement requirement : requirements) {
            if (!requirement.isConditionMet(condition)) {
                return false;
            }
        }
        return true;
    }

    public HashMap<String, ConditionalLoots> getSubLoots() {
        return subLoots;
    }
}
