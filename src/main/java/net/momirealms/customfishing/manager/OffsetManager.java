package net.momirealms.customfishing.manager;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.object.Function;
import net.momirealms.customfishing.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class OffsetManager extends Function {

    private CustomFishing plugin;
    private final String[] negative;
    private final String[] positive;
    private String font;

    public OffsetManager(CustomFishing plugin) {
        this.plugin = plugin;
        this.negative = new String[8];
        this.positive = new String[8];
    }

    @Override
    public void load() {
        loadConfig();
    }

    public String getFont() {
        return font;
    }

    private void loadConfig() {
        YamlConfiguration config = ConfigUtil.getConfig("config.yml");
        ConfigurationSection section = config.getConfigurationSection("other-settings.offset-characters");
        if (section != null) {
            font = section.getString("font", "customfishing:offset_chars");
            positive[0] = section.getString("1");
            positive[1] = section.getString("2");
            positive[2] = section.getString("4");
            positive[3] = section.getString("8");
            positive[4] = section.getString("16");
            positive[5] = section.getString("32");
            positive[6] = section.getString("64");
            positive[7] = section.getString("128");
            negative[0] = section.getString("-1");
            negative[1] = section.getString("-2");
            negative[2] = section.getString("-4");
            negative[3] = section.getString("-8");
            negative[4] = section.getString("-16");
            negative[5] = section.getString("-32");
            negative[6] = section.getString("-64");
            negative[7] = section.getString("-128");
        }
    }

    public String getShortestNegChars(int n) {
        StringBuilder stringBuilder = new StringBuilder();
        while (n >= 128) {
            stringBuilder.append(negative[7]);
            n -= 128;
        }
        if (n - 64 >= 0) {
            stringBuilder.append(negative[6]);
            n -= 64;
        }
        if (n - 32 >= 0) {
            stringBuilder.append(negative[5]);
            n -= 32;
        }
        if (n - 16 >= 0) {
            stringBuilder.append(negative[4]);
            n -= 16;
        }
        if (n - 8 >= 0) {
            stringBuilder.append(negative[3]);
            n -= 8;
        }
        if (n - 4 >= 0) {
            stringBuilder.append(negative[2]);
            n -= 4;
        }
        if (n - 2 >= 0) {
            stringBuilder.append(negative[1]);
            n -= 2;
        }
        if (n - 1 >= 0) {
            stringBuilder.append(negative[0]);
        }
        return stringBuilder.toString();
    }

    public String getShortestPosChars(int n) {
        StringBuilder stringBuilder = new StringBuilder();
        while (n >= 128) {
            stringBuilder.append(positive[7]);
            n -= 128;
        }
        if (n - 64 >= 0) {
            stringBuilder.append(positive[6]);
            n -= 64;
        }
        if (n - 32 >= 0) {
            stringBuilder.append(positive[5]);
            n -= 32;
        }
        if (n - 16 >= 0) {
            stringBuilder.append(positive[4]);
            n -= 16;
        }
        if (n - 8 >= 0) {
            stringBuilder.append(positive[3]);
            n -= 8;
        }
        if (n - 4 >= 0) {
            stringBuilder.append(positive[2]);
            n -= 4;
        }
        if (n - 2 >= 0) {
            stringBuilder.append(positive[1]);
            n -= 2;
        }
        if (n - 1 >= 0) {
            stringBuilder.append(positive[0]);
        }
        return stringBuilder.toString();
    }

    public String getOffsetChars(int n) {
        if (n > 0) {
            return getShortestPosChars(n);
        }
        else {
            return getShortestNegChars(-n);
        }
    }
}
