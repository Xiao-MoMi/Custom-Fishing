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

import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.fishing.CustomFishingHook;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This class provides
 */
public class FishingEffectApplyEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    private final Stage stage;
    private final Effect effect;
    private final CustomFishingHook hook;

    public FishingEffectApplyEvent(CustomFishingHook hook, Effect effect, Stage stage) {
        this.hook = hook;
        this.effect = effect;
        this.stage = stage;
    }

    /**
     * Get the current stage.
     * <p>
     * {@link Stage#CAST}: The effect at this stage determines whether the player can perform a certain mechanism for instance lava fishing.
     * {@link Stage#LOOT}: The effect at this stage play a crucial role in what loot will appear next, and weighted effects should be applied at this stage.
     * {@link Stage#FISHING}: The effects at this stage affect the hook time, game difficulty and other fishing-related attributes
     * <p>
     * For developers, {@link Stage#CAST} will only be triggered once, while the other two stages will be triggered multiple times
     *
     * @return the stage
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Get the {@link Effect}
     * <p>
     * Effects at stage {@link Stage#CAST} are constant because this stage only affects what mechanics the player can play.
     * Effects at stage {@link Stage#LOOT}/{@link Stage#FISHING} are temporary because the fishhook could move. For example, it flows from the water into the lava or another biome,
     * causing some conditional effects changing.
     * <p>
     * For developers, {@link Stage#CAST} will only be triggered once, while the other two stages will be triggered multiple times
     *
     * @return the effect
     */
    public Effect getEffect() {
        return effect;
    }

    /**
     * Get the Custom Fishing hook
     *
     * @return the fishing hook
     */
    public CustomFishingHook getHook() {
        return hook;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public enum Stage {

        CAST(0), LOOT(1), FISHING(2);

        private final int id;

        Stage(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
