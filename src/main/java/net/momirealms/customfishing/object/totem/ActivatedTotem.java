package net.momirealms.customfishing.object.totem;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.manager.FishingManager;
import net.momirealms.customfishing.util.ArmorStandUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
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
                    try {
                        for (int i = 0; i < entityID.length; i++) {
                            CustomFishing.protocolManager.sendServerPacket(player, ArmorStandUtil.getMetaPacket(entityID[i],
                                    totem.getHoloText()[entityID.length - 1 - i].replace("{time}", String.valueOf(totem.getDuration() - timer))
                                            .replace("{max_time}", String.valueOf(totem.getDuration()))
                            ));
                        }
                        addPotionEffect(player);
                    }
                    catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                if (hasHolo) {
                    try {
                        for (int j : entityID) {
                            CustomFishing.protocolManager.sendServerPacket(player, ArmorStandUtil.getDestroyPacket(j));
                        }
                    }
                    catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
                nearbyPlayerSet.remove(player);
            }
        }

        for (Player newComer : nearbyPlayers) {
            if (hasHolo) {
                try {
                    for (int i = 0; i < entityID.length; i++) {
                        CustomFishing.protocolManager.sendServerPacket(newComer, ArmorStandUtil.getSpawnPacket(entityID[i], location.clone().add(0.5, totem.getHoloOffset() + i * 0.4, 0.5)));
                        CustomFishing.protocolManager.sendServerPacket(newComer, ArmorStandUtil.getMetaPacket(entityID[i],
                                totem.getHoloText()[entityID.length - 1 - i].replace("{time}", String.valueOf(totem.getDuration() - timer))
                                                        .replace("{max_time}", String.valueOf(totem.getDuration()))
                        ));
                    }
                    addPotionEffect(newComer);
                }
                catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
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
        fishingManager.removeTotem(this);

        if (hasHolo) {
            for (Player player : nearbyPlayerSet) {
                try {
                    for (int j : entityID) {
                        CustomFishing.protocolManager.sendServerPacket(player, ArmorStandUtil.getDestroyPacket(j));
                    }
                }
                catch (InvocationTargetException e) {
                    e.printStackTrace();
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
