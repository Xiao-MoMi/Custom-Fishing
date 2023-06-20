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

import net.momirealms.customfishing.fishing.requirements.Requirement;
import net.momirealms.customfishing.fishing.requirements.RequirementInterface;
import net.momirealms.customfishing.util.ConfigUtils;
import net.momirealms.customfishing.util.JedisUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

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
    public static boolean hideSaveInfo;
    public static boolean baitAnimation;
    public static int corePoolSize;
    public static int maximumPoolSize;
    public static int keepAliveTime;
    public static String dateFormat;
    public static RequirementInterface[] mechanicRequirements;

    public static void load() {
        ConfigUtils.update("config.yml", new ArrayList<>());
        YamlConfiguration config = ConfigUtils.getConfig("config.yml");
        lang = config.getString("lang","english");
        bStats = config.getBoolean("metrics", true);
        updateChecker = config.getBoolean("update-checker", true);
        loadMechanics(Objects.requireNonNull(config.getConfigurationSection("mechanics")));
        loadTitle(Objects.requireNonNull(config.getConfigurationSection("titles")));
        loadFishingWorlds(Objects.requireNonNull(config.getConfigurationSection("worlds")));
        loadOtherSettings(Objects.requireNonNull(config.getConfigurationSection("other-settings")));
    }

    private static void loadOtherSettings(ConfigurationSection config) {
        priority = config.getString("event-priority", "NORMAL").toUpperCase();
        disableJobsXp = config.getBoolean("disable-JobsReborn-fishing-exp", false);
        preventPickUp = config.getBoolean("prevent-other-players-pick-up-loot", false);
        convertMMOItems = config.getBoolean("convert-MMOItems-rods", false);
        logEarning = config.getBoolean("log-earnings", true);
        hideSaveInfo = config.getBoolean("hide-data-saving-info", false);
        corePoolSize = config.getInt("thread-pool-settings.corePoolSize", 1);
        maximumPoolSize = config.getInt("thread-pool-settings.maximumPoolSize", 4);
        keepAliveTime = config.getInt("thread-pool-settings.keepAliveTime", 10);
        dateFormat = config.getString("date-format", "yyyy-MM-dd");
    }

    private static void loadMechanics(ConfigurationSection config) {
        disableBar = config.getBoolean("disable-bar-mechanic", false);
        instantBar = config.getBoolean("instant-bar", false);
        enableVanillaLoot = config.getBoolean("other-loots.vanilla.enable", true);
        vanillaLootRatio = config.getDouble("other-loots.vanilla.ratio", 0.4);
        enableMcMMOLoot = config.getBoolean("other-loots.mcMMO.enable", false);
        mcMMOLootChance = config.getDouble("other-loots.mcMMO.chance", 0.5);
        needRodToFish = config.getBoolean("need-special-rod-to-fish", false);
        needRodForLoot = config.getBoolean("need-special-rod-for-loots", false);
        rodLoseDurability = config.getBoolean("rod-lose-durability", true);
        enableCompetition = config.getBoolean("fishing-competition.enable", true);
        enableWaterAnimation = config.getBoolean("splash-animation.water.enable", false);
        enableLavaAnimation = config.getBoolean("splash-animation.lava.enable", false);
        allRodsFishInLava = config.getBoolean("all-rods-fish-in-lava", false);
        water_item = config.getString("splash-animation.water.item");
        lava_item = config.getString("splash-animation.lava.item");
        water_time = config.getInt("splash-animation.water.time");
        lava_time = config.getInt("splash-animation.lava.time");
        lavaMinTime = config.getInt("lava-fishing.min-wait-time", 100);
        lavaMaxTime = config.getInt("lava-fishing.max-wait-time", 600) - lavaMinTime;
        enableFishingBag = config.getBoolean("fishing-bag.enable", true);
        canStoreLoot = config.getBoolean("fishing-bag.can-store-loot", false);
        addTagToFish = config.getBoolean("add-custom-fishing-tags-to-loots", true);
        fishingBagTitle = config.getString("fishing-bag.bag-title", "Fishing Bag");
        enableStatistics = config.getBoolean("fishing-statistics.enable", true);
        baitAnimation = config.getBoolean("bait-animation", true);
        mechanicRequirements = ConfigUtils.getRequirementsWithMsg(config.getConfigurationSection("mechanic-requirements"));
        bagWhiteListItems = new HashSet<>();
        for (String material : config.getStringList("fishing-bag.whitelist-items")) bagWhiteListItems.add(Material.valueOf(material.toUpperCase()));
        redisSettings(config);
    }

    private static void loadTitle(ConfigurationSection config) {
        enableSuccessTitle = config.getBoolean("success.enable", true);
        enableFailureTitle = config.getBoolean("failure.enable", true);
        successTitle = config.getStringList("success.title").toArray(new String[0]);
        successSubTitle = config.getStringList("success.subtitle").toArray(new String[0]);
        successFadeIn = config.getInt("success.fade.in", 10) * 50;
        successFadeStay = config.getInt("success.fade.stay", 30) * 50;
        successFadeOut = config.getInt("success.fade.out", 10) * 50;
        failureTitle = config.getStringList("failure.title").toArray(new String[0]);
        failureSubTitle = config.getStringList("failure.subtitle").toArray(new String[0]);
        failureFadeIn = config.getInt("failure.fade.in", 10) * 50;
        failureFadeStay = config.getInt("failure.fade.stay", 30) * 50;
        failureFadeOut = config.getInt("failure.fade.out", 10) * 50;
        if (successTitle.length == 0) successTitle = new String[]{""};
        if (successSubTitle.length == 0) successSubTitle = new String[]{""};
        if (failureTitle.length == 0) failureTitle = new String[]{""};
        if (failureSubTitle.length == 0) failureSubTitle = new String[]{""};
    }

    private static void loadFishingWorlds(ConfigurationSection config) {
        whiteOrBlack = config.getString("mode","whitelist").equals("whitelist");
        worldList = config.getStringList("list");
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

    private static void redisSettings(ConfigurationSection config) {
        if (enableCompetition && config.getBoolean("fishing-competition.redis", false)) {
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
