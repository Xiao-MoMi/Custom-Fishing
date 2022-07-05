package net.momirealms.customfishing.requirements;

import org.apache.commons.lang.StringUtils;

import java.util.List;

public record Time(List<String> times) implements Requirement{

    public List<String> getTimes() {
        return this.times;
    }

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
        long time = fishingCondition.getLocation().getWorld().getTime();
        for (String range : times) {
            String[] timeMinMax = StringUtils.split(range, "~");
            if (time > Long.parseLong(timeMinMax[0]) && time < Long.parseLong(timeMinMax[1])) {
                return true;
            }
        }
        return false;
    }
}
