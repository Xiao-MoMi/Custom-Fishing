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

package net.momirealms.customfishing.requirements;

import net.momirealms.customfishing.ConfigReader;
import net.momirealms.customfishing.hook.PapiHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FishingCondition{

    private final Location location;
    private final Player player;
    private HashMap<String, String> papiMap;

    public FishingCondition(Location location, Player player) {
        this.location = location;
        this.player = player;
        if (ConfigReader.Config.papi){
            this.papiMap = new HashMap<>();
            CustomPapi.allPapi.forEach(papi -> {
                this.papiMap.put(papi, PapiHook.parse(player, papi));
            });
        }
    }

    public HashMap<String, String> getPapiMap() {
        return papiMap;
    }

    public Location getLocation() {
        return location;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return "FishingCondition{" +
                "location=" + location +
                ", player=" + player +
                ", papiMap=" + papiMap +
                '}';
    }
}