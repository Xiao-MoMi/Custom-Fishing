package net.momirealms.customfishing.fishing.bar;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Random;

public abstract class FishingBar {

    protected String[] titles;
    protected String font;
    protected String barImage;

    public FishingBar(ConfigurationSection section) {
        this.titles = section.getStringList("title").size() == 0 ? new String[]{section.getString("title")} : section.getStringList("title").toArray(new String[0]);
        this.font = section.getString("subtitle.font", "customfishing:bar");
        this.barImage = section.getString("subtitle.bar","뀃");
    }

    public String getRandomTitle() {
        return titles[new Random().nextInt(titles.length)];
    }

    public String getBarImage() {
        return barImage;
    }

    public String getFont() {
        return font;
    }
}
