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

package net.momirealms.customfishing.api.mechanic.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.game.Game;
import net.momirealms.customfishing.api.mechanic.game.GameFactory;

public class MiniGameConfigParser {

    private final String id;
    private Game game;

    public MiniGameConfigParser(String id, Section section) {
        this.id = id;
        analyze(section);
    }

    private void analyze(Section section) {
        String type = section.getString("game-type");
        GameFactory factory = BukkitCustomFishingPlugin.getInstance().getGameManager().getGameFactory(type);
        if (factory == null) {
            throw new RuntimeException("Unknown game-type: " + type);
        }
        this.game = factory.create(id, section);
    }

    public Game getGame() {
        return game;
    }
}
