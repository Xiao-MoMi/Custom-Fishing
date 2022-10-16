package net.momirealms.customfishing.integration.papi;

import me.clip.placeholderapi.PlaceholderAPI;
import net.momirealms.customfishing.Function;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderManager extends Function {

    private final Pattern placeholderPattern = Pattern.compile("%([^%]*)%");
    private final CompetitionPapi competitionPapi;

    public PlaceholderManager() {
        this.competitionPapi = new CompetitionPapi();
        load();
    }

    public String parse(Player player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public String parse(OfflinePlayer offlinePlayer, String text) {
        return PlaceholderAPI.setPlaceholders(offlinePlayer, text);
    }

    @Override
    public void load() {
        competitionPapi.register();
    }

    @Override
    public void unload() {
        if (this.competitionPapi != null) competitionPapi.unregister();
    }

    public List<String> detectPlaceholders(String text){
        if (text == null || !text.contains("%")) return Collections.emptyList();
        List<String> placeholders = new ArrayList<>();
        Matcher matcher = placeholderPattern.matcher(text);
        while (matcher.find()) placeholders.add(matcher.group());
        return placeholders;
    }
}
