package net.momirealms.customfishing.api.mechanic.statistic;

public class StatisticsKey {

    private final String amountKey;
    private final String sizeKey;

    public StatisticsKey(String amountKey, String sizeKey) {
        this.amountKey = amountKey;
        this.sizeKey = sizeKey;
    }

    public String getAmountKey() {
        return amountKey;
    }

    public String getSizeKey() {
        return sizeKey;
    }
}
