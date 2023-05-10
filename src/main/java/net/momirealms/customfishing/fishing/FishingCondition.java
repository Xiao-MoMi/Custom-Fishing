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

package net.momirealms.customfishing.fishing;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.requirements.CustomPapi;
import net.momirealms.customfishing.integration.papi.PlaceholderManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class FishingCondition{

    private final Location location;
    private final Player player;
    private final String rodID;
    private final String baitID;
    private final HashMap<String, String> papiMap;

    public FishingCondition(Location location, Player player, @Nullable String rodID, @Nullable String baitID) {
        this.location = location;
        this.player = player;
        this.rodID = rodID;
        this.baitID = baitID;
        this.papiMap = new HashMap<>();
        if (player != null) {
            PlaceholderManager placeholderManager = CustomFishing.getInstance().getIntegrationManager().getPlaceholderManager();
            for (String papi : CustomPapi.allPapi) {
                this.papiMap.put(papi, placeholderManager.parse(player, papi));
            }
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

    public String getRodID() {
        return rodID;
    }

    public String getBaitID() {
        return baitID;
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