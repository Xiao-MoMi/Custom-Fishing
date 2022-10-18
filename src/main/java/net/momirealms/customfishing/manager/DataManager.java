package net.momirealms.customfishing.manager;

import net.momirealms.customfishing.object.Function;
import net.momirealms.customfishing.util.ConfigUtil;
import org.bukkit.configuration.file.YamlConfiguration;

public class DataManager extends Function {

    public static String user;
    public static String password;
    public static String url;
    public static String ENCODING;
    public static String tableName;
    public static boolean enable_pool;
    public static int maximum_pool_size;
    public static int minimum_idle;
    public static int maximum_lifetime;
    public static int idle_timeout;

    @Override
    public void load() {
        super.load();
    }

    @Override
    public void unload() {
        super.unload();
    }

    public void loadConfig() {
        YamlConfiguration config = ConfigUtil.getConfig("database.yml");

    }
}
