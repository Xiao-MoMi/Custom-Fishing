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
import net.momirealms.customfishing.hook.season.CustomCropsSeason;
import net.momirealms.customfishing.hook.season.RealisticSeason;
import net.momirealms.customfishing.hook.season.SeasonInterface;
import net.momirealms.customfishing.hook.skill.*;
import net.momirealms.customfishing.object.*;
import net.momirealms.customfishing.object.action.*;
import net.momirealms.customfishing.object.loot.DroppedItem;
import net.momirealms.customfishing.object.loot.Loot;
import net.momirealms.customfishing.object.loot.Mob;
import net.momirealms.customfishing.requirements.*;
import net.momirealms.customfishing.utils.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class ConfigReader{

    public static HashMap<String, Loot> LOOT = new HashMap<>();
    public static HashMap<String, ItemStack> LootItem = new HashMap<>();
    public static HashMap<String, ItemStack> UtilItem = new HashMap<>();
    public static HashMap<String, Bonus> ROD = new HashMap<>();
    public static HashMap<String, ItemStack> RodItem = new HashMap<>();
    public static HashMap<String, Bonus> BAIT = new HashMap<>();
    public static HashMap<String, ItemStack> BaitItem = new HashMap<>();
    public static HashMap<String, Layout> LAYOUT = new HashMap<>();
    public static HashMap<String, String> OTHERS = new HashMap<>();
    public static HashMap<String, HashMap<Integer, Bonus>> ENCHANTS = new HashMap<>();
    public static HashMap<String, CompetitionConfig> CompetitionsT = new HashMap<>();
    public static HashMap<String, CompetitionConfig> CompetitionsC = new HashMap<>();
    public static boolean useRedis;

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
        loadEnchants();
        loadCompetitions();
    }

    public static class Config {

        public static boolean wg;
        public static boolean mm;
        public static boolean papi;
        public static boolean needOpenWater;
        public static boolean needSpecialRod;
        public static boolean competition;
        public static boolean convertMMOItems;
        public static boolean loseDurability;
        public static boolean preventPick;
        public static boolean doubleRealIn;
        public static boolean vanillaLoot;
        public static boolean showBar;
        public static boolean mcMMOLoot;
        public static int fishFinderCoolDown;
        public static double timeMultiply;
        public static double vanillaRatio;
        public static double mcMMOLootChance;
        public static SkillXP skillXP;
        public static String version;
        public static String lang;
        public static String priority;
        public static SeasonInterface season;

        public static void loadConfig() {

            CustomFishing.instance.saveDefaultConfig();
            CustomFishing.instance.reloadConfig();
            FileConfiguration config = CustomFishing.instance.getConfig();

            wg = (mm = (papi = false));
            if (config.getBoolean("config.integrations.WorldGuard")){
                if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) AdventureUtil.consoleMessage("<red>[CustomFishing] Failed to initialize WorldGuard!</red>");
                else {
                    AdventureUtil.consoleMessage("[CustomFishing] <color:#00BFFF>WorldGuard <color:#E1FFFF>Hooked!");
                    wg = true;
                }
            }
            if (config.getBoolean("config.integrations.MythicMobs")){
                if (Bukkit.getPluginManager().getPlugin("MythicMobs") == null) AdventureUtil.consoleMessage("<red>[CustomFishing] Failed to initialize MythicMobs!</red>");
                else {
                    AdventureUtil.consoleMessage("[CustomFishing] <color:#00BFFF>MythicMobs <color:#E1FFFF>Hooked!");
                    mm = true;
                }
            }
            if (config.getBoolean("config.integrations.PlaceholderAPI")){
                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) AdventureUtil.consoleMessage("<red>[CustomFishing] Failed to initialize PlaceholderAPI!</red>");
                else papi = true;
            }

            skillXP = null;
            if(config.getBoolean("config.integrations.mcMMO",false)){
                if (Bukkit.getPluginManager().getPlugin("mcMMO") == null) CustomFishing.instance.getLogger().warning("Failed to initialize mcMMO!");
                else {
                    skillXP = new mcMMO();
                    AdventureUtil.consoleMessage("[CustomFishing] <color:#00BFFF>mcMMO <color:#E1FFFF>Hooked!");
                }
            }
            if(config.getBoolean("config.integrations.AureliumSkills",false)){
                if (Bukkit.getPluginManager().getPlugin("AureliumSkills") == null) CustomFishing.instance.getLogger().warning("Failed to initialize AureliumSkills!");
                else {
                    skillXP = new Aurelium();
                    AdventureUtil.consoleMessage("[CustomFishing] <color:#00BFFF>AureliumSkills <color:#E1FFFF>Hooked!");
                }
            }
            if(config.getBoolean("config.integrations.MMOCore",false)){
                if (Bukkit.getPluginManager().getPlugin("MMOCore") == null) CustomFishing.instance.getLogger().warning("Failed to initialize MMOCore!");
                else {
                    skillXP = new MMOCore();
                    AdventureUtil.consoleMessage("[CustomFishing] <color:#00BFFF>MMOCore <color:#E1FFFF>Hooked!");
                }
            }
            if(config.getBoolean("config.integrations.EcoSkills",false)){
                if (Bukkit.getPluginManager().getPlugin("EcoSkills") == null) CustomFishing.instance.getLogger().warning("Failed to initialize EcoSkills!");
                else {
                    skillXP = new EcoSkill();
                    AdventureUtil.consoleMessage("[CustomFishing] <color:#00BFFF>EcoSkills <color:#E1FFFF>Hooked!");
                }
            }

            season = null;
            if (config.getBoolean("config.integrations.RealisticSeasons",false)){
                if (Bukkit.getPluginManager().getPlugin("RealisticSeasons") == null) Log.warn("Failed to initialize RealisticSeasons!");
                else {
                    season = new RealisticSeason();
                    AdventureUtil.consoleMessage("[CustomFishing] <color:#00BFFF>RealisticSeasons <color:#E1FFFF>Hooked!");
                }
            }
            if (config.getBoolean("config.integrations.CustomCrops",false)){
                if (Bukkit.getPluginManager().getPlugin("CustomCrops") == null) Log.warn("Failed to initialize CustomCrops!");
                else {
                    season = new CustomCropsSeason();
                    AdventureUtil.consoleMessage("[CustomFishing] <color:#00BFFF>CustomCrops <color:#E1FFFF>Hooked!");
                }
            }

            doubleRealIn = config.getBoolean("config.double-reel-in", true);

            mcMMOLoot = config.getBoolean("config.other-loot.mcMMO", false);
            mcMMOLootChance = config.getDouble("config.other-loot.mcMMO-chance", 0.5);

            vanillaLoot = config.getBoolean("config.other-loot.vanilla", true);
            showBar = config.getBoolean("config.other-loot.bar", true);
            vanillaRatio = config.getDouble("config.other-loot.vanilla-ratio");

            convertMMOItems = config.getBoolean("config.convert-MMOITEMS", false);
            needOpenWater = config.getBoolean("config.need-open-water", false);
            needSpecialRod = config.getBoolean("config.need-special-rod", false);
            loseDurability = config.getBoolean("config.rod-lose-durability", true);
            preventPick = config.getBoolean("config.prevent-other-players-pick-up-loot", false);

            version = config.getString("config-version");
            priority = config.getString("config.event-priority");
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
        public static String noRod;
        public static String hookOther;

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
            noPlayer = config.getString("messages.no-player", "messages.no-player is missing");
            noScore = config.getString("messages.no-score", "messages.no-score is missing");
            noRod = config.getString("messages.no-rod", "messages.no-rod is missing");
            hookOther = config.getString("messages.hook-other-entity","messages.hook-other-entity is missing");
        }
    }

    public static class Title {

        public static List<String> success_title;
        public static List<String> success_subtitle;
        public static List<String> failure_title;
        public static List<String> failure_subtitle;
        public static int success_in;
        public static int success_out;
        public static int success_stay;
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

    public static void loadLoot() {

        LOOT.clear();
        LootItem.clear();
        OTHERS.clear();
        CustomPapi.allPapi.clear();

        File loot_file = new File(CustomFishing.instance.getDataFolder() + File.separator + "loots");

        if (!loot_file.exists()) {
            if (!loot_file.mkdir()) {
                AdventureUtil.consoleMessage("<red>[CustomFishing] Error! Failed to create loots folder...</red>");
                return;
            }
            CustomFishing.instance.saveResource("loots" + File.separator + "default.yml", false);
            CustomFishing.instance.saveResource("loots" + File.separator + "example.yml", false);
        }

        File[] files = loot_file.listFiles();

        if (files != null) {

            for (File file : files) {

                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                Set<String> keys = config.getKeys(false);

                keys.forEach(key -> {

                    if (!config.getBoolean(key + ".enable", true)) return;

                    DroppedItem loot = new DroppedItem(key);

                    String[] diff = StringUtils.split(config.getString(key + ".difficulty", "1-1"),"-");
                    Difficulty difficulty = new Difficulty(Integer.parseInt(diff[0]), Integer.parseInt(diff[1]));

                    int weight = config.getInt(key + ".weight",10);
                    int time = config.getInt(key + ".time",10000);

                    loot.setDifficulty(difficulty);
                    loot.setTime(time);
                    loot.setWeight(weight);
                    loot.setNick(config.getString(key + ".nick", key));
                    loot.setScore(config.getDouble(key + ".score",0));
                    loot.setShowInFinder(config.getBoolean(key + ".show-in-fishfinder", true));
                    loot.setRandomDurability(config.getBoolean(key + ".random-durability", false));

                    if (config.contains(key + ".group"))
                        loot.setGroup(config.getString(key + ".group"));
                    if (config.contains(key + ".layout"))
                        loot.setLayout(config.getStringList(key + ".layout"));
                    if (config.contains(key + ".random-enchantments")){
                        List<LeveledEnchantment> randomEnchants = new ArrayList<>();
                        config.getConfigurationSection(key + ".random-enchantments").getValues(false).forEach((order, enchant) -> {
                            if (enchant instanceof MemorySection memorySection){
                                LeveledEnchantment enchantment = new LeveledEnchantment(NamespacedKey.fromString(memorySection.getString("enchant")), memorySection.getInt("level"));
                                enchantment.setChance(memorySection.getDouble("chance"));
                                randomEnchants.add(enchantment);
                            }
                        });
                        loot.setRandomEnchants(randomEnchants);
                    }

                    List<ActionB> successActions = new ArrayList<>();
                    if (config.contains(key + ".action.success.message"))
                        successActions.add(new MessageA(config.getStringList(key + ".action.success.message"), loot.getNick()));
                    if (config.contains(key + ".action.success.command"))
                        successActions.add(new CommandA(config.getStringList(key + ".action.success.command"), loot.getNick()));
                    if (config.contains(key + ".action.success.exp"))
                        successActions.add(new XPB(config.getInt(key + ".action.success.exp")));
                    if (config.contains(key + ".action.success.mending"))
                        successActions.add(new XPA(config.getInt(key + ".action.success.mending")));
                    if (config.contains(key + ".action.success.skill-xp"))
                        successActions.add(new FishingXPB(config.getInt(key + ".action.success.skill-xp")));
                    loot.setSuccessActions(successActions);

                    List<ActionB> failureActions = new ArrayList<>();
                    if (config.contains(key + ".action.failure.message"))
                        failureActions.add(new MessageA(config.getStringList(key + ".action.failure.message"), loot.getNick()));
                    if (config.contains( key + ".action.failure.command"))
                        failureActions.add(new CommandA(config.getStringList(key + ".action.failure.command"), loot.getNick()));
                    if (config.contains( key + ".action.failure.exp"))
                        failureActions.add(new XPB(config.getInt( key + ".action.failure.exp")));
                    if (config.contains(key + ".action.failure.mending"))
                        failureActions.add(new XPA(config.getInt(key + ".action.failure.mending")));
                    if (config.contains( key + ".action.failure.skill-xp"))
                        failureActions.add(new FishingXPB(config.getInt( key + ".action.failure.skill-xp")));
                    loot.setFailureActions(failureActions);

                    List<ActionB> hookActions = new ArrayList<>();
                    if (config.contains(key + ".action.hook.message"))
                        hookActions.add(new MessageA(config.getStringList(key + ".action.hook.message"), loot.getNick()));
                    if (config.contains(key + ".action.hook.command"))
                        hookActions.add(new CommandA(config.getStringList(key + ".action.hook.command"), loot.getNick()));
                    if (config.contains(key + ".action.hook.exp"))
                        successActions.add(new XPB(config.getInt(key + ".action.hook.exp")));
                    if (config.contains(key + ".action.hook.mending"))
                        successActions.add(new XPA(config.getInt(key + ".action.hook.mending")));
                    if (config.contains(key + ".action.hook.skill-xp"))
                        successActions.add(new FishingXPB(config.getInt(key + ".action.hook.skill-xp")));
                    loot.setHookActions(hookActions);

                    if (config.contains(key + ".requirements")){
                        List<Requirement> requirements = new ArrayList<>();
                        config.getConfigurationSection(key + ".requirements").getKeys(false).forEach(requirement -> {
                            switch (requirement){
                                case "weather" -> requirements.add(new Weather(config.getStringList(key + ".requirements.weather")));
                                case "ypos" -> requirements.add(new YPos(config.getStringList(key + ".requirements.ypos")));
                                case "season" -> {
                                    if (Config.season != null) requirements.add(new Season(config.getStringList(key + ".requirements.season")));
                                    else AdventureUtil.consoleMessage("<red>[CustomFishing] You need to enable season hook in config.yml to use season condition!</red>");
                                }
                                case "world" -> requirements.add(new World(config.getStringList(key + ".requirements.world")));
                                case "biome" -> requirements.add(new Biome(config.getStringList(key + ".requirements.biome")));
                                case "permission" -> requirements.add(new Permission(config.getString(key + ".requirements.permission")));
                                case "region" -> {
                                    if (Config.wg) requirements.add(new Region(config.getStringList(key + ".requirements.regions")));
                                    else AdventureUtil.consoleMessage("<red>[CustomFishing] You need to enable WorldGuard Integration to use region condition!</red>");
                                }
                                case "time" -> requirements.add(new Time(config.getStringList(key + ".requirements.time")));
                                case "skill-level" -> requirements.add(new SkillLevel(config.getInt(key + ".requirements.skill-level")));
                                case "papi-condition" -> {
                                    if (Config.papi) requirements.add(new CustomPapi(config.getConfigurationSection(key + ".requirements.papi-condition").getValues(false)));
                                    else AdventureUtil.consoleMessage("<red>[CustomFishing] You need to enable PlaceholderAPI Integration to use papi condition!</red>");
                                }
                            }
                        });
                        loot.setRequirements(requirements);
                    }

                    String material = config.getString(key + ".material","COD");
                    if (material.contains(":")) {
                        if (material.startsWith("ItemsAdder:")){
                            loot.setType("ia");
                            loot.setId(material.substring(11));
                        }
                        else if (material.startsWith("Oraxen:")){
                            loot.setType("oraxen");
                            loot.setId(material.substring(7));
                        }
                        else if (material.startsWith("MMOItems:")){
                            loot.setType("mmoitems");
                            loot.setId(material.substring(9));
                        }
                        else if (material.startsWith("MythicMobs:")){
                            loot.setType("mm");
                            loot.setId(material.substring(11));
                        }
                        else {
                            AdventureUtil.consoleMessage("<red>Unknown Item: " + key);
                            return;
                        }
                        OTHERS.put(key, material);
                        LOOT.put(key, loot);
                    }
                    else {

                        Item item = new Item(material);

                        item.setUnbreakable(config.getBoolean(key + ".unbreakable",false));

                        if (config.contains(key + ".display.lore"))
                            item.setLore(config.getStringList(key + ".display.lore"));
                        if (config.contains(key + ".display.name"))
                            item.setName(config.getString(key + ".display.name"));
                        if (config.contains(key + ".custom-model-data"))
                            item.setCustomModelData(config.getInt(key + ".custom-model-data"));
                        if (config.contains(key + ".enchantments")){
                            List<LeveledEnchantment> enchantmentList = new ArrayList<>();
                            config.getConfigurationSection(key + ".enchantments").getKeys(false).forEach(enchant -> {
                                LeveledEnchantment leveledEnchantment = new LeveledEnchantment(
                                        NamespacedKey.fromString(enchant),
                                        config.getInt(key + ".enchantments." + enchant)
                                );
                                enchantmentList.add(leveledEnchantment);
                            });
                            item.setEnchantment(enchantmentList);
                        }
                        if (config.contains(key + ".item_flags")) {
                            ArrayList<ItemFlag> itemFlags = new ArrayList<>();
                            config.getStringList(key + ".item_flags").forEach(flag -> itemFlags.add(ItemFlag.valueOf(flag)));
                            item.setItemFlags(itemFlags);
                        }
                        if (config.contains(key + ".nbt")){
                            Map<String, Object> nbt = config.getConfigurationSection(key + ".nbt").getValues(false);
                            item.setNbt(nbt);
                        }
                        loot.setType("default");
                        LOOT.put(key, loot);
                        if (item.getMaterial().equalsIgnoreCase("AIR")) LootItem.put(key, new ItemStack(Material.AIR));
                        else LootItem.put(key, ItemStackUtil.getFromItem(item));
                    }
                });
            }
            AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + LOOT.size() + " <gray>loots");
        }

        if (Config.mm){
            File mob_file = new File(CustomFishing.instance.getDataFolder() + File.separator + "mobs");
            if (!mob_file.exists()) {
                if (!mob_file.mkdir()) {
                    AdventureUtil.consoleMessage("<red>[CustomFishing] Error! Failed to create mobs folder...</red>");
                    return;
                }
                CustomFishing.instance.saveResource("mobs" + File.separator + "example.yml", false);
            }
            File[] mobFiles = mob_file.listFiles();
            if (mobFiles != null) {

                int size = LOOT.size();

                for (File file : mobFiles) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    Set<String> mobs = config.getKeys(false);
                    mobs.forEach(key -> {

                        if (!config.getBoolean(key + ".enable", true)) return;

                        Mob loot = new Mob(key, config.getString(key + ".mythicmobsID", key));

                        String[] diff = StringUtils.split(config.getString(key + ".difficulty", "1-1"),"-");
                        Difficulty difficulty = new Difficulty(Integer.parseInt(diff[0]), Integer.parseInt(diff[1]));

                        int weight = config.getInt(key + ".weight",10);
                        int time = config.getInt(key + ".time",10000);

                        loot.setDifficulty(difficulty);
                        loot.setTime(time);
                        loot.setWeight(weight);
                        loot.setNick(config.getString(key + ".name", key));
                        loot.setScore(config.getDouble(key + ".score",0));
                        loot.setShowInFinder(config.getBoolean(key + ".show-in-fishfinder", true));
                        loot.setMmLevel(config.getInt(key + ".level", 0));
                        loot.setMobVector(new MobVector(
                                config.getDouble(key + ".vector.horizontal",1.1),
                                config.getDouble(key + ".vector.vertical",1.3)
                        ));

                        if (config.contains(key + ".group"))
                            loot.setGroup(config.getString(key + ".group"));
                        if (config.contains(key + ".layout"))
                            loot.setLayout(config.getStringList(key + ".layout"));

                        List<ActionB> successActions = new ArrayList<>();
                        if (config.contains(key + ".action.success.message"))
                            successActions.add(new MessageA(config.getStringList(key + ".action.success.message"), loot.getNick()));
                        if (config.contains(key + ".action.success.command"))
                            successActions.add(new CommandA(config.getStringList(key + ".action.success.command"), loot.getNick()));
                        if (config.contains(key + ".action.success.exp"))
                            successActions.add(new XPB(config.getInt(key + ".action.success.exp")));
                        if (config.contains(key + ".action.success.mending"))
                            successActions.add(new XPA(config.getInt(key + ".action.success.mending")));
                        if (config.contains(key + ".action.success.skill-xp"))
                            successActions.add(new FishingXPB(config.getInt(key + ".action.success.skill-xp")));
                        loot.setSuccessActions(successActions);

                        List<ActionB> failureActions = new ArrayList<>();
                        if (config.contains(key + ".action.failure.message"))
                            failureActions.add(new MessageA(config.getStringList(key + ".action.failure.message"), loot.getNick()));
                        if (config.contains(key + ".action.failure.command"))
                            failureActions.add(new CommandA(config.getStringList(key + ".action.failure.command"), loot.getNick()));
                        if (config.contains(key + ".action.failure.exp"))
                            failureActions.add(new XPB(config.getInt(key + ".action.failure.exp")));
                        if (config.contains(key + ".action.failure.mending"))
                            failureActions.add(new XPA(config.getInt(key + ".action.failure.mending")));
                        if (config.contains(key + ".action.failure.skill-xp"))
                            failureActions.add(new FishingXPB(config.getInt(key + ".action.failure.skill-xp")));
                        loot.setFailureActions(failureActions);

                        List<ActionB> hookActions = new ArrayList<>();
                        if (config.contains(key + ".action.hook.message"))
                            hookActions.add(new MessageA(config.getStringList(key + ".action.hook.message"), loot.getNick()));
                        if (config.contains(key + ".action.hook.command"))
                            hookActions.add(new CommandA(config.getStringList(key + ".action.hook.command"), loot.getNick()));
                        if (config.contains(key + ".action.hook.exp"))
                            successActions.add(new XPB(config.getInt(key + ".action.hook.exp")));
                        if (config.contains(key + ".action.hook.mending"))
                            successActions.add(new XPA(config.getInt(key + ".action.hook.mending")));
                        if (config.contains(key + ".action.hook.skill-xp"))
                            successActions.add(new FishingXPB(config.getInt(key + ".action.hook.skill-xp")));
                        loot.setHookActions(hookActions);

                        if (config.contains(key + ".requirements")){
                            List<Requirement> requirements = new ArrayList<>();
                            config.getConfigurationSection(key + ".requirements").getKeys(false).forEach(requirement -> {
                                switch (requirement){
                                    case "weather" -> requirements.add(new Weather(config.getStringList(key + ".requirements.weather")));
                                    case "ypos" -> requirements.add(new YPos(config.getStringList(key + ".requirements.ypos")));
                                    case "season" -> {
                                        if (Config.season != null) requirements.add(new Season(config.getStringList(key + ".requirements.season")));
                                        else AdventureUtil.consoleMessage("<red>[CustomFishing] You need to enable season hook in config.yml to use season condition!</red>");
                                    }
                                    case "world" -> requirements.add(new World(config.getStringList(key + ".requirements.world")));
                                    case "biome" -> requirements.add(new Biome(config.getStringList(key + ".requirements.biome")));
                                    case "permission" -> requirements.add(new Permission(config.getString(key + ".requirements.permission")));
                                    case "region" -> {
                                        if (Config.wg) requirements.add(new Region(config.getStringList(key + ".requirements.regions")));
                                        else AdventureUtil.consoleMessage("<red>[CustomFishing] You need to enable WorldGuard Integration to use region condition!</red>");
                                    }
                                    case "time" -> requirements.add(new Time(config.getStringList(key + ".requirements.time")));
                                    case "skill-level" -> requirements.add(new SkillLevel(config.getInt(key + ".requirements.skill-level")));
                                    case "papi-condition" -> {
                                        if (Config.papi) requirements.add(new CustomPapi(config.getConfigurationSection(key + ".requirements.papi-condition").getValues(false)));
                                        else AdventureUtil.consoleMessage("<red>[CustomFishing] You need to enable PlaceholderAPI Integration to use papi condition!</red>");
                                    }
                                }
                            });
                            loot.setRequirements(requirements);
                        }
                        LOOT.put(key, loot);
                    });
                }
                AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + (LOOT.size() - size) + " <gray>mobs");
            }
        }
    }

    public static void loadUtil() {

        UtilItem.clear();

        File util_file = new File(CustomFishing.instance.getDataFolder() + File.separator + "utils");

        if (!util_file.exists()) {
            if (!util_file.mkdir()) {
                AdventureUtil.consoleMessage("<red>[CustomFishing] Error! Failed to create utils folder...</red>");
                return;
            }
            CustomFishing.instance.saveResource("utils" + File.separator + "fishfinder.yml", false);
        }

        File[] files = util_file.listFiles();

        if (files != null) {
            for (File file : files) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                Set<String> keys = config.getKeys(false);
                keys.forEach(key -> {

                    if (!config.getBoolean(key + ".enable", true)) return;

                    Item item = new Item(config.getString(key + ".material", "COMPASS"));
                    item.setUnbreakable(config.getBoolean(key + ".unbreakable", false));
                    if (config.contains(key + ".display.lore"))
                        item.setLore(config.getStringList(key + ".display.lore"));
                    if (config.contains(key + ".display.name"))
                        item.setName(config.getString(key + ".display.name"));
                    if (config.contains(key + ".custom-model-data"))
                        item.setCustomModelData(config.getInt(key + ".custom-model-data"));
                    if (config.contains(key + ".enchantments")) {
                        List<LeveledEnchantment> enchantmentList = new ArrayList<>();
                        config.getConfigurationSection(key + ".enchantments").getKeys(false).forEach(enchant -> {
                            LeveledEnchantment leveledEnchantment = new LeveledEnchantment(
                                    NamespacedKey.fromString(enchant),
                                    config.getInt(key + ".enchantments." + enchant)
                            );
                            enchantmentList.add(leveledEnchantment);
                        });
                        item.setEnchantment(enchantmentList);
                    }
                    if (config.contains(key + ".item_flags")) {
                        ArrayList<ItemFlag> itemFlags = new ArrayList<>();
                        config.getStringList(key + ".item_flags").forEach(flag -> itemFlags.add(ItemFlag.valueOf(flag)));
                        item.setItemFlags(itemFlags);
                    }
                    if (config.contains(key + ".nbt")) {
                        Map<String, Object> nbt = config.getConfigurationSection(key + ".nbt").getValues(false);
                        item.setNbt(nbt);
                    }
                    UtilItem.put(key, NBTUtil.addIdentifier(ItemStackUtil.getFromItem(item), "util", key));
                });
            }
            AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + UtilItem.size() + " <gray>utils");
        }
    }

    public static void loadRod() {

        ROD.clear();
        RodItem.clear();

        File rod_file = new File(CustomFishing.instance.getDataFolder() + File.separator + "rods");

        if (!rod_file.exists()) {
            if (!rod_file.mkdir()) {
                AdventureUtil.consoleMessage("<red>[CustomFishing] Error! Failed to create rods folder...</red>");
                return;
            }
            CustomFishing.instance.saveResource("rods" + File.separator + "default.yml", false);
        }

        File[] files = rod_file.listFiles();

        if (files != null) {
            for (File file : files) {

                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                Set<String> keys = config.getKeys(false);

                keys.forEach(key -> {

                    Item item = new Item("FISHING_ROD");
                    item.setUnbreakable(config.getBoolean(key + ".unbreakable", false));
                    if (config.contains(key + ".display.lore"))
                        item.setLore(config.getStringList(key + ".display.lore"));
                    if (config.contains(key + ".display.name"))
                        item.setName(config.getString(key + ".display.name"));
                    if (config.contains(key + ".custom-model-data"))
                        item.setCustomModelData(config.getInt(key + ".custom-model-data"));
                    if (config.contains(key + ".enchantments")) {
                        List<LeveledEnchantment> enchantmentList = new ArrayList<>();
                        config.getConfigurationSection(key + ".enchantments").getKeys(false).forEach(enchant -> {
                            LeveledEnchantment leveledEnchantment = new LeveledEnchantment(
                                    NamespacedKey.fromString(enchant),
                                    config.getInt(key + ".enchantments." + enchant)
                            );
                            enchantmentList.add(leveledEnchantment);
                        });
                        item.setEnchantment(enchantmentList);
                    }
                    if (config.contains(key + ".item_flags")) {
                        ArrayList<ItemFlag> itemFlags = new ArrayList<>();
                        config.getStringList(key + ".item_flags").forEach(flag -> itemFlags.add(ItemFlag.valueOf(flag)));
                        item.setItemFlags(itemFlags);
                    }
                    if (config.contains(key + ".nbt")) {
                        Map<String, Object> nbt = config.getConfigurationSection(key + ".nbt").getValues(false);
                        item.setNbt(nbt);
                    }
                    RodItem.put(key, NBTUtil.addIdentifier(ItemStackUtil.getFromItem(item), "rod", key));

                    if (config.contains(key + ".modifier")) {
                        Bonus bonus = new Bonus();
                        config.getConfigurationSection(key + ".modifier").getKeys(false).forEach(modifier -> {
                            switch (modifier) {
                                case "weight-PM" -> {
                                    HashMap<String, Integer> pm = new HashMap<>();
                                    config.getConfigurationSection(key + ".modifier.weight-PM").getValues(false).forEach((group, value) -> {
                                        pm.put(group, (Integer) value);
                                    });
                                    bonus.setWeightPM(pm);
                                }
                                case "weight-MQ" -> {
                                    HashMap<String, Double> mq = new HashMap<>();
                                    config.getConfigurationSection(key + ".modifier.weight-MQ").getValues(false).forEach((group, value) -> {
                                        mq.put(group, Double.parseDouble(String.valueOf(value))-1);
                                    });
                                    bonus.setWeightMQ(mq);
                                }
                                case "time" -> bonus.setTime(config.getDouble(key + ".modifier.time"));
                                case "difficulty" -> bonus.setDifficulty(config.getInt(key + ".modifier.difficulty"));
                                case "double-loot" -> bonus.setDoubleLoot(config.getDouble(key + ".modifier.double-loot"));
                                case "score" -> bonus.setScore(config.getDouble(key + ".modifier.score"));
                            }
                        });
                        ROD.put(key, bonus);
                    }
                });
            }
            AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + RodItem.size() + " <gray>rods");
        }
    }

    public static void loadBait(){

        BAIT.clear();
        BaitItem.clear();

        File bait_file = new File(CustomFishing.instance.getDataFolder() + File.separator + "baits");

        if (!bait_file.exists()) {
            if (!bait_file.mkdir()) {
                AdventureUtil.consoleMessage("<red>[CustomFishing] Error! Failed to create baits folder...</red>");
                return;
            }
            CustomFishing.instance.saveResource("baits" + File.separator + "default.yml", false);
        }

        File[] files = bait_file.listFiles();

        if (files != null) {
            for (File file : files) {

                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                Set<String> keys = config.getKeys(false);
                keys.forEach(key -> {

                    Item item = new Item(config.getString(key + ".material", "PAPER"));
                    item.setUnbreakable(config.getBoolean(key + ".unbreakable", false));
                    if (config.contains(key + ".display.lore"))
                        item.setLore(config.getStringList(key + ".display.lore"));
                    if (config.contains(key + ".display.name"))
                        item.setName(config.getString(key + ".display.name"));
                    if (config.contains(key + ".custom-model-data"))
                        item.setCustomModelData(config.getInt(key + ".custom-model-data"));
                    if (config.contains(key + ".enchantments")) {
                        List<LeveledEnchantment> enchantmentList = new ArrayList<>();
                        config.getConfigurationSection(key + ".enchantments").getKeys(false).forEach(enchant -> {
                            LeveledEnchantment leveledEnchantment = new LeveledEnchantment(
                                    NamespacedKey.fromString(enchant),
                                    config.getInt(key + ".enchantments." + enchant)
                            );
                            enchantmentList.add(leveledEnchantment);
                        });
                        item.setEnchantment(enchantmentList);
                    }
                    if (config.contains(key + ".item_flags")) {
                        ArrayList<ItemFlag> itemFlags = new ArrayList<>();
                        config.getStringList(key + ".item_flags").forEach(flag -> itemFlags.add(ItemFlag.valueOf(flag)));
                        item.setItemFlags(itemFlags);
                    }
                    if (config.contains(key + ".nbt")) {
                        Map<String, Object> nbt = config.getConfigurationSection(key + ".nbt").getValues(false);
                        item.setNbt(nbt);
                    }
                    BaitItem.put(key, NBTUtil.addIdentifier(ItemStackUtil.getFromItem(item), "bait", key));

                    if (config.contains(key + ".modifier")) {
                        Bonus bonus = new Bonus();
                        config.getConfigurationSection(key + ".modifier").getKeys(false).forEach(modifier -> {
                            switch (modifier) {
                                case "weight-PM" -> {
                                    HashMap<String, Integer> pm = new HashMap<>();
                                    config.getConfigurationSection(key + ".modifier.weight-PM").getValues(false).forEach((group, value) -> {
                                        pm.put(group, (Integer) value);
                                    });
                                    bonus.setWeightPM(pm);
                                }
                                case "weight-MQ" -> {
                                    HashMap<String, Double> mq = new HashMap<>();
                                    config.getConfigurationSection(key + ".modifier.weight-MQ").getValues(false).forEach((group, value) -> {
                                        mq.put(group, Double.parseDouble(String.valueOf(value))-1);
                                    });
                                    bonus.setWeightMQ(mq);
                                }
                                case "time" -> bonus.setTime(config.getDouble(key + ".modifier.time"));
                                case "difficulty" -> bonus.setDifficulty(config.getInt(key + ".modifier.difficulty"));
                                case "double-loot" -> bonus.setDoubleLoot(config.getDouble(key + ".modifier.double-loot"));
                                case "score" -> bonus.setScore(config.getDouble(key + ".modifier.score"));
                            }
                        });
                        BAIT.put(key, bonus);
                    }
                });
            }
            AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + BaitItem.size() + " <gray>baits");
        }
    }

    public static void loadCompetitions(){

        CompetitionsT.clear();
        CompetitionsC.clear();

        YamlConfiguration config = getConfig("competition.yml");

        Set<String> keys = config.getKeys(false);
        keys.forEach(key -> {
            CompetitionConfig competitionConfig;
            boolean enableBsb = config.getBoolean(key + ".bossbar.enable", false);
            if (enableBsb){
                competitionConfig = new CompetitionConfig(true);
                BossBarConfig bossBarConfig = new BossBarConfig(
                        config.getString(key + ".bossbar.text", "You forget to set text for bossbar"),
                        BossBar.Overlay.valueOf(config.getString(key + ".bossbar.overlay","SOLID").toUpperCase()),
                        BossBar.Color.valueOf(config.getString(key + ".bossbar.color","WHITE").toUpperCase()),
                        config.getInt(key + ".bossbar.refresh-rate",5)
                );
                competitionConfig.setBossBarConfig(bossBarConfig);
            } else competitionConfig = new CompetitionConfig(false);

            competitionConfig.setDuration(config.getInt(key + ".duration",600));
            competitionConfig.setGoal(Goal.valueOf(config.getString(key + ".goal", "RANDOM")));
            if (config.contains(key + ".broadcast.start"))
                competitionConfig.setStartMessage(config.getStringList(key + ".broadcast.start"));
            if (config.contains(key + ".broadcast.end"))
                competitionConfig.setEndMessage(config.getStringList(key + ".broadcast.end"));
            if (config.contains(key + ".command.join"))
                competitionConfig.setJoinCommand(config.getStringList(key + ".command.join"));
            if (config.contains(key + ".command.start"))
                competitionConfig.setStartCommand(config.getStringList(key + ".command.start"));
            if (config.contains(key + ".command.end"))
                competitionConfig.setEndCommand(config.getStringList(key + ".command.end"));
            if (config.contains(key + ".min-players"))
                competitionConfig.setMinPlayers(config.getInt(key + ".min-players"));
            if (config.contains(key + ".prize")){
                HashMap<String, List<ActionB>> rewardsMap = new HashMap<>();
                config.getConfigurationSection(key + ".prize").getKeys(false).forEach(rank -> {
                    List<ActionB> rewards = new ArrayList<>();
                    if (config.contains(key + ".prize." + rank + ".messages"))
                        rewards.add(new MessageB(config.getStringList(key + ".prize." + rank + ".messages")));
                    if (config.contains(key + ".prize." + rank + ".commands"))
                        rewards.add(new CommandB(config.getStringList(key + ".prize." + rank + ".commands")));
                    rewardsMap.put(rank, rewards);
                });
                competitionConfig.setRewards(rewardsMap);
            }
            if (config.contains(key + ".start-time")){
                config.getStringList(key + ".start-time").forEach(time -> CompetitionsT.put(time, competitionConfig));
            }
            CompetitionsC.put(key, competitionConfig);
        });
    }

    public static void tryEnableJedis(){
        YamlConfiguration configuration = ConfigReader.getConfig("redis.yml");
        useRedis = false;
        if (configuration.getBoolean("redis.enable")){
            JedisUtil.initializeRedis(configuration);
            useRedis = true;
        }
    }

    public static void loadBars(){
        LAYOUT.clear();
        YamlConfiguration config = ConfigReader.getConfig("bars.yml");
        Set<String> keys = config.getKeys(false);
        keys.forEach(key -> {
            int range = config.getInt(key + ".range");
            Set<String> rates = Objects.requireNonNull(config.getConfigurationSection(key + ".layout")).getKeys(false);
            double[] successRate = new double[rates.size()];
            for(int i = 0; i < rates.size(); i++)
                successRate[i] = config.getDouble(key + ".layout." +(i + 1));
            int size = rates.size()*range -1;
            Layout layout = new Layout(range, successRate, size);
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

    public static void loadEnchants(){

        ENCHANTS.clear();

        YamlConfiguration config = ConfigReader.getConfig("enchant-bonus.yml");
        Set<String> keys = config.getKeys(false);
        keys.forEach(key -> {
            HashMap<Integer, Bonus> levelBonus = new HashMap<>();
            config.getConfigurationSection(key).getKeys(false).forEach(level -> {
                Bonus bonus = new Bonus();
                config.getConfigurationSection(key + "." + level).getKeys(false).forEach(modifier -> {
                    switch (modifier) {
                        case "weight-PM" -> {
                            HashMap<String, Integer> pm = new HashMap<>();
                            config.getConfigurationSection(key + "." + level + ".weight-PM").getValues(false).forEach((group, value) -> {
                                pm.put(group, (Integer) value);
                            });
                            bonus.setWeightPM(pm);
                        }
                        case "weight-MQ" -> {
                            HashMap<String, Double> mq = new HashMap<>();
                            config.getConfigurationSection(key + "." + level + ".weight-MQ").getValues(false).forEach((group, value) -> {
                                mq.put(group, Double.parseDouble(String.valueOf(value))-1);
                            });
                            bonus.setWeightMQ(mq);
                        }
                        case "time" -> bonus.setTime(config.getDouble(key + "." + level + ".time"));
                        case "difficulty" -> bonus.setDifficulty(config.getInt(key + "." + level + ".difficulty"));
                        case "double-loot" -> bonus.setDoubleLoot(config.getDouble(key + "." + level + ".double-loot"));
                        case "score" -> bonus.setScore(config.getDouble(key + "." + level + ".score"));
                    }
                });
                levelBonus.put(Integer.parseInt(level), bonus);
            });
            ENCHANTS.put(key, levelBonus);
        });
        AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + ENCHANTS.size() + " <gray>enchants bonus");
    }
}