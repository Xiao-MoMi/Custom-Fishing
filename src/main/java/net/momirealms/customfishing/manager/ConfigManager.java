package net.momirealms.customfishing.manager;

import net.momirealms.customfishing.util.ConfigUtil;
import net.momirealms.customfishing.util.JedisUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
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
    public static boolean needOpenWater;
    public static boolean needRodForLoots;
    public static boolean needRodToFish;
    public static boolean rodLoseDurability;
    public static int fishFinderCoolDown;
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

        alwaysFishingBar = config.getBoolean("mechanics.other-loots.fishing-bar", true);
        otherLootBar = config.getBoolean("mechanics.other-loots.fishing-bar", true);
        enableVanillaLoot = config.getBoolean("mechanics.other-loots.vanilla.enable", true);
        vanillaLootRatio = config.getDouble("mechanics.other-loots.vanilla.ratio", 0.4);
        enableMcMMOLoot = config.getBoolean("mechanics.other-loots.mcMMO.enable", false);
        mcMMOLootChance = config.getDouble("mechanics.other-loots.mcMMO.chance", 0.5);
        needOpenWater = config.getBoolean("mechanics.need-open-water", true);
        needRodForLoots = config.getBoolean("mechanics.need-special-rod.for-loots", false);
        needRodToFish = config.getBoolean("mechanics.need-special-rod.to-fish", false);
        rodLoseDurability = config.getBoolean("mechanics.rod-lose-durability", true);
        fishFinderCoolDown = config.getInt("mechanics.fishfinder-cooldown", 3000);
        enableCompetition = config.getBoolean("mechanics.fishing-competition", true);

        priority = config.getString("other-settings.event-priority", "NORMAL").toUpperCase();
        disableJobsXp = config.getBoolean("other-settings.disable-JobsReborn-fishing-exp", false);
        preventPickUp = config.getBoolean("other-settings.prevent-other-players-pick-up-loot", false);
        convertMMOItems = config.getBoolean("other-settings.convert-MMOItems-rods", false);

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

        tryEnableJedis();
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

    public static void tryEnableJedis(){
        YamlConfiguration configuration = ConfigUtil.getConfig("redis.yml");
        useRedis = false;
        if (configuration.getBoolean("redis.enable")){
            JedisUtil.initializeRedis(configuration);
            useRedis = true;
        }
    }
}
