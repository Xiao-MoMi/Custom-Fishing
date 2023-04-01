package net.momirealms.customfishing.fishing.requirements;

import net.momirealms.customfishing.fishing.FishingCondition;

import java.util.Calendar;
import java.util.HashSet;

public class DateImpl extends Requirement implements RequirementInterface {

    private final HashSet<String> dates;

    public DateImpl(String[] msg, HashSet<String> dates) {
        super(msg);
        this.dates = dates;
    }

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
        Calendar calendar = Calendar.getInstance();
        String current = (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DATE);
        boolean met = dates.contains(current);
        if (!met) {
            notMetMessage(fishingCondition.getPlayer());
            return false;
        }
        return true;
    }
}
