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

package net.momirealms.customfishing.object.totem;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.manager.FishingManager;
import net.momirealms.customfishing.util.ArmorStandUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ActivatedTotem extends BukkitRunnable {

    public static int id = 127616121;
    private int timer;
    private final Totem totem;
    private final Location location;
    private final Set<Player> nearbyPlayerSet;
    private final int[] entityID;
    private final boolean hasHolo;
    private final BukkitRunnable particleTimerTask;
    private final FishingManager fishingManager;

    public ActivatedTotem(Location location, Totem totem, FishingManager fishingManager) {
        this.fishingManager = fishingManager;
        this.totem = totem;
        this.location = location;
        this.entityID = new int[totem.getHoloText().length];
        for (int i = 0; i < totem.getHoloText().length; i++) {
            this.entityID[i] = id++;
        }
        this.hasHolo = totem.getHoloText() != null;
        this.nearbyPlayerSet = Collections.synchronizedSet(new HashSet<>());
        this.particleTimerTask = new TotemParticle(location, totem.getRadius(), totem.getParticle());
        this.particleTimerTask.runTaskTimerAsynchronously(CustomFishing.plugin, 0, 4);
    }

    @Override
    public void run() {

        timer++;
        if (timer > totem.getDuration()) {
            stop();
            return;
        }

        HashSet<Player> temp = new HashSet<>(nearbyPlayerSet);
        Collection<Player> nearbyPlayers = location.getNearbyPlayers(totem.getRadius());

        for (Player player : temp) {
            if (nearbyPlayers.remove(player)) {
                if (hasHolo) {
                    for (int i = 0; i < entityID.length; i++) {
                        CustomFishing.protocolManager.sendServerPacket(player, ArmorStandUtil.getMetaPacket(entityID[i],
                                totem.getHoloText()[entityID.length - 1 - i].replace("{time}", String.valueOf(totem.getDuration() - timer))
                                        .replace("{max_time}", String.valueOf(totem.getDuration()))
                        ));
                    }
                    addPotionEffect(player);
                }
            }
            else {
                if (hasHolo) {
                    for (int j : entityID) {
                        CustomFishing.protocolManager.sendServerPacket(player, ArmorStandUtil.getDestroyPacket(j));
                    }
                }
                nearbyPlayerSet.remove(player);
            }
        }

        for (Player newComer : nearbyPlayers) {
            if (hasHolo) {
                for (int i = 0; i < entityID.length; i++) {
                    CustomFishing.protocolManager.sendServerPacket(newComer, ArmorStandUtil.getSpawnPacket(entityID[i], location.clone().add(0.5, totem.getHoloOffset() + i * 0.4, 0.5)));
                    CustomFishing.protocolManager.sendServerPacket(newComer, ArmorStandUtil.getMetaPacket(entityID[i],
                            totem.getHoloText()[entityID.length - 1 - i].replace("{time}", String.valueOf(totem.getDuration() - timer))
                                                    .replace("{max_time}", String.valueOf(totem.getDuration()))
                    ));
                }
                addPotionEffect(newComer);
            }
            nearbyPlayerSet.add(newComer);
        }
    }

    public Set<Player> getNearbyPlayerSet() {
        return nearbyPlayerSet;
    }

    public Totem getTotem() {
        return totem;
    }

    public void stop() {
        this.particleTimerTask.cancel();
        cancel();
        fishingManager.removeTotem(location);

        if (hasHolo) {
            for (Player player : nearbyPlayerSet) {
                for (int j : entityID) {
                    CustomFishing.protocolManager.sendServerPacket(player, ArmorStandUtil.getDestroyPacket(j));
                }
            }
        }
        nearbyPlayerSet.clear();
    }

    private void addPotionEffect(Player player) {
        if (totem.getPotionEffects() != null) {
            for (PotionEffect potionEffect : totem.getPotionEffects()) {
                player.addPotionEffect(potionEffect);
            }
        }
    }
}
