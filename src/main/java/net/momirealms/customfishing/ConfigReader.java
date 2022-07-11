package net.momirealms.customfishing;

import net.momirealms.customfishing.requirements.*;
import net.momirealms.customfishing.utils.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class ConfigReader{

    public static HashMap<String, LootInstance> LOOT = new HashMap<>();
    public static HashMap<String, ItemStack> LOOTITEM = new HashMap<>();
    public static HashMap<String, UtilInstance> UTIL = new HashMap<>();
    public static HashMap<String, ItemStack> UTILITEM = new HashMap<>();
    public static HashMap<String, RodInstance> ROD = new HashMap<>();
    public static HashMap<String, ItemStack> RODITEM = new HashMap<>();
    public static HashMap<String, LayoutUtil> LAYOUT = new HashMap<>();

    private static YamlConfiguration getConfig(String configName) {
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
        loadLoot();
        loadUtil();
        loadRod();
    }

    public static class Config {

        public static boolean wg;
        public static boolean mm;
        public static boolean papi;
        public static boolean season;
        public static boolean vanillaDrop;
        public static boolean needOpenWater;
        public static boolean needSpecialRod;
        public static String season_papi;
        public static int fishFinderCoolDown;

        public static void loadConfig() {
            CustomFishing.instance.saveDefaultConfig();
            CustomFishing.instance.reloadConfig();
            FileConfiguration config = CustomFishing.instance.getConfig();

            wg = config.getBoolean("config.integrations.WorldGuard");
            if (wg){
                if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null){
                    AdventureManager.consoleMessage("<red>[CustomFishing] 未检测到 WorldGuard!</red>");
                    wg = false;
                }else {
                    AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><green>WorldGuard Hooked!");
                }
            }
            mm = config.getBoolean("config.integrations.MythicMobs");
            if (mm){
                if (Bukkit.getPluginManager().getPlugin("MythicMobs") == null){
                    AdventureManager.consoleMessage("<red>[CustomFishing] 未检测到 MythicMobs!</red>");
                    mm = false;
                }else {
                    AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><green>MythicMobs Hooked!");
                }
            }
            papi = config.getBoolean("config.integrations.PlaceholderAPI");
            if (papi){
                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null){
                    AdventureManager.consoleMessage("<red>[CustomFishing] 未检测到 PlaceholderAPI!</red>");
                    papi = false;
                }else {
                    AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><green>PlaceholderAPI Hooked!");
                }
            }
            season = config.getBoolean("config.season.enable");
            if (!papi && season) {
                season = false;
                AdventureManager.consoleMessage("<red>[CustomFishing] 使用季节特性请先打开 PlaceholderAPI 兼容!</red>");
            }

            if (season) {
                season_papi = config.getString("config.season.papi");
            }else {
                season_papi = null;
            }

            vanillaDrop = config.getBoolean("config.vanilla-loot-when-no-custom-fish");
            needOpenWater = config.getBoolean("config.need-open-water");
            needSpecialRod = config.getBoolean("config.need-special-rod");
            fishFinderCoolDown = config.getInt("config.fishfinder-cooldown");

            /*
            计算获取布局
             */
            LAYOUT.clear();
            Set<String> keys = Objects.requireNonNull(config.getConfigurationSection("config.success-rate")).getKeys(false);
            keys.forEach(key -> {
                int range = config.getInt("config.success-rate." + key + ".range");
                Set<String> rates = Objects.requireNonNull(config.getConfigurationSection("config.success-rate." + key + ".layout")).getKeys(false);
                double[] successRate = new double[rates.size()];
                for(int i = 0; i < rates.size(); i++){
                    successRate[i] = config.getDouble("config.success-rate." + key + ".layout." +(i + 1));
                }
                int size = rates.size()*range -1;
                LayoutUtil layout = new LayoutUtil(key, range, successRate, size);
                layout.setTitle(config.getString("config.success-rate." + key + ".title"," "));
                layout.setBar(config.getString("config.success-rate." + key + ".subtitle.bar","뀃"));
                layout.setEnd(config.getString("config.success-rate." + key + ".subtitle.end","</font>"));
                layout.setStart(config.getString("config.success-rate." + key + ".subtitle.start","<font:customfishing:default>"));
                layout.setPointer(config.getString("config.success-rate." + key + ".subtitle.pointer","뀄"));
                layout.setPointerOffset(config.getString("config.success-rate." + key + ".subtitle.pointer_offset","뀂"));
                layout.setOffset(config.getString("config.success-rate." + key + ".subtitle.offset","뀁"));
                LAYOUT.put(key, layout);
            });
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

        public static void loadMessage() {
            YamlConfiguration config = getConfig("messages.yml");

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
            /*
            必设置的内容，为构造所需
             */
            String name;
            if (config.contains("items." + key + ".display.name")) {
                name = config.getString("items." + key + ".display.name");
            } else {
                AdventureManager.consoleMessage("<red>[CustomFishing] 错误! 未设置 " + key + " 的物品名称!</red>");
                return;
            }
            Difficulty difficulty;
            if (config.contains("items." + key + ".difficulty")) {
                String[] split = StringUtils.split(config.getString("items." + key + ".difficulty"), "-");
                assert split != null;
                if (Integer.parseInt(split[1]) <= 0 || Integer.parseInt(split[0]) <= 0){
                    AdventureManager.consoleMessage("<red>[CustomFishing] 错误! " + key + " 的捕获难度必须为正整数与正整数的组合!</red>");
                    return;
                }else {
                    difficulty = new Difficulty(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                }
            } else {
                AdventureManager.consoleMessage("<red>[CustomFishing] 错误! 未设置 " + key + " 的捕获难度!</red>");
                return;
            }
            int weight;
            if (config.contains("items." + key + ".weight")) {
                weight = config.getInt("items." + key + ".weight");
                if (weight <= 0){
                    AdventureManager.consoleMessage("<red>[CustomFishing] 错误! " + key + " 的捕获权重必须为正整数!</red>");
                    return;
                }
            } else {
                AdventureManager.consoleMessage("<red>[CustomFishing] 错误! 未设置 " + key + " 的捕获权重!</red>");
                return;
            }
            int time;
            if (config.contains("items." + key + ".time")) {
                time = config.getInt("items." + key + ".time");
                if (time <= 0){
                    AdventureManager.consoleMessage("<red>[CustomFishing] 错误! " + key + " 的捕捉时间必须为正整数!</red>");
                    return;
                }
            } else {
                AdventureManager.consoleMessage("<red>[CustomFishing] 错误! 未设置 " + key + " 的捕捉时间!</red>");
                return;
            }

            //新建单例
            LootInstance loot = new LootInstance(key, name, difficulty, weight, time);

            /*
            必须设置的内容，但并非构造所需
             */
            if (config.contains("items." + key + ".material")) {
                loot.setMaterial(config.getString("items." + key + ".material"));
            } else {
                AdventureManager.consoleMessage("<red>[CustomFishing] 错误! 未设置 " + key + " 的物品材质!</red>");
                return;
            }
            /*
            可选的设置内容
             */
            if (config.contains("items." + key + ".display.lore"))
                loot.setLore(config.getStringList("items." + key + ".display.lore"));

            if (config.contains("items." + key + ".nbt"))
                loot.setNbt(config.getMapList("items." + key + ".nbt").get(0));

            if (config.contains("items."+ key +".nick")){
                loot.setNick(config.getString("items."+key+".nick"));
            }else {
                loot.setNick(loot.getName());
            }

            if (config.contains("items." + key + ".action.message"))
                loot.setMsg(config.getString("items." + key + ".action.message"));
            if (config.contains("items." + key + ".action.command"))
                loot.setCommands(config.getStringList("items." + key + ".action.command"));
            if (config.contains("items." + key + ".action.exp"))
                loot.setExp(config.getInt("items." + key + ".action.exp"));
            if (config.contains("items." + key + ".layout"))
                loot.setLayout(config.getString("items." + key + ".layout"));
            if (config.contains("items." + key + ".group"))
                loot.setGroup(config.getString("items." + key + ".group"));
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
                                AdventureManager.consoleMessage("<red>[CustomFishing] 使用季节特性请先在 config.yml 中启用季节特性!</red>");
                            }
                        }
                        case "world" -> requirements.add(new World(config.getStringList("items." + key + ".requirements.world")));
                        case "biome" -> requirements.add(new Biome(config.getStringList("items." + key + ".requirements.biome")));
                        case "permission" -> requirements.add(new Permission(config.getString("items." + key + ".requirements.permission")));
                        case "region" -> {
                            if (Config.wg){
                                requirements.add(new Region(config.getStringList("items." + key + ".requirements.regions")));
                            }else {
                                AdventureManager.consoleMessage("<red>[CustomFishing] 指定钓鱼区域需要启用 WorldGuard 兼容!</red>");
                            }
                        }
                        case "time" -> requirements.add(new Time(config.getStringList("items." + key + ".requirements.time")));
                    }
                });
                loot.setRequirements(requirements);
            }
            //添加单例进缓存
            LOOT.put(key, loot);
            //添加根据单例生成的NBT物品进缓存
            loot.addLoot2cache(key);
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
                    AdventureManager.consoleMessage("<red>[CustomFishing] 错误! 未设置 " + key + " 的怪物名称!</red>");
                    return;
                }
                Difficulty difficulty;
                if (config.contains("mobs." + key + ".difficulty")) {
                    String[] split = StringUtils.split(config.getString("mobs." + key + ".difficulty"), "-");
                    assert split != null;
                    if (Integer.parseInt(split[1]) <= 0 || Integer.parseInt(split[0]) <= 0){
                        AdventureManager.consoleMessage("<red>[CustomFishing] 错误! " + key + " 的捕获难度必须为正整数与正整数的组合!</red>");
                        return;
                    }else {
                        difficulty = new Difficulty(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                    }
                } else {
                    AdventureManager.consoleMessage("<red>[CustomFishing] 错误! 未设置 " + key + " 的捕获难度!</red>");
                    return;
                }
                int weight;
                if (config.contains("mobs." + key + ".weight")) {
                    weight = config.getInt("mobs." + key + ".weight");
                    if (weight <= 0){
                        AdventureManager.consoleMessage("<red>[CustomFishing] 错误! " + key + " 的捕获权重必须为正整数!</red>");
                        return;
                    }
                } else {
                    AdventureManager.consoleMessage("<red>[CustomFishing] 错误! 未设置 " + key + " 的捕获权重!</red>");
                    return;
                }
                int time;
                if (config.contains("mobs." + key + ".time")) {
                    time = config.getInt("mobs." + key + ".time");
                    if (time <= 0){
                        AdventureManager.consoleMessage("<red>[CustomFishing] 错误! " + key + " 的捕捉时间必须为正整数!</red>");
                        return;
                    }
                } else {
                    AdventureManager.consoleMessage("<red>[CustomFishing] 错误! 未设置 " + key + " 的捕捉时间!</red>");
                    return;
                }
                //新建单例
                LootInstance loot = new LootInstance(key, name, difficulty, weight, time);
                //设置昵称
                loot.setNick(name);
                //设置MM怪ID
                if (config.contains("mobs." + key + ".mythicmobsID")) {
                    loot.setMm(config.getString("mobs." + key + ".mythicmobsID"));
                } else {
                    AdventureManager.consoleMessage("<red>[CustomFishing] 错误! 未设置 " + key + " 的MM怪ID!</red>");
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
                    loot.setMsg(config.getString("mobs." + key + ".action.message"));
                if (config.contains("mobs." + key + ".action.command"))
                    loot.setCommands(config.getStringList("mobs." + key + ".action.command"));
                if (config.contains("mobs." + key + ".action.exp"))
                    loot.setExp(config.getInt("mobs." + key + ".action.exp"));
                if (config.contains("mobs." + key + ".layout"))
                    loot.setLayout(config.getString("mobs." + key + "layout"));
                if (config.contains("mobs." + key + ".group"))
                    loot.setGroup(config.getString("mobs." + key + ".group"));
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
                                    AdventureManager.consoleMessage("<red>[CustomFishing] 使用季节特性请先在 config.yml 中启用季节特性!</red>");
                                }
                            }
                            case "world" -> requirements.add(new World(config.getStringList("mobs." + key + ".requirements.world")));
                            case "biome" -> requirements.add(new Biome(config.getStringList("mobs." + key + ".requirements.biome")));
                            case "permission" -> requirements.add(new Permission(config.getString("mobs." + key + ".requirements.permission")));
                            case "region" -> {
                                if (Config.wg){
                                    requirements.add(new Region(config.getStringList("mobs." + key + ".requirements.regions")));
                                }else {
                                    AdventureManager.consoleMessage("<red>[CustomFishing] 指定钓鱼区域需要启用 WorldGuard 兼容!</red>");
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
                AdventureManager.consoleMessage("<red>[CustomFishing] loots.yml 文件存在配置错误!</red>");
            } else {
                AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><white>从 loots.yml 载入了<green>" + keys.size() + "<white>条物品数据");
                AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><white>从 loots.yml 载入了<green>" + mobs.size() + "<white>条怪物数据");
            }
            return;
        }
        if (keys.size() != LOOTITEM.size()){
            AdventureManager.consoleMessage("<red>[CustomFishing] loots.yml 文件存在配置错误!</red>");
        } else {
            AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><white>从 loots.yml 载入了<green>" + keys.size() + "<white>条物品数据");
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
            String name;
            if (config.contains("utils." + key + ".display.name")) {
                name = config.getString("utils." + key + ".display.name");
            } else {
                AdventureManager.consoleMessage("<red>[CustomFishing] 错误! 未设置 " + key + " 的物品名称!</red>");
                return;
            }
            String material;
            if (config.contains("utils." + key + ".material")) {
                material = config.getString("utils." + key + ".material");
            } else {
                AdventureManager.consoleMessage("<red>[CustomFishing] 错误! 未设置 " + key + " 的物品材质!</red>");
                return;
            }
            //构造
            UtilInstance utilInstance = new UtilInstance(key, name, material);

            if (config.contains("utils." + key + ".display.lore"))
                utilInstance.setLore(config.getStringList("utils." + key + ".display.lore"));
            if (config.contains("utils." + key + ".nbt"))
                utilInstance.setNbt(config.getMapList("utils." + key + ".nbt").get(0));

            UTIL.put(key, utilInstance);
            utilInstance.addUtil2cache(key);
        });
        if (keys.size() != UTILITEM.size()){
            AdventureManager.consoleMessage("<red>[CustomFishing] utils.yml 文件存在配置错误!</red>");
        } else {
            AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><white>从 utils.yml 载入了<green>" + keys.size() + "<white>条工具数据");
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
            String name;
            if (config.contains("rods." + key + ".display.name")) {
                name = config.getString("rods." + key + ".display.name");
            } else {
                AdventureManager.consoleMessage("<red>[CustomFishing] 错误! 未设置 " + key + " 的物品名称!</red>");
                return;
            }
            RodInstance rodInstance = new RodInstance(name);
            if (config.contains("rods." + key + ".display.lore")) {
                rodInstance.setLore(config.getStringList("rods." + key + ".display.lore"));
            }
            if (config.contains("rods." + key + ".nbt")) {
                rodInstance.setNbt(config.getMapList("rods." + key + ".nbt").get(0));
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
                    }
                });
            }
            ROD.put(key, rodInstance);
            rodInstance.addRod2Cache(key);
        });

        if (keys.size() != RODITEM.size()){
            AdventureManager.consoleMessage("<red>[CustomFishing] rods.yml 文件存在配置错误!</red>");
        } else {
            AdventureManager.consoleMessage("<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient><white>从 rods.yml 载入了<green>" + keys.size() + "<white>条鱼竿数据");
        }
    }
}
