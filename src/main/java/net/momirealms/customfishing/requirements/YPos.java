package net.momirealms.customfishing.requirements;

import org.apache.commons.lang.StringUtils;

import java.util.List;

public record YPos(List<String> yPos) implements Requirement {

    public List<String> getYPos() {
        return this.yPos;
    }

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
        int y = (int) fishingCondition.getLocation().getY();
        for (String range : yPos) {
            String[] yMinMax = StringUtils.split(range, "~");
            if (y > Integer.parseInt(yMinMax[0]) && y < Integer.parseInt(yMinMax[1])) {
                return true;
            }
        }
        return false;
    }
}