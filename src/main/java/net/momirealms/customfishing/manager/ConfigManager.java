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

import net.momirealms.customfishing.util.ConfigUtil;
import net.momirealms.customfishing.util.JedisUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ConfigManager {

    public static World[] worlds;
    public static List<World> worldList;
    public static boolean whiteOrBlack;
    public static String priority;
    public static String lang;
    public static boolean otherLootBar;
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
    public static boolean alwaysFishingBar;
    public static String[] successTitle;
    public static String[] successSubTitle;
    public static int successFadeIn;
    public static int successFadeStay;
    public static int successFadeOut;
    public static String[] failureTitle;
    public static String[] failureSubTitle;
    public static int failureFadeIn;
    public static int failureFadeStay;
    public static int failureFadeOut;
    public static boolean useRedis;
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
    public static boolean vaultHook;
    public static boolean disableBar;
    public static boolean instantBar;
    public static String fishingBagTitle;
    public static HashSet<Material> bagWhiteListItems;

    public static void load() {
        ConfigUtil.update("config.yml");
        YamlConfiguration config = ConfigUtil.getConfig("config.yml");

        lang = config.getString("lang","english");

        whiteOrBlack = config.getString("worlds.mode","whitelist").equals("whitelist");
        List<String> worldsName = config.getStringList("worlds.list");
        worlds = new World[worldsName.size()];
        for (int i = 0; i < worldsName.size(); i++) {
            if (Bukkit.getWorld(worldsName.get(i)) != null) {
                worlds[i] = Bukkit.getWorld(worldsName.get(i));
            }
        }
        worldList = new ArrayList<>();
        for (World world : worlds) {
            if (world == null) continue;
            worldList.add(world);
        }
        worlds = worldList.toArray(new World[0]);

        disableBar = config.getBoolean("mechanics.disable-bar-mechanic", false);
        instantBar = config.getBoolean("mechanics.instant-bar", false);
        alwaysFishingBar = config.getBoolean("mechanics.other-loots.fishing-bar", true);
        otherLootBar = config.getBoolean("mechanics.other-loots.fishing-bar", true);
        enableVanillaLoot = config.getBoolean("mechanics.other-loots.vanilla.enable", true);
        vanillaLootRatio = config.getDouble("mechanics.other-loots.vanilla.ratio", 0.4);
        enableMcMMOLoot = config.getBoolean("mechanics.other-loots.mcMMO.enable", false);
        mcMMOLootChance = config.getDouble("mechanics.other-loots.mcMMO.chance", 0.5);
        needRodToFish = config.getBoolean("mechanics.need-special-rod-to-fish", false);
        needRodForLoot = config.getBoolean("mechanics.need-special-rod-for-loots", false);
        rodLoseDurability = config.getBoolean("mechanics.rod-lose-durability", true);
        enableCompetition = config.getBoolean("mechanics.fishing-competition.enable", true);

        priority = config.getString("other-settings.event-priority", "NORMAL").toUpperCase();
        disableJobsXp = config.getBoolean("other-settings.disable-JobsReborn-fishing-exp", false);
        preventPickUp = config.getBoolean("other-settings.prevent-other-players-pick-up-loot", false);
        convertMMOItems = config.getBoolean("other-settings.convert-MMOItems-rods", false);
        logEarning = config.getBoolean("other-settings.log-earnings", true);
        vaultHook = config.getBoolean("integration.Vault", true);

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

        enableWaterAnimation = config.getBoolean("mechanics.splash-animation.water.enable", false);
        enableLavaAnimation = config.getBoolean("mechanics.splash-animation.lava.enable", false);
        water_item = config.getString("mechanics.splash-animation.water.item");
        lava_item = config.getString("mechanics.splash-animation.lava.item");
        water_time = config.getInt("mechanics.splash-animation.water.time");
        lava_time = config.getInt("mechanics.splash-animation.lava.time");

        lavaMinTime = config.getInt("mechanics.lava-fishing.min-wait-time", 100);
        lavaMaxTime = config.getInt("mechanics.lava-fishing.max-wait-time", 600) - lavaMinTime;

        enableFishingBag = config.getBoolean("mechanics.fishing-bag.enable", true);
        addTagToFish = config.getBoolean("mechanics.fishing-bag.can-store-loot", false);
        fishingBagTitle = config.getString("mechanics.fishing-bag.bag-title", "Fishing Bag");
        bagWhiteListItems = new HashSet<>();
        for (String material : config.getStringList("mechanics.fishing-bag.whitelist-items")) {
            bagWhiteListItems.add(Material.valueOf(material.toUpperCase()));
        }

        useRedis = false;
        if (enableCompetition && config.getBoolean("mechanics.fishing-competition.redis", false)) {
            YamlConfiguration configuration = ConfigUtil.getConfig("database.yml");
            JedisUtil.initializeRedis(configuration);
            useRedis = true;
        }
    }
    public static List<World> getWorldsList() {
        if (whiteOrBlack) {
            return worldList;
        }
        else {
            List<World> worldList = new ArrayList<>(Bukkit.getWorlds());
            worldList.removeAll(ConfigManager.worldList);
            return worldList;
        }
    }
}
