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

package net.momirealms.customfishing.manager;

import net.momirealms.customfishing.util.ConfigUtils;
import net.momirealms.customfishing.util.JedisUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ConfigManager {

    public static List<String> worldList;
    public static boolean whiteOrBlack;
    public static String priority;
    public static String lang;
    public static boolean enableVanillaLoot;
    public static boolean enableMcMMOLoot;
    public static double vanillaLootRatio;
    public static double mcMMOLootChance;
    public static boolean needRodToFish;
    public static boolean needRodForLoot;
    public static boolean rodLoseDurability;
    public static boolean enableCompetition;
    public static boolean disableJobsXp;
    public static boolean convertMMOItems;
    public static boolean preventPickUp;
    public static boolean enableFishingBag;
    public static boolean allRodsFishInLava;
    public static boolean enableSuccessTitle;
    public static String[] successTitle;
    public static String[] successSubTitle;
    public static int successFadeIn;
    public static int successFadeStay;
    public static int successFadeOut;
    public static boolean enableFailureTitle;
    public static String[] failureTitle;
    public static String[] failureSubTitle;
    public static int failureFadeIn;
    public static int failureFadeStay;
    public static int failureFadeOut;
    public static boolean useRedis;
    public static boolean canStoreLoot;
    public static int lavaMaxTime;
    public static int lavaMinTime;
    public static boolean enableWaterAnimation;
    public static boolean enableLavaAnimation;
    public static String water_item;
    public static String lava_item;
    public static int water_time;
    public static int lava_time;
    public static boolean addTagToFish;
    public static boolean logEarning;
    public static boolean disableBar;
    public static boolean instantBar;
    public static String fishingBagTitle;
    public static boolean bStats;
    public static HashSet<Material> bagWhiteListItems;
    public static boolean enableStatistics;
    public static boolean updateChecker;

    public static void load() {
        ConfigUtils.update("config.yml");
        YamlConfiguration config = ConfigUtils.getConfig("config.yml");
        lang = config.getString("lang","english");
        bStats = config.getBoolean("metrics", true);
        updateChecker = config.getBoolean("update-checker", true);
        loadMechanics(config);
        loadTitle(config);
        loadFishingWorlds(config);
        loadOtherSettings(config);
    }

    private static void loadOtherSettings(YamlConfiguration config) {
        priority = config.getString("other-settings.event-priority", "NORMAL").toUpperCase();
        disableJobsXp = config.getBoolean("other-settings.disable-JobsReborn-fishing-exp", false);
        preventPickUp = config.getBoolean("other-settings.prevent-other-players-pick-up-loot", false);
        convertMMOItems = config.getBoolean("other-settings.convert-MMOItems-rods", false);
        logEarning = config.getBoolean("other-settings.log-earnings", true);
    }

    private static void loadMechanics(YamlConfiguration config) {
        disableBar = config.getBoolean("mechanics.disable-bar-mechanic", false);
        instantBar = config.getBoolean("mechanics.instant-bar", false);
        enableVanillaLoot = config.getBoolean("mechanics.other-loots.vanilla.enable", true);
        vanillaLootRatio = config.getDouble("mechanics.other-loots.vanilla.ratio", 0.4);
        enableMcMMOLoot = config.getBoolean("mechanics.other-loots.mcMMO.enable", false);
        mcMMOLootChance = config.getDouble("mechanics.other-loots.mcMMO.chance", 0.5);
        needRodToFish = config.getBoolean("mechanics.need-special-rod-to-fish", false);
        needRodForLoot = config.getBoolean("mechanics.need-special-rod-for-loots", false);
        rodLoseDurability = config.getBoolean("mechanics.rod-lose-durability", true);
        enableCompetition = config.getBoolean("mechanics.fishing-competition.enable", true);
        enableWaterAnimation = config.getBoolean("mechanics.splash-animation.water.enable", false);
        enableLavaAnimation = config.getBoolean("mechanics.splash-animation.lava.enable", false);
        allRodsFishInLava = config.getBoolean("mechanics.all-rods-fish-in-lava", false);
        water_item = config.getString("mechanics.splash-animation.water.item");
        lava_item = config.getString("mechanics.splash-animation.lava.item");
        water_time = config.getInt("mechanics.splash-animation.water.time");
        lava_time = config.getInt("mechanics.splash-animation.lava.time");
        lavaMinTime = config.getInt("mechanics.lava-fishing.min-wait-time", 100);
        lavaMaxTime = config.getInt("mechanics.lava-fishing.max-wait-time", 600) - lavaMinTime;
        enableFishingBag = config.getBoolean("mechanics.fishing-bag.enable", true);
        canStoreLoot = config.getBoolean("mechanics.fishing-bag.can-store-loot", false);
        addTagToFish = config.getBoolean("mechanics.add-custom-fishing-tags-to-loots", true);
        fishingBagTitle = config.getString("mechanics.fishing-bag.bag-title", "Fishing Bag");
        enableStatistics = config.getBoolean("mechanics.fishing-statistics.enable", true);
        bagWhiteListItems = new HashSet<>();
        for (String material : config.getStringList("mechanics.fishing-bag.whitelist-items")) bagWhiteListItems.add(Material.valueOf(material.toUpperCase()));
        redisSettings(config);
    }

    private static void loadTitle(YamlConfiguration config) {
        enableSuccessTitle = config.getBoolean("titles.success.enable", true);
        enableFailureTitle = config.getBoolean("titles.failure.enable", true);
        successTitle = config.getStringList("titles.success.title").toArray(new String[0]);
        successSubTitle = config.getStringList("titles.success.subtitle").toArray(new String[0]);
        successFadeIn = config.getInt("titles.success.fade.in", 10) * 50;
        successFadeStay = config.getInt("titles.success.fade.stay", 30) * 50;
        successFadeOut = config.getInt("titles.success.fade.out", 10) * 50;
        failureTitle = config.getStringList("titles.failure.title").toArray(new String[0]);
        failureSubTitle = config.getStringList("titles.failure.subtitle").toArray(new String[0]);
        failureFadeIn = config.getInt("titles.failure.fade.in", 10) * 50;
        failureFadeStay = config.getInt("titles.failure.fade.stay", 30) * 50;
        failureFadeOut = config.getInt("titles.failure.fade.out", 10) * 50;
        if (successTitle.length == 0) successTitle = new String[]{""};
        if (successSubTitle.length == 0) successSubTitle = new String[]{""};
        if (failureTitle.length == 0) failureTitle = new String[]{""};
        if (failureSubTitle.length == 0) failureSubTitle = new String[]{""};
    }

    private static void loadFishingWorlds(YamlConfiguration config) {
        whiteOrBlack = config.getString("worlds.mode","whitelist").equals("whitelist");
        worldList = config.getStringList("worlds.list");
    }

    public static List<String> getWorldsList() {
        if (whiteOrBlack) {
            return worldList;
        }
        else {
            List<String> worldList = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                worldList.add(world.getName());
            }
            worldList.removeAll(ConfigManager.worldList);
            return worldList;
        }
    }

    private static void redisSettings(YamlConfiguration config) {
        if (enableCompetition && config.getBoolean("mechanics.fishing-competition.redis", false)) {
            if (!JedisUtils.isPoolEnabled()) {
                YamlConfiguration configuration = ConfigUtils.getConfig("database.yml");
                JedisUtils.initializeRedis(configuration);
            }
            useRedis = true;
        }
        else if (useRedis && JedisUtils.isPoolEnabled()) {
            JedisUtils.closePool();
            useRedis = false;
        }
    }
}
