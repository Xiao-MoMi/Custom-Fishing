package net.momirealms.customfishing.fishing.bar;

import org.bukkit.configuration.ConfigurationSection;

public class ModeThreeBar extends FishingBar {

    private final String fish_image;
    private final int fish_icon_width;
    private final String[] strain;
    private final String[] struggling_fish_image;
    private final int bar_effective_width;
    private final int fish_offset;
    private final int fish_start_position;
    private final int success_position;

    public ModeThreeBar(ConfigurationSection section) {
        super(section);
        this.fish_icon_width = section.getInt("arguments.fish-icon-width");
        this.fish_image = section.getString("subtitle.fish");
        this.strain = section.getStringList("strain").toArray(new String[0]);
        this.struggling_fish_image = section.getStringList("subtitle.struggling-fish").toArray(new String[0]);
        this.bar_effective_width = section.getInt("arguments.bar-effective-area-width");
        this.fish_offset = section.getInt("arguments.fish-offset");
        this.fish_start_position = section.getInt("arguments.fish-start-position");
        this.success_position = section.getInt("arguments.success-position");
    }

    public String getFish_image() {
        return fish_image;
    }

    public int getFish_icon_width() {
        return fish_icon_width;
    }

    public String[] getStrain() {
        return strain;
    }

    public int getBar_effective_width() {
        return bar_effective_width;
    }

    public int getFish_offset() {
        return fish_offset;
    }

    public int getFish_start_position() {
        return fish_start_position;
    }

    public int getSuccess_position() {
        return success_position;
    }

    public String[] getStruggling_fish_image() {
        return struggling_fish_image;
    }
}
