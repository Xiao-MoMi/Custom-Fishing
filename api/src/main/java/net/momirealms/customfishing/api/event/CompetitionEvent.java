/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.api.event;

import net.momirealms.customfishing.api.mechanic.competition.FishingCompetition;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents an event that occurs during a fishing competition.
 * It is triggered when the state of a fishing competition changes.
 */
public class CompetitionEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    private final State state;
    private final FishingCompetition competition;

    /**
     * Constructs a new CompetitionEvent.
     *
     * @param state The current state of the competition
     * @param competition The fishing competition associated with this event
     */
    public CompetitionEvent(State state, FishingCompetition competition) {
        super(true);
        this.state = state;
        this.competition = competition;
    }

    /**
     * Gets the current {@link State} of the competition.
     *
     * @return The current state of the competition
     */
    public State getState() {
        return state;
    }

    /**
     * Gets the {@link FishingCompetition} associated with this event.
     *
     * @return The fishing competition associated with this event
     */
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

    public enum State {
        END,
        STOP,
        START
    }
}
