package net.momirealms.customfishing.api.mechanic.competition;

public abstract class AbstractCompetitionInfo {

    protected int refreshRate;
    protected int switchInterval;
    protected boolean showToAll;
    protected String[] texts;

    public int getRefreshRate() {
        return refreshRate;
    }

    public int getSwitchInterval() {
        return switchInterval;
    }

    public boolean isShowToAll() {
        return showToAll;
    }

    public String[] getTexts() {
        return texts;
    }
}
