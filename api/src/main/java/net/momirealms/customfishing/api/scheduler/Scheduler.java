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

package net.momirealms.customfishing.api.scheduler;

import org.bukkit.Location;

import java.util.concurrent.TimeUnit;

public interface Scheduler {

    void runTaskSync(Runnable runnable, Location location);

    CancellableTask runTaskSyncTimer(Runnable runnable, Location location, long delayTicks, long periodTicks);

    CancellableTask runTaskAsyncLater(Runnable runnable, long delay, TimeUnit timeUnit);

    void runTaskAsync(Runnable runnable);

    CancellableTask runTaskSyncLater(Runnable runnable, Location location, long delay, TimeUnit timeUnit);

    CancellableTask runTaskSyncLater(Runnable runnable, Location location, long delayTicks);

    CancellableTask runTaskAsyncTimer(Runnable runnable, long delay, long period, TimeUnit timeUnit);
}
