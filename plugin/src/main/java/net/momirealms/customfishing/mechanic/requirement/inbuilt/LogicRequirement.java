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

package net.momirealms.customfishing.mechanic.requirement.inbuilt;

import net.momirealms.customfishing.api.manager.RequirementManager;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.mechanic.requirement.AbstractRequirement;
import org.bukkit.configuration.MemorySection;

import java.util.*;

public class LogicRequirement extends AbstractRequirement {

    private List<Requirement> requirementList;
    private boolean checkAction;

    public LogicRequirement(RequirementManager requirementManager, Object args, List<Action> actions, boolean checkAction) {
        super(actions);
        if (!(args instanceof MemorySection section1))
            return;
        this.requirementList = new ArrayList<>();
        this.checkAction = checkAction;
        Deque<StackElement> stack = new ArrayDeque<>();
        stack.push(new StackElement(section1.getValues(false), requirementList));
        while (!stack.isEmpty()) {
            StackElement stackElement = stack.pop();
            Map<String, Object> currentMap = stackElement.getMap();
            List<Requirement> currentResult = stackElement.getRequirements();
            for (Map.Entry<String, Object> entry : currentMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof MemorySection section2) {
                    Map<String, Object> sectionMap = section2.getValues(false);
                    if (key.startsWith("&&")) {
                        List<Requirement> andReqList = new ArrayList<>();
                        currentResult.add(new AndRequirement(andReqList));
                        stack.push(new StackElement(sectionMap, andReqList));
                    } else if (key.startsWith("||")) {
                        List<Requirement> orReqList = new ArrayList<>();
                        currentResult.add(new OrRequirement(orReqList));
                        stack.push(new StackElement(sectionMap, orReqList));
                    } else {
                        currentResult.add(requirementManager.getRequirement(section2, checkAction));
                    }
                }
            }
        }
    }

    public static class StackElement {

        private final Map<String, Object> map;
        private final List<Requirement> requirements;

        public StackElement(Map<String, Object> map, List<Requirement> requirements) {
            this.map = map;
            this.requirements = requirements;
        }

        public Map<String, Object> getMap() {
            return map;
        }

        public List<Requirement> getRequirements() {
            return requirements;
        }
    }

    @Override
    public boolean isConditionMet(Condition condition) {
        for (Requirement requirement : requirementList) {
            if (!requirement.isConditionMet(condition)) {
                if (checkAction) super.triggerActions(condition);
                return false;
            }
        }
        return true;
    }

    public static class AndRequirement implements Requirement {

        private final List<Requirement> requirementList;

        public AndRequirement(List<Requirement> requirementList) {
            this.requirementList = requirementList;
        }

        @Override
        public boolean isConditionMet(Condition condition) {
            for (Requirement requirement : requirementList) {
                if (!requirement.isConditionMet(condition)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class OrRequirement implements Requirement {

        private final List<Requirement> requirementList;

        public OrRequirement(List<Requirement> requirementList) {
            this.requirementList = requirementList;
        }

        @Override
        public boolean isConditionMet(Condition condition) {
            for (Requirement requirement : requirementList) {
                if (requirement.isConditionMet(condition)) {
                    return true;
                }
            }
            return false;
        }
    }
}
