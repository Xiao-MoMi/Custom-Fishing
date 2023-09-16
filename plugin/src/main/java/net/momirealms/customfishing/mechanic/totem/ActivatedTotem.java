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

package net.momirealms.customfishing.mechanic.totem;

import net.momirealms.customfishing.api.scheduler.CancellableTask;
import net.momirealms.customfishing.mechanic.totem.particle.ParticleSetting;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class ActivatedTotem {

    private final List<CancellableTask> subTasks;
    private final Location coreLocation;
    private final TotemConfig totemConfig;

    public ActivatedTotem(Location coreLocation, TotemConfig config) {
        this.subTasks = new ArrayList<>();
        this.coreLocation = coreLocation.clone().add(0.5,0,0.5);
        this.totemConfig = config;
        for (ParticleSetting particleSetting : config.getParticleSettings()) {
            this.subTasks.add(particleSetting.start(coreLocation, config.getRadius()));
        }
    }

    public TotemConfig getTotemConfig() {
        return totemConfig;
    }

    public Location getCoreLocation() {
        return coreLocation;
    }

    public void cancel() {
        for (CancellableTask task : subTasks) {
            task.cancel();
        }
    }
}
