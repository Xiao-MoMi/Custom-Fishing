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

package net.momirealms.customfishing.helper;

import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import de.tr7zw.changeme.nbtapi.utils.VersionChecker;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.manager.ConfigManager;
import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;

public class VersionHelper {

    private boolean isNewerThan1_19_R2;
    private String serverVersion;
    private final CustomFishing plugin;
    private final boolean isSpigot;
    private final String pluginVersion;

    public VersionHelper(CustomFishing plugin) {
        this.plugin = plugin;
        isVersionNewerThan1_19_R2();
        disableUseLessInfo();
        isSpigot = plugin.getServer().getName().equals("CraftBukkit");
        pluginVersion = plugin.getDescription().getVersion();
    }

    public boolean isVersionNewerThan1_19_R2() {
        if (serverVersion == null) {
            serverVersion = plugin.getServer().getClass().getPackage().getName().split("\\.")[3];
            String[] split = serverVersion.split("_");
            int main_ver = Integer.parseInt(split[1]);
            if (main_ver >= 20) isNewerThan1_19_R2 = true;
            else if (main_ver == 19) isNewerThan1_19_R2 = Integer.parseInt(split[2].substring(1)) >= 2;
            else isNewerThan1_19_R2 = false;
        }
        return isNewerThan1_19_R2;
    }

    private void disableUseLessInfo() {
        MinecraftVersion.disableBStats();
        MinecraftVersion.disableUpdateCheck();
        VersionChecker.hideOk = true;
        try {
            Field field = MinecraftVersion.class.getDeclaredField("version");
            field.setAccessible(true);
            MinecraftVersion minecraftVersion;
            try {
                minecraftVersion = MinecraftVersion.valueOf(serverVersion.replace("v", "MC"));
            } catch (IllegalArgumentException ex) {
                minecraftVersion = MinecraftVersion.UNKNOWN;
            }
            field.set(MinecraftVersion.class, minecraftVersion);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        boolean hasGsonSupport;
        try {
            Class.forName("com.google.gson.Gson");
            hasGsonSupport = true;
        } catch (Exception ex) {
            hasGsonSupport = false;
        }
        try {
            Field field= MinecraftVersion.class.getDeclaredField("hasGsonSupport");
            field.setAccessible(true);
            field.set(Boolean.class, hasGsonSupport);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void checkUpdate() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL("https://api.polymart.org/v1/getResourceInfoSimple/?resource_id=2723&key=version");
                URLConnection conn = url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(60000);
                InputStream inputStream = conn.getInputStream();
                String newest = new BufferedReader(new InputStreamReader(inputStream)).readLine();
                String current = plugin.getDescription().getVersion();
                inputStream.close();

                if (!compareVer(newest, current)) {
                    AdventureUtil.consoleMessage(ConfigManager.lang.equalsIgnoreCase("chinese") ? "[CustomFishing] 当前已是最新版本" : "[CustomFishing] You are using the latest version.");
                    return;
                }

                if (ConfigManager.lang.equalsIgnoreCase("chinese")) {
                    AdventureUtil.consoleMessage("[CustomFishing] 当前版本: <red>" + current);
                    AdventureUtil.consoleMessage("[CustomFishing] 最新版本: <green>" + newest);
                    AdventureUtil.consoleMessage("[CustomFishing] 请到 <u>售后群<!u> 或 <u>https://polymart.org/resource/customfishing.2723<!u> 获取最新版本.");
                }
                else {
                    AdventureUtil.consoleMessage("[CustomFishing] Current version: <red>" + current);
                    AdventureUtil.consoleMessage("[CustomFishing] Latest version: <green>" + newest);
                    AdventureUtil.consoleMessage("[CustomFishing] Update is available: <u>https://polymart.org/resource/customfishing.2723<!u>");
                }
            } catch (Exception exception) {
                Log.warn("Error occurred when checking update");
            }
        });
    }

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
                    // hotfix2 & hotfix
                    if (newHotfix.length == 2 && currentHotfix.length == 1) return true;
                    // hotfix3 & hotfix2
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
        // if common parts are the same, the longer is newer
        return newVS.length > currentVS.length;
    }

    public boolean isSpigot() {
        return isSpigot;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }
}
