package net.momirealms.customfishing.fishing.bar;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Random;

public class ModeTwoBar extends FishingBar {

    private final int[] time_requirements;
    private final String judgement_area_image;
    private final String fish_image;
    private final int bar_effective_width;
    private final int judgement_area_offset;
    private final int judgement_area_width;
    private final int fish_icon_width;
    private final String[] progress;
    private final double punishment;

    public ModeTwoBar(ConfigurationSection section) {
        super(section);
        this.time_requirements = section.getIntegerList("hold-time-requirements").stream().mapToInt(Integer::intValue).toArray();
        this.judgement_area_image = section.getString("subtitle.judgment-area");
        this.fish_image = section.getString("subtitle.fish");
        this.bar_effective_width = section.getInt("arguments.bar-effective-area-width");
        this.judgement_area_offset = section.getInt("arguments.judgment-area-offset");
        this.judgement_area_width = section.getInt("arguments.judgment-area-width");
        this.fish_icon_width = section.getInt("arguments.fish-icon-width");
        this.punishment = section.getDouble("arguments.punishment");
        this.progress = section.getStringList("progress").toArray(new String[0]);
    }

    public int getRandomTimeRequirement() {
        return time_requirements[new Random().nextInt(time_requirements.length)] * 20;
    }

    public String getJudgement_area_image() {
        return judgement_area_image;
    }

    public String getFish_image() {
        return fish_image;
    }

    public int getBar_effective_width() {
        return bar_effective_width;
    }

    public int getJudgement_area_offset() {
        return judgement_area_offset;
    }

    public int getJudgement_area_width() {
        return judgement_area_width;
    }

    public int getFish_icon_width() {
        return fish_icon_width;
    }

    public String[] getProgress() {
        return progress;
    }

    public double getPunishment() {
        return punishment;
    }
}
