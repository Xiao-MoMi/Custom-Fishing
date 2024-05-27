///*
// *  Copyright (C) <2022> <XiaoMoMi>
// *
// *  This program is free software: you can redistribute it and/or modify
// *  it under the terms of the GNU General Public License as published by
// *  the Free Software Foundation, either version 3 of the License, or
// *  any later version.
// *
// *  This program is distributed in the hope that it will be useful,
// *  but WITHOUT ANY WARRANTY; without even the implied warranty of
// *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *  GNU General Public License for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
// */
//
//package net.momirealms.customfishing.bukkit.totem;
//
//import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
//import net.momirealms.customfishing.api.mechanic.action.Action;
//import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
//import net.momirealms.customfishing.api.mechanic.totem.TotemConfig;
//import net.momirealms.customfishing.api.mechanic.totem.TotemParticle;
//import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
//import org.bukkit.Location;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//public class ActivatedTotem {
//
//    private final List<SchedulerTask> subTasks;
//    private final Location coreLocation;
//    private final TotemConfig totemConfig;
//    private final long expireTime;
//    private final EffectCarrier effectCarrier;
//
//    public ActivatedTotem(Location coreLocation, TotemConfig config) {
//        this.subTasks = new ArrayList<>();
//        this.expireTime = System.currentTimeMillis() + config.getDuration() * 1000L;
//        this.coreLocation = coreLocation.clone().add(0.5,0,0.5);
//        this.totemConfig = config;
//        this.effectCarrier = BukkitCustomFishingPlugin.get().getEffectManager().getEffectCarrier("totem", config.getKey());
//        for (TotemParticle particleSetting : config.getParticleSettings()) {
//            this.subTasks.add(particleSetting.start(coreLocation, config.getRadius()));
//        }
//    }
//
//    public TotemConfig getTotemConfig() {
//        return totemConfig;
//    }
//
//    public Location getCoreLocation() {
//        return coreLocation;
//    }
//
//    public void cancel() {
//        for (CancellableTask task : subTasks) {
//            task.cancel();
//        }
//    }
//
//    public long getExpireTime() {
//        return expireTime;
//    }
//
//    public void doTimerAction() {
//        HashMap<String, String> args = new HashMap<>();
//        args.put("{time_left}", String.valueOf((expireTime - System.currentTimeMillis())/1000));
//        PlayerContext playerContext = new PlayerContext(coreLocation, null, args);
//        if (effectCarrier != null) {
//            Action[] actions = effectCarrier.getActions(ActionTrigger.TIMER);
//            if (actions != null) {
//                for (Action action : actions) {
//                    action.trigger(playerContext);
//                }
//            }
//        }
//    }
//
//    public EffectCarrier getEffectCarrier() {
//        return effectCarrier;
//    }
//}
