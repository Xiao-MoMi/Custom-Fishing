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

package net.momirealms.customfishing;

import net.kyori.adventure.bossbar.BossBar;
import net.momirealms.customcrops.helper.Log;
import net.momirealms.customfishing.competition.CompetitionConfig;
import net.momirealms.customfishing.competition.Goal;
import net.momirealms.customfishing.competition.bossbar.BossBarConfig;
import net.momirealms.customfishing.competition.reward.CommandImpl;
import net.momirealms.customfishing.competition.reward.MessageImpl;
import net.momirealms.customfishing.competition.reward.Reward;
import net.momirealms.customfishing.hook.Placeholders;
import net.momirealms.customfishing.hook.skill.*;
import net.momirealms.customfishing.titlebar.Difficulty;
import net.momirealms.customfishing.titlebar.Layout;
import net.momirealms.customfishing.item.Bait;
import net.momirealms.customfishing.item.Loot;
import net.momirealms.customfishing.item.Rod;
import net.momirealms.customfishing.item.Util;
import net.momirealms.customfishing.requirements.*;
import net.momirealms.customfishing.utils.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class ConfigReader{

    public static HashMap<String, Loot> LOOT = new HashMap<>();
    public static HashMap<String, ItemStack> LOOTITEM = new HashMap<>();
    public static HashMap<String, Util> UTIL = new HashMap<>();
    public static HashMap<String, ItemStack> UTILITEM = new HashMap<>();
    public static HashMap<String, Rod> ROD = new HashMap<>();
    public static HashMap<String, ItemStack> RODITEM = new HashMap<>();
    public static HashMap<String, Bait> BAIT = new HashMap<>();
    public static HashMap<String, ItemStack> BAITITEM = new HashMap<>();
    public static HashMap<String, Layout> LAYOUT = new HashMap<>();
    public static HashMap<String, CompetitionConfig> Competitions = new HashMap<>();
    public static HashMap<String, CompetitionConfig> CompetitionsCommand = new HashMap<>();


    public static YamlConfiguration getConfig(String configName) {
        File file = new File(CustomFishing.instance.getDataFolder(), configName);
        if (!file.exists()) {
            CustomFishing.instance.saveResource(configName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void Reload() {
        Config.loadConfig();
        Message.loadMessage();
        Title.loadTitle();
        loadBars();
        loadLoot();
        loadUtil();
        loadRod();
        loadBait();
        loadCompetitions();
    }

    public static class Config {

        public static boolean wg;
        public static boolean mm;
        public static boolean papi;
        public static boolean season;
        public static boolean vanillaDrop;
        public static boolean needOpenWater;
        public static boolean needSpecialRod;
        public static boolean competition;
        public static boolean convertMMOItems;
        public static boolean loseDurability;
        public static boolean rsSeason;
        public static boolean ccSeason;
        public static String season_papi;
        public static String lang;
        public static int fishFinderCoolDown;
        public static double timeMultiply;
        public static SkillXP skillXP;
        public static String version;

        public static void loadConfig() {

            CustomFishing.instance.saveDefaultConfig();
            CustomFishing.instance.reloadConfig();
            FileConfiguration config = CustomFishing.instance.getConfig();

            wg = config.getBoolean("config.integrations.WorldGuard");
            if (wg){
                if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null){
                    AdventureManager.consoleMessage("<red>[CustomFishing] Failed to initialize WorldGuard!</red>");
                    wg = false;
                }else {
                    AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><color:#00BFFF>WorldGuard <color:#E1FFFF>Hooked!");
                }
            }
            mm = config.getBoolean("config.integrations.MythicMobs");
            if (mm){
                if (Bukkit.getPluginManager().getPlugin("MythicMobs") == null){
                    AdventureManager.consoleMessage("<red>[CustomFishing] Failed to initialize MythicMobs!</red>");
                    mm = false;
                }else {
                    AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><color:#00BFFF>MythicMobs <color:#E1FFFF>Hooked!");
                }
            }
            papi = config.getBoolean("config.integrations.PlaceholderAPI");
            if (papi){
                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null){
                    AdventureManager.consoleMessage("<red>[CustomFishing] Failed to initialize PlaceholderAPI!</red>");
                    papi = false;
                }
            }

            skillXP = null;

            if(config.getBoolean("config.integrations.mcMMO",false)){
                if(Bukkit.getPluginManager().getPlugin("mcMMO") == null){
                    CustomFishing.instance.getLogger().warning("Failed to initialize mcMMO!");
                }else {
                    skillXP = new mcMMO();
                    AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><color:#00BFFF>mcMMO <color:#E1FFFF>Hooked!");
                }
            }
            if(config.getBoolean("config.integrations.AureliumSkills",false)){
                if(Bukkit.getPluginManager().getPlugin("AureliumSkills") == null){
                    CustomFishing.instance.getLogger().warning("Failed to initialize AureliumSkills!");
                }else {
                    skillXP = new Aurelium();
                    AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><color:#00BFFF>AureliumSkills <color:#E1FFFF>Hooked!");
                }
            }
            if(config.getBoolean("config.integrations.MMOCore",false)){
                if(Bukkit.getPluginManager().getPlugin("MMOCore") == null){
                    CustomFishing.instance.getLogger().warning("Failed to initialize MMOCore!");
                }else {
                    skillXP = new MMOCore();
                    AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><color:#00BFFF>MMOCore <color:#E1FFFF>Hooked!");
                }
            }
            if(config.getBoolean("config.integrations.EcoSkills",false)){
                if(Bukkit.getPluginManager().getPlugin("EcoSkills") == null){
                    CustomFishing.instance.getLogger().warning("Failed to initialize EcoSkills!");
                }else {
                    skillXP = new EcoSkill();
                    AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><color:#00BFFF>EcoSkills <color:#E1FFFF>Hooked!");
                }
            }

            season = config.getBoolean("config.season.enable");
            if (!papi && season) {
                season = false;
            }else {
                season_papi = config.getString("config.season.papi");
            }

            rsSeason = false;
            if (config.getBoolean("config.integrations.RealisticSeasons",false)){
                if (Bukkit.getPluginManager().getPlugin("RealisticSeasons") == null) Log.warn("Failed to initialize RealisticSeasons!");
                else {
                    rsSeason = true;
                    AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><color:#00BFFF>RealisticSeasons <color:#E1FFFF>Hooked!");
                }
            }
            ccSeason = false;
            if (config.getBoolean("config.integrations.CustomCrops",false)){
                if (Bukkit.getPluginManager().getPlugin("CustomCrops") == null) Log.warn("Failed to initialize CustomCrops!");
                else {
                    ccSeason = true;
                    AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><color:#00BFFF>CustomCrops <color:#E1FFFF>Hooked!");
                }
            }

            if (rsSeason || ccSeason){
                season = true;
            }

            vanillaDrop = config.getBoolean("config.vanilla-loot-when-no-custom-fish", true);
            convertMMOItems = config.getBoolean("config.convert-MMOITEMS", false);
            needOpenWater = config.getBoolean("config.need-open-water", false);
            needSpecialRod = config.getBoolean("config.need-special-rod", false);
            loseDurability = config.getBoolean("config.rod-lose-durability", true);

            version = config.getString("config-version");
            fishFinderCoolDown = config.getInt("config.fishfinder-cooldown");
            timeMultiply = config.getDouble("config.time-multiply");
            lang = config.getString("config.lang","cn");
            competition = config.getBoolean("config.fishing-competition",true);
        }
    }

    public static class Message {
        public static String prefix;
        public static String reload;
        public static String escape;
        public static String noPerm;
        public static String notExist;
        public static String noConsole;
        public static String wrongAmount;
        public static String lackArgs;
        public static String notOnline;
        public static String giveItem;
        public static String getItem;
        public static String coolDown;
        public static String possibleLoots;
        public static String splitChar;
        public static String noLoot;
        public static String notOpenWater;
        public static String competitionOn;
        public static String notEnoughPlayers;
        public static String noRank;
        public static String forceSuccess;
        public static String forceFailure;
        public static String forceEnd;
        public static String forceCancel;
        public static String noPlayer;
        public static String noScore;
        public static void loadMessage() {
            YamlConfiguration config = getConfig("messages/messages_" + Config.lang +".yml");
            prefix = config.getString("messages.prefix");
            reload = config.getString("messages.reload");
            escape = config.getString("messages.escape");
            noPerm = config.getString("messages.no-perm");
            notExist = config.getString("messages.not-exist");
            noConsole = config.getString("messages.no-console");
            wrongAmount = config.getString("messages.wrong-amount");
            lackArgs = config.getString("messages.lack-args");
            notOnline = config.getString("messages.not-online");
            giveItem = config.getString("messages.give-item");
            getItem = config.getString("messages.get-item");
            coolDown = config.getString("messages.cooldown");
            possibleLoots = config.getString("messages.possible-loots");
            splitChar = config.getString("messages.split-char");
            noLoot = config.getString("messages.no-loot");
            notOpenWater = config.getString("messages.not-open-water");
            competitionOn = config.getString("messages.competition-ongoing");
            notEnoughPlayers = config.getString("messages.players-not-enough");
            noRank = config.getString("messages.no-rank");
            forceSuccess = config.getString("messages.force-competition-success");
            forceFailure = config.getString("messages.force-competition-failure");
            forceEnd = config.getString("messages.force-competition-end");
            forceCancel = config.getString("messages.force-competition-cancel");
            noPlayer = config.getString("messages.no-player");
            noScore = config.getString("messages.no-score");
        }
    }

    public static class Title {
        public static List<String> success_title;
        public static List<String> success_subtitle;
        public static int success_in;
        public static int success_out;
        public static int success_stay;
        public static List<String> failure_title;
        public static List<String> failure_subtitle;
        public static int failure_in;
        public static int failure_out;
        public static int failure_stay;
        public static void loadTitle() {
            YamlConfiguration config = getConfig("titles.yml");
            success_title = config.getStringList("titles.success.title");
            success_subtitle = config.getStringList("titles.success.subtitle");
            success_in = config.getInt("titles.success.fade.in")*50;
            success_out = config.getInt("titles.success.fade.out")*50;
            success_stay = config.getInt("titles.success.fade.stay")*50;
            failure_title = config.getStringList("titles.failure.title");
            failure_subtitle = config.getStringList("titles.failure.subtitle");
            failure_in = config.getInt("titles.failure.fade.in")*50;
            failure_out = config.getInt("titles.failure.fade.out")*50;
            failure_stay = config.getInt("titles.failure.fade.stay")*50;
        }
    }

    /*
    载入Loot战利品
     */
    public static void loadLoot() {

        LOOT.clear();
        LOOTITEM.clear();

        YamlConfiguration config = getConfig("loots.yml");
        Set<String> keys = Objects.requireNonNull(config.getConfigurationSection("items")).getKeys(false);
        keys.forEach(key -> {

            Difficulty difficulty;
            if (config.contains("items." + key + ".difficulty")) {
                String[] split = StringUtils.split(config.getString("items." + key + ".difficulty"), "-");
                assert split != null;
                if (Integer.parseInt(split[1]) <= 0 || Integer.parseInt(split[0]) <= 0){
                    AdventureManager.consoleMessage("<red>[CustomFishing] Error! " + key + " has wrong difficulty format!</red>");
                    return;
                }else {
                    difficulty = new Difficulty(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                }
            } else {
                difficulty = new Difficulty(1, 1);
            }
            int weight;
            if (config.contains("items." + key + ".weight")) {
                weight = config.getInt("items." + key + ".weight");
            } else {
                AdventureManager.consoleMessage("<red>[CustomFishing] Error! No weight set for " + key + " !</red>");
                return;
            }
            int time;
            if (config.contains("items." + key + ".time")) {
                time = config.getInt("items." + key + ".time");
                if (time <= 0){
                    AdventureManager.consoleMessage("<red>[CustomFishing] Error! " + key + " time must be positive!</red>");
                    return;
                }
            } else {
                time = 10000;
            }

            Loot loot = new Loot(key, difficulty, weight, time);

            if (config.contains("items." + key + ".material")) {
                loot.setMaterial(config.getString("items." + key + ".material"));
            } else {
                AdventureManager.consoleMessage("<red>[CustomFishing] Error! No material set for " + key + " !</red>");
                return;
            }
            /*
            可选的设置内容
             */
            if (config.contains("items." + key + ".display.lore"))
                loot.setLore(config.getStringList("items." + key + ".display.lore"));
            if (config.contains("items." + key + ".display.name"))
                loot.setName(config.getString("items." + key + ".display.name"));
            if (config.contains("items." + key + ".enchantments")) {
                ArrayList<Enchantment> arrayList = new ArrayList<>();
                config.getStringList("items." + key + ".enchantments").forEach(enchant -> {
                    String[] split = StringUtils.split(enchant, "/");
                    NamespacedKey namespacedKey = NamespacedKey.fromString(split[0]);
                    arrayList.add(new Enchantment(namespacedKey, Integer.parseInt(split[1])));
                });
                loot.setEnchantment(arrayList);
            }
            if (config.contains("items." + key + ".item_flags")) {
                ArrayList<ItemFlag> arrayList = new ArrayList<>();
                config.getStringList("items." + key + ".item_flags").forEach(flag -> {
                    arrayList.add(ItemFlag.valueOf(flag));
                });
                loot.setItemFlags(arrayList);
            }
            if (config.contains("items." + key + ".nbt"))
                loot.setNbt((Map<String, Object>) config.getMapList("items." + key + ".nbt").get(0));
            if (config.contains("items." + key + ".custom-model-data"))
                loot.setCustommodeldata(config.getInt("items." + key + ".custom-model-data"));
            if (config.contains("items."+ key +".nick")){
                loot.setNick(config.getString("items."+key+".nick"));
            }else {
                loot.setNick(loot.getName());
            }
            loot.setUnbreakable(config.getBoolean("items." + key + ".unbreakable",false));
            loot.setScore((float) config.getDouble("items." + key + ".score",0));

            if (config.contains("items." + key + ".action.message"))
                loot.setMsg(config.getStringList("items." + key + ".action.message"));
            if (config.contains("items." + key + ".action.command"))
                loot.setCommands(config.getStringList("items." + key + ".action.command"));
            if (config.contains("items." + key + ".action-hook.message"))
                loot.setHookMsg(config.getStringList("items." + key + ".action-hook.message"));
            if (config.contains("items." + key + ".action-hook.command"))
                loot.setHookCommands(config.getStringList("items." + key + ".action-hook.command"));
            if (config.contains("items." + key + ".action.exp"))
                loot.setExp(config.getInt("items." + key + ".action.exp"));
            if (config.contains("items." + key + ".layout"))
                loot.setLayout(config.getString("items." + key + ".layout"));
            if (config.contains("items." + key + ".skill-xp"))
                loot.setSkillXP(config.getDouble("items." + key + ".skill-xp"));
            if (config.contains("items." + key + ".group"))
                loot.setGroup(config.getString("items." + key + ".group"));
            if (config.contains("items." + key + ".show-in-fishfinder")){
                loot.setShowInFinder(config.getBoolean("items." + key + ".show-in-fishfinder"));
            }else {
                loot.setShowInFinder(true);
            }
            /*
            设置捕获条件
             */
            if (config.contains("items." + key + ".requirements")){
                List<Requirement> requirements = new ArrayList<>();
                Objects.requireNonNull(config.getConfigurationSection("items." + key + ".requirements")).getKeys(false).forEach(requirement -> {
                    switch (requirement){
                        case "weather" -> requirements.add(new Weather(config.getStringList("items." + key + ".requirements.weather")));
                        case "ypos" -> requirements.add(new YPos(config.getStringList("items." + key + ".requirements.ypos")));
                        case "season" -> {
                            if (Config.season){
                                requirements.add(new Season(config.getStringList("items." + key + ".requirements.season")));
                            }else {
                                AdventureManager.consoleMessage("<red>[CustomFishing] Plz enable season in config.yml!</red>");
                            }
                        }
                        case "world" -> requirements.add(new World(config.getStringList("items." + key + ".requirements.world")));
                        case "biome" -> requirements.add(new Biome(config.getStringList("items." + key + ".requirements.biome")));
                        case "permission" -> requirements.add(new Permission(config.getString("items." + key + ".requirements.permission")));
                        case "region" -> {
                            if (Config.wg){
                                requirements.add(new Region(config.getStringList("items." + key + ".requirements.regions")));
                            }else {
                                AdventureManager.consoleMessage("<red>[CustomFishing] Plz enable WorldGuard Integration!</red>");
                            }
                        }
                        case "time" -> requirements.add(new Time(config.getStringList("items." + key + ".requirements.time")));
                    }
                });
                loot.setRequirements(requirements);
            }
            LOOT.put(key, loot);
            if (loot.getMaterial().equalsIgnoreCase("AIR")){
                LOOTITEM.put(key, new ItemStack(Material.AIR));
            }else {
                LOOTITEM.put(key, ItemStackGenerator.fromItem(loot));
            }
        });

        if (config.contains("mobs") && Config.mm){
            Set<String> mobs = Objects.requireNonNull(config.getConfigurationSection("mobs")).getKeys(false);
            mobs.forEach(key -> {

            /*
            必设置的内容，为构造所需
             */
                String name;
                if (config.contains("mobs." + key + ".name")) {
                    name = config.getString("mobs." + key + ".name");
                } else {
                    AdventureManager.consoleMessage("<red>[CustomFishing] Error! No name set for mob " + key + " !</red>");
                    return;
                }
                Difficulty difficulty;
                if (config.contains("mobs." + key + ".difficulty")) {
                    String[] split = StringUtils.split(config.getString("mobs." + key + ".difficulty"), "-");
                    assert split != null;
                    if (Integer.parseInt(split[1]) <= 0 || Integer.parseInt(split[0]) <= 0){
                        AdventureManager.consoleMessage("<red>[CustomFishing] Error! " + key + " has wrong difficulty format!</red>");
                        return;
                    }else {
                        difficulty = new Difficulty(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                    }
                } else {
                    difficulty = new Difficulty(1, 1);
                }
                int weight;
                if (config.contains("mobs." + key + ".weight")) {
                    weight = config.getInt("mobs." + key + ".weight");
                } else {
                    AdventureManager.consoleMessage("<red>[CustomFishing] Error! No weight set for " + key + " !</red>");
                    return;
                }
                int time;
                if (config.contains("mobs." + key + ".time")) {
                    time = config.getInt("mobs." + key + ".time");
                    if (time <= 0){
                        AdventureManager.consoleMessage("<red>[CustomFishing] Error! " + key + " time must be positive!</red>");
                        return;
                    }
                } else {
                    time = 10000;
                }
                //新建单例
                Loot loot = new Loot(key, difficulty, weight, time);
                //设置昵称
                loot.setNick(name);
                //设置MM怪ID
                if (config.contains("mobs." + key + ".mythicmobsID")) {
                    loot.setMm(config.getString("mobs." + key + ".mythicmobsID"));
                } else {
                    AdventureManager.consoleMessage("<red>[CustomFishing] Error! No MythicMobs id set for " + key + " !</red>");
                    return;
                }
                //设置MM怪位移
                if (config.contains("mobs." + key + ".vector.horizontal") && config.contains("mobs." + key + ".vector.vertical")) {
                    loot.setVectorUtil(new VectorUtil(config.getDouble("mobs." + key + ".vector.horizontal"), config.getDouble("mobs." + key + ".vector.vertical")));
                } else {
                    loot.setVectorUtil(new VectorUtil(1.1, 1.3));
                }

                if (config.contains("mobs." + key + ".level"))
                    loot.setMmLevel(config.getInt("mobs." + key + ".level", 0));
                if (config.contains("mobs." + key + ".action.message"))
                    loot.setMsg(config.getStringList("mobs." + key + ".action.message"));
                if (config.contains("mobs." + key + ".action.command"))
                    loot.setCommands(config.getStringList("mobs." + key + ".action.command"));
                if (config.contains("mobs." + key + ".action-hook.message"))
                    loot.setHookMsg(config.getStringList("mobs." + key + ".action-hook.message"));
                if (config.contains("mobs." + key + ".action-hook.command"))
                    loot.setHookCommands(config.getStringList("mobs." + key + ".action-hook.command"));
                if (config.contains("mobs." + key + ".action.exp"))
                    loot.setExp(config.getInt("mobs." + key + ".action.exp"));
                if (config.contains("mobs." + key + ".skill-xp"))
                    loot.setSkillXP(config.getDouble("mobs." + key + ".skill-xp"));
                if (config.contains("mobs." + key + ".layout"))
                    loot.setLayout(config.getString("mobs." + key + "layout"));
                if (config.contains("mobs." + key + ".group"))
                    loot.setGroup(config.getString("mobs." + key + ".group"));
                if (config.contains("mobs." + key + ".show-in-fishfinder")){
                    loot.setShowInFinder(config.getBoolean("mobs." + key + ".show-in-fishfinder"));
                }else {
                    loot.setShowInFinder(true);
                }
                loot.setScore((float) config.getDouble("mobs." + key + ".score",0));
                /*
                设置捕获条件
                 */
                if (config.contains("mobs." + key + ".requirements")){
                    List<Requirement> requirements = new ArrayList<>();
                    Objects.requireNonNull(config.getConfigurationSection("mobs." + key + ".requirements")).getKeys(false).forEach(requirement -> {
                        switch (requirement){
                            case "weather" -> requirements.add(new Weather(config.getStringList("mobs." + key + ".requirements.weather")));
                            case "ypos" -> requirements.add(new YPos(config.getStringList("mobs." + key + ".requirements.ypos")));
                            case "season" -> {
                                if (Config.season){
                                    requirements.add(new Season(config.getStringList("mobs." + key + ".requirements.season")));
                                }else {
                                    AdventureManager.consoleMessage("<red>[CustomFishing] Plz enable season in config.yml!</red>");
                                }
                            }
                            case "world" -> requirements.add(new World(config.getStringList("mobs." + key + ".requirements.world")));
                            case "biome" -> requirements.add(new Biome(config.getStringList("mobs." + key + ".requirements.biome")));
                            case "permission" -> requirements.add(new Permission(config.getString("mobs." + key + ".requirements.permission")));
                            case "region" -> {
                                if (Config.wg){
                                    requirements.add(new Region(config.getStringList("mobs." + key + ".requirements.regions")));
                                }else {
                                    AdventureManager.consoleMessage("<red>[CustomFishing] Plz enable WorldGuard Integration!</red>");
                                }
                            }
                            case "time" -> requirements.add(new Time(config.getStringList("mobs." + key + ".requirements.time")));
                        }
                    });
                    loot.setRequirements(requirements);
                }
                //丢入缓存
                LOOT.put(key, loot);
            });
            if (keys.size() != LOOTITEM.size() || mobs.size() != LOOT.size()- LOOTITEM.size()) {
                AdventureManager.consoleMessage("<red>[CustomFishing] loots.yml exists error!</red>");
            } else {
                AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><white>" + keys.size() + " <color:#E1FFFF>loots loaded!");
                AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><white>" + mobs.size() + " <color:#E1FFFF>mobs loaded!");
            }
            return;
        }
        if (keys.size() != LOOTITEM.size()){
            AdventureManager.consoleMessage("<red>[CustomFishing] loots.yml exists error!</red>");
        } else {
            AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><white>" + keys.size() + " <color:#E1FFFF>loots loaded!");
        }
    }

    /*
    载入util物品
     */
    public static void loadUtil() {

        UTIL.clear();
        UTILITEM.clear();

        YamlConfiguration config = getConfig("utils.yml");
        Set<String> keys = Objects.requireNonNull(config.getConfigurationSection("utils")).getKeys(false);
        keys.forEach(key -> {
            /*
            必设置的内容，为构造所需
             */

            String material;
            if (config.contains("utils." + key + ".material")) {
                material = config.getString("utils." + key + ".material");
            } else {
                AdventureManager.consoleMessage("<red>[CustomFishing] Error! No material set for " + key + " !</red>");
                return;
            }

            Util utilInstance = new Util(material);
            if (config.contains("utils." + key + ".custom-model-data"))
                utilInstance.setCustommodeldata(config.getInt("utils." + key + ".custom-model-data"));
            if (config.contains("utils." + key + ".display.name"))
                utilInstance.setName(config.getString("utils." + key + ".display.name"));
            if (config.contains("utils." + key + ".display.lore"))
                utilInstance.setLore(config.getStringList("utils." + key + ".display.lore"));
            if (config.contains("utils." + key + ".nbt"))
                utilInstance.setNbt((Map<String, Object>) config.getMapList("utils." + key + ".nbt").get(0));
            utilInstance.setUnbreakable(config.getBoolean("utils." + key + ".unbreakable",false));
            if (config.contains("utils." + key + ".enchantments")) {
                ArrayList<Enchantment> arrayList = new ArrayList<>();
                config.getStringList("utils." + key + ".enchantments").forEach(enchant -> {
                    String[] split = StringUtils.split(enchant, "/");
                    NamespacedKey namespacedKey = NamespacedKey.fromString(split[0]);
                    arrayList.add(new Enchantment(namespacedKey, Integer.parseInt(split[1])));
                });
                utilInstance.setEnchantment(arrayList);
            }
            if (config.contains("utils." + key + ".item_flags")) {
                ArrayList<ItemFlag> arrayList = new ArrayList<>();
                config.getStringList("utils." + key + ".item_flags").forEach(flag -> {
                    arrayList.add(ItemFlag.valueOf(flag));
                });
                utilInstance.setItemFlags(arrayList);
            }

            UTIL.put(key, utilInstance);
            UTILITEM.put(key, NBTUtil.addIdentifier(ItemStackGenerator.fromItem(utilInstance), "util", key));
        });
        if (keys.size() != UTILITEM.size()){
            AdventureManager.consoleMessage("<red>[CustomFishing] utils.yml exists error!</red>");
        } else {
            AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><white>" + keys.size() + " <color:#E1FFFF>utils loaded!");
        }
    }

    /*
    载入rod物品
     */
    public static void loadRod() {

        ROD.clear();
        RODITEM.clear();

        YamlConfiguration config = getConfig("rods.yml");
        Set<String> keys = Objects.requireNonNull(config.getConfigurationSection("rods")).getKeys(false);

        keys.forEach(key -> {
            Rod rodInstance = new Rod();
            if (config.contains("rods." + key + ".display.name"))
                rodInstance.setName(config.getString("rods." + key + ".display.name"));
            if (config.contains("rods." + key + ".display.lore"))
                rodInstance.setLore(config.getStringList("rods." + key + ".display.lore"));
            if (config.contains("rods." + key + ".nbt"))
                rodInstance.setNbt((Map<String, Object>)(config.getMapList("rods." + key + ".nbt").get(0)));
            if (config.contains("rods." + key + ".custom-model-data"))
                rodInstance.setCustommodeldata(config.getInt("rods." + key + ".custom-model-data"));
            rodInstance.setUnbreakable(config.getBoolean("rods." + key + ".unbreakable",false));
            if (config.contains("rods." + key + ".enchantments")) {
                ArrayList<Enchantment> arrayList = new ArrayList<>();
                config.getStringList("rods." + key + ".enchantments").forEach(enchant -> {
                    String[] split = StringUtils.split(enchant, "/");
                    NamespacedKey namespacedKey = NamespacedKey.fromString(split[0]);
                    arrayList.add(new Enchantment(namespacedKey, Integer.parseInt(split[1])));
                });
                rodInstance.setEnchantment(arrayList);
            }
            if (config.contains("rods." + key + ".item_flags")) {
                ArrayList<ItemFlag> arrayList = new ArrayList<>();
                config.getStringList("rods." + key + ".item_flags").forEach(flag -> {
                    arrayList.add(ItemFlag.valueOf(flag));
                });
                rodInstance.setItemFlags(arrayList);
            }
            if (config.contains("rods." + key + ".modifier")){
                config.getConfigurationSection("rods." + key + ".modifier").getKeys(false).forEach(modifier -> {
                    switch (modifier){
                        case "weight-PM" -> {
                            HashMap<String, Integer> pm = new HashMap<>();
                            config.getConfigurationSection("rods." + key + ".modifier.weight-PM").getValues(false).forEach((group, value) -> {
                                pm.put(group, (Integer) value);
                            });
                            rodInstance.setWeightPM(pm);
                        }
                        case "weight-MQ" -> {
                            HashMap<String, Double> mq = new HashMap<>();
                            config.getConfigurationSection("rods." + key + ".modifier.weight-MQ").getValues(false).forEach((group, value) -> {
                                mq.put(group, (Double) value);
                            });
                            rodInstance.setWeightMQ(mq);
                        }
                        case "time" -> rodInstance.setTime(config.getDouble("rods." + key + ".modifier.time"));
                        case "difficulty" -> rodInstance.setDifficulty(config.getInt("rods." + key + ".modifier.difficulty"));
                        case "double-loot" -> rodInstance.setDoubleLoot(config.getDouble("rods." + key + ".modifier.double-loot"));
                        case "score" -> rodInstance.setScoreModifier(config.getDouble("rods." + key + ".modifier.score"));
                    }
                });
            }
            ROD.put(key, rodInstance);
            RODITEM.put(key, NBTUtil.addIdentifier(ItemStackGenerator.fromItem(rodInstance), "rod", key));
        });

        if (keys.size() != RODITEM.size()){
            AdventureManager.consoleMessage("<red>[CustomFishing] rods.yml exists error!</red>");
        } else {
            AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><white>" + keys.size() + " <color:#E1FFFF>rods loaded!");
        }
    }

    public static void loadBait(){

        BAITITEM.clear();
        BAIT.clear();

        YamlConfiguration config = getConfig("baits.yml");
        Set<String> keys = config.getConfigurationSection("baits").getKeys(false);

        keys.forEach(key -> {
            String material;
            if (config.contains("baits." + key + ".material")) {
                material = config.getString("baits." + key + ".material");
            } else {
                AdventureManager.consoleMessage("<red>[CustomFishing] Error! No material set for " + key + " !</red>");
                return;
            }
            Bait baitInstance = new Bait(material);
            if (config.contains("baits." + key + ".display.lore"))
                baitInstance.setLore(config.getStringList("baits." + key + ".display.lore"));
            if (config.contains("baits." + key + ".display.name"))
                baitInstance.setName(config.getString("baits." + key + ".display.name"));
            if (config.contains("baits." + key + ".custom-model-data"))
                baitInstance.setCustommodeldata(config.getInt("baits." + key + ".custom-model-data"));
            if (config.contains("baits." + key + ".nbt")) {
                baitInstance.setNbt((Map<String, Object>) config.getMapList("baits." + key + ".nbt").get(0));
            }
            baitInstance.setUnbreakable(config.getBoolean("baits." + key + ".unbreakable",false));
            if (config.contains("baits." + key + ".enchantments")) {
                ArrayList<Enchantment> arrayList = new ArrayList<>();
                config.getStringList("baits." + key + ".enchantments").forEach(enchant -> {
                    String[] split = StringUtils.split(enchant, "/");
                    NamespacedKey namespacedKey = NamespacedKey.fromString(split[0]);
                    arrayList.add(new Enchantment(namespacedKey, Integer.parseInt(split[1])));
                });
                baitInstance.setEnchantment(arrayList);
            }
            if (config.contains("baits." + key + ".item_flags")) {
                ArrayList<ItemFlag> arrayList = new ArrayList<>();
                config.getStringList("baits." + key + ".item_flags").forEach(flag -> {
                    arrayList.add(ItemFlag.valueOf(flag));
                });
                baitInstance.setItemFlags(arrayList);
            }
            if (config.contains("baits." + key + ".modifier")){
                config.getConfigurationSection("baits." + key + ".modifier").getKeys(false).forEach(modifier -> {
                    switch (modifier){
                        case "weight-PM" -> {
                            HashMap<String, Integer> pm = new HashMap<>();
                            config.getConfigurationSection("baits." + key + ".modifier.weight-PM").getValues(false).forEach((group, value) -> {
                                pm.put(group, (Integer) value);
                            });
                            baitInstance.setWeightPM(pm);
                        }
                        case "weight-MQ" -> {
                            HashMap<String, Double> mq = new HashMap<>();
                            config.getConfigurationSection("baits." + key + ".modifier.weight-MQ").getValues(false).forEach((group, value) -> {
                                mq.put(group, (Double) value);
                            });
                            baitInstance.setWeightMQ(mq);
                        }
                        case "time" -> baitInstance.setTime(config.getDouble("baits." + key + ".modifier.time"));
                        case "difficulty" -> baitInstance.setDifficulty(config.getInt("baits." + key + ".modifier.difficulty"));
                        case "double-loot" -> baitInstance.setDoubleLoot(config.getDouble("baits." + key + ".modifier.double-loot"));
                        case "score" -> baitInstance.setScoreModifier(config.getDouble("baits." + key + ".modifier.score"));
                    }
                });
            }
            BAIT.put(key, baitInstance);
            BAITITEM.put(key, NBTUtil.addIdentifier(ItemStackGenerator.fromItem(baitInstance), "bait", key));
        });

        if (keys.size() != BAITITEM.size()){
            AdventureManager.consoleMessage("<red>[CustomFishing] baits.yml exists error!</red>");
        } else {
            AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><white>" + keys.size() + " <color:#E1FFFF>baits loaded!");
        }
    }

    public static void loadCompetitions(){
        Competitions.clear();
        CompetitionsCommand.clear();
        YamlConfiguration config = getConfig("competition.yml");
        Set<String> keys = config.getConfigurationSection("").getKeys(false);
        keys.forEach(key -> {
            CompetitionConfig competitionConfig;
            if (config.getBoolean(key + ".bossbar.enable", true)){
                competitionConfig = new CompetitionConfig(true);
                BossBarConfig bossBarConfig = new BossBarConfig(
                        config.getString(key + ".bossbar.text"),
                        BossBar.Overlay.valueOf(config.getString(key + ".bossbar.overlay")),
                        BossBar.Color.valueOf(config.getString(key + ".bossbar.color")),
                        config.getInt(key + ".bossbar.refresh-rate")
                );
                competitionConfig.setBossBarConfig(bossBarConfig);
            }else {
                competitionConfig = new CompetitionConfig(false);
            }
            competitionConfig.setDuration(config.getInt(key + ".duration",600));
            competitionConfig.setGoal(Goal.valueOf(config.getString(key + ".goal", "RANDOM")));
            if (config.contains(key + ".broadcast.start")){
                competitionConfig.setStartMessage(config.getStringList(key + ".broadcast.start"));
            }
            if (config.contains(key + ".broadcast.end")){
                competitionConfig.setEndMessage(config.getStringList(key + ".broadcast.end"));
            }
            if (config.contains(key + ".command.join")){
                competitionConfig.setJoinCommand(config.getStringList(key + ".command.join"));
            }
            if (config.contains(key + ".command.start")){
                competitionConfig.setStartCommand(config.getStringList(key + ".command.start"));
            }
            if (config.contains(key + ".command.end")){
                competitionConfig.setEndCommand(config.getStringList(key + ".command.end"));
            }
            if (config.contains(key + ".min-players")){
                competitionConfig.setMinPlayers(config.getInt(key + ".min-players"));
            }
            if (config.contains(key + ".prize")){
                HashMap<String, List<Reward>> rewardsMap = new HashMap<>();
                config.getConfigurationSection(key + ".prize").getKeys(false).forEach(rank -> {
                    List<Reward> rewards = new ArrayList<>();
                    if (config.contains(key + ".prize." + rank + ".messages")){
                        rewards.add(new MessageImpl(config.getStringList(key + ".prize." + rank + ".messages")));
                    }
                    if (config.contains(key + ".prize." + rank + ".commands")){
                        rewards.add(new CommandImpl(config.getStringList(key + ".prize." + rank + ".commands")));
                    }
                    rewardsMap.put(rank, rewards);
                });
                competitionConfig.setRewards(rewardsMap);
            }
            config.getStringList(key + ".start-time").forEach(time -> {
                Competitions.put(time, competitionConfig);
            });
            CompetitionsCommand.put(key, competitionConfig);
        });
    }

    public static void tryEnableJedis(){
        YamlConfiguration configuration = ConfigReader.getConfig("redis.yml");
        if (configuration.getBoolean("redis.enable")){
            JedisUtil.initializeRedis(configuration);
            JedisUtil.useRedis = true;
        }else {
            JedisUtil.useRedis = false;
        }
    }

    public static void loadBars(){
        LAYOUT.clear();
        YamlConfiguration config = ConfigReader.getConfig("bars.yml");
        Set<String> keys = Objects.requireNonNull(config.getConfigurationSection("")).getKeys(false);
        keys.forEach(key -> {
            int range = config.getInt(key + ".range");
            Set<String> rates = Objects.requireNonNull(config.getConfigurationSection(key + ".layout")).getKeys(false);
            double[] successRate = new double[rates.size()];
            for(int i = 0; i < rates.size(); i++){
                successRate[i] = config.getDouble(key + ".layout." +(i + 1));
            }
            int size = rates.size()*range -1;
            Layout layout = new Layout(key, range, successRate, size);
            layout.setTitle(config.getString(key + ".title"," "));
            layout.setBar(config.getString(key + ".subtitle.bar","뀃"));
            layout.setEnd(config.getString(key + ".subtitle.end","</font>"));
            layout.setStart(config.getString(key + ".subtitle.start","<font:customfishing:default>"));
            layout.setPointer(config.getString(key + ".subtitle.pointer","뀄"));
            layout.setPointerOffset(config.getString(key + ".subtitle.pointer_offset","뀂"));
            layout.setOffset(config.getString(key + ".subtitle.offset","뀁"));
            LAYOUT.put(key, layout);
        });
    }
}