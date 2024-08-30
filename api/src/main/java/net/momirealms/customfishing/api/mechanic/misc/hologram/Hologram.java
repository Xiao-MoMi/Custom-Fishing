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

package net.momirealms.customfishing.api.mechanic.misc.hologram;

import net.momirealms.sparrow.heart.feature.entity.FakeNamedEntity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class Hologram {

    private final FakeNamedEntity entity;
    private Set<Player> set1 = new HashSet<>();
    private int ticksRemaining = 0;

    public Hologram(FakeNamedEntity entity) {
        this.entity = entity;
    }

    public void name(String json) {
        entity.name(json);
    }

    public void destroy() {
        for (Player player : set1) {
            entity.destroy(player);
        }
        set1.clear();
    }

    public void setTicksRemaining(int ticks) {
        ticksRemaining = ticks;
    }

    public boolean reduceTicks() {
        ticksRemaining--;
        return ticksRemaining < 0;
    }

    public void updateNearbyPlayers(Set<Player> set2) {
        Set<Player> intersectionSet = new HashSet<>(set1);
        intersectionSet.retainAll(set2);
        Set<Player> uniqueToSet1 = new HashSet<>(set1);
        uniqueToSet1.removeAll(set2);
        Set<Player> uniqueToSet2 = new HashSet<>(set2);
        uniqueToSet2.removeAll(set1);
        for (Player p : uniqueToSet1) {
            entity.destroy(p);
        }
        for (Player p : uniqueToSet2) {
            entity.spawn(p);
        }
        for (Player p : intersectionSet) {
            entity.updateMetaData(p);
        }
        set1 = set2;
    }
}
