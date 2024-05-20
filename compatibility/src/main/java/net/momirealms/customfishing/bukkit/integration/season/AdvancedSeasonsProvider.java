package net.momirealms.customfishing.bukkit.integration.season;

import net.advancedplugins.seasons.api.AdvancedSeasonsAPI;
import net.momirealms.customfishing.api.integration.SeasonProvider;
import net.momirealms.customfishing.api.mechanic.misc.season.Season;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class AdvancedSeasonsProvider implements SeasonProvider {

    private final AdvancedSeasonsAPI api;

    public AdvancedSeasonsProvider() {
        this.api = new AdvancedSeasonsAPI();
    }

    @NotNull
    @Override
    public Season getSeason(@NotNull World world) {
        return switch (api.getSeason(world)) {
            case "SPRING" -> Season.SPRING;
            case "WINTER" -> Season.WINTER;
            case "SUMMER" -> Season.SUMMER;
            case "FALL" -> Season.AUTUMN;
            default -> Season.DISABLE;
        };
    }

    @Override
    public String identifier() {
        return "AdvancedSeasons";
    }
}
