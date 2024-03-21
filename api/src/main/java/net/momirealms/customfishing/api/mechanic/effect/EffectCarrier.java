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

package net.momirealms.customfishing.api.mechanic.effect;

import net.momirealms.customfishing.api.common.Key;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EffectCarrier {

    private Key key;
    private Requirement[] requirements;
    private EffectModifier[] effect;
    private Map<ActionTrigger, Action[]> actionMap;
    private boolean persist;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final EffectCarrier item;

        public Builder() {
            this.item = new EffectCarrier();
        }

        public Builder persist(boolean persist) {
            item.persist = persist;
            return this;
        }

        public Builder key(Key key) {
            item.key = key;
            return this;
        }

        public Builder requirements(Requirement[] requirements) {
            item.requirements = requirements;
            return this;
        }

        public Builder effect(EffectModifier[] effect) {
            item.effect = effect;
            return this;
        }

        public Builder actionMap(Map<ActionTrigger, Action[]> actionMap) {
            item.actionMap = actionMap;
            return this;
        }

        public EffectCarrier build() {
            return item;
        }
    }

    public Key getKey() {
        return key;
    }

    public Requirement[] getRequirements() {
        return requirements;
    }

    public EffectModifier[] getEffectModifiers() {
        return effect;
    }

    public Map<ActionTrigger, Action[]> getActionMap() {
        return actionMap;
    }

    @Nullable
    public Action[] getActions(ActionTrigger trigger) {
        return actionMap.get(trigger);
    }

    public boolean isPersist() {
        return persist;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isConditionMet(Condition condition) {
        if (requirements == null) return true;
        for (Requirement requirement : requirements) {
            if (!requirement.isConditionMet(condition)) {
                return false;
            }
        }
        return true;
    }
}
