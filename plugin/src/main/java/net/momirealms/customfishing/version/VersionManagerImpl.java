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

package net.momirealms.customfishing.version;

import net.momirealms.customfishing.CustomFishingPluginImpl;
import net.momirealms.customfishing.api.manager.VersionManager;
import net.momirealms.customfishing.api.util.LogUtils;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CompletableFuture;

/**
 * This class implements the VersionManager interface and is responsible for managing version-related information.
 */
public class VersionManagerImpl implements VersionManager {

    private final CustomFishingPluginImpl plugin;
    private final float mcVersion;
    private boolean isFolia;
    private boolean isMojmap;
    private boolean isSpigot;
    private final String pluginVersion;

    @SuppressWarnings("deprecation")
    public VersionManagerImpl(CustomFishingPluginImpl plugin) {
        this.plugin = plugin;
        // Get the server version

        String[] split = Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.");
        this.mcVersion = Float.parseFloat(split[1] + "." + (split.length >= 3 ? split[2] : "0"));

        // Get the plugin version
        this.pluginVersion = plugin.getDescription().getVersion();

        this.isSpigot = Bukkit.getServer().getName().equals("CraftBukkit");

        // Check if the server is Folia
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            this.isFolia = true;
        } catch (ClassNotFoundException ignored) {
            this.isFolia = false;
        }

        // Check if the server is Mojmap
        try {
            Class.forName("net.minecraft.network.protocol.game.ClientboundBossEventPacket");
            this.isMojmap = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    @Override
    public boolean isVersionNewerThan1_19() {
        return mcVersion >= 19;
    }

    @Override
    public boolean isVersionNewerThan1_19_R3() {
        return mcVersion >= 19.4;
    }
    
    @Override
    public boolean isVersionNewerThan1_19_R2() {
        return mcVersion >= 19.3;
    }

    @Override
    public boolean isVersionNewerThan1_20() {
        return mcVersion >= 20;
    }

    @Override
    public boolean isSpigot() {
        return false;
    }

    @Override
    public String getPluginVersion() {
        return pluginVersion;
    }

    @Override
    public boolean hasRegionScheduler() {
        return isFolia;
    }

    @Override
    public boolean isMojmap() {
        return isMojmap;
    }

    // Method to asynchronously check for plugin updates
    @Override
    public CompletableFuture<Boolean> checkUpdate() {
        CompletableFuture<Boolean> updateFuture = new CompletableFuture<>();
        plugin.getScheduler().runTaskAsync(() -> {
            try {
                URL url = new URL("https://api.polymart.org/v1/getResourceInfoSimple/?resource_id=2723&key=version");
                URLConnection conn = url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(60000);
                InputStream inputStream = conn.getInputStream();
                String newest = new BufferedReader(new InputStreamReader(inputStream)).readLine();
                String current = plugin.getVersionManager().getPluginVersion();
                inputStream.close();
                if (!compareVer(newest, current)) {
                    updateFuture.complete(false);
                    return;
                }
                updateFuture.complete(true);
            } catch (Exception exception) {
                LogUtils.warn("Error occurred when checking update.", exception);
                updateFuture.complete(false);
            }
        });
        return updateFuture;
    }

    // Method to compare two version strings
    private boolean compareVer(String newV, String currentV) {
        if (newV == null || currentV == null || newV.isEmpty() || currentV.isEmpty()) {
            return false;
        }
        String[] newVS = newV.split("\\.");
        String[] currentVS = currentV.split("\\.");
        int maxL = Math.min(newVS.length, currentVS.length);
        for (int i = 0; i < maxL; i++) {
            try {
                String[] newPart = newVS[i].split("-");
                String[] currentPart = currentVS[i].split("-");
                int newNum = Integer.parseInt(newPart[0]);
                int currentNum = Integer.parseInt(currentPart[0]);
                if (newNum > currentNum) {
                    return true;
                } else if (newNum < currentNum) {
                    return false;
                } else if (newPart.length > 1 && currentPart.length > 1) {
                    String[] newHotfix = newPart[1].split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                    String[] currentHotfix = currentPart[1].split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                    if (newHotfix.length == 2 && currentHotfix.length == 1) return true;
                    else if (newHotfix.length > 1 && currentHotfix.length > 1) {
                        int newHotfixNum = Integer.parseInt(newHotfix[1]);
                        int currentHotfixNum = Integer.parseInt(currentHotfix[1]);
                        if (newHotfixNum > currentHotfixNum) {
                            return true;
                        } else if (newHotfixNum < currentHotfixNum) {
                            return false;
                        } else {
                            return newHotfix[0].compareTo(currentHotfix[0]) > 0;
                        }
                    }
                } else if (newPart.length > 1) {
                    return true;
                } else if (currentPart.length > 1) {
                    return false;
                }
            }
            catch (NumberFormatException ignored) {
                return false;
            }
        }
        return newVS.length > currentVS.length;
    }
}