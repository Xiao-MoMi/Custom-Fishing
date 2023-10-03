package net.momirealms.customfishing.api.event;

import net.momirealms.customfishing.api.mechanic.competition.FishingCompetition;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CompetitionEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();
    private final State state;
    private final FishingCompetition competition;

    public CompetitionEvent(State state, FishingCompetition competition) {
        this.state = state;
        this.competition = competition;
    }

    public State getState() {
        return state;
    }

    public FishingCompetition getCompetition() {
        return competition;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static enum State {
        END,
        STOP,
        START
    }
}
