package net.momirealms.customfishing.requirements;

import me.clip.placeholderapi.PlaceholderAPI;
import net.momirealms.customfishing.ConfigReader;
import org.bukkit.ChatColor;

import java.util.List;

public record Season(List<String> seasons) implements Requirement {

    public List<String> getSeasons() {
        return this.seasons;
    }

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
        String currentSeason = ChatColor.stripColor(PlaceholderAPI.setPlaceholders(fishingCondition.getPlayer(), ConfigReader.Config.season_papi));
        for (String season : seasons) {
            if (season.equalsIgnoreCase(currentSeason)) {
                return true;
            }
        }
        return false;
    }
}