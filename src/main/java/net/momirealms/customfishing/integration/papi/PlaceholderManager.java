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

package net.momirealms.customfishing.integration.papi;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.competition.Competition;
import net.momirealms.customfishing.manager.ConfigManager;
import net.momirealms.customfishing.manager.MessageManager;
import net.momirealms.customfishing.object.Function;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderManager extends Function {

    private CustomFishing plugin;
    private final Pattern basicPattern = Pattern.compile("%([^%]*)%");
    private final Pattern betterPattern = Pattern.compile("\\{(.+?)\\}");
    private final Pattern allPattern = Pattern.compile("%([^%]*)%|\\{(.+?)\\}");
    private CompetitionPapi competitionPapi;
    private StatisticsPapi statisticsPapi;
    private boolean hasPlaceholderAPI = false;

    public PlaceholderManager(CustomFishing plugin) {
        this.plugin = plugin;
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            hasPlaceholderAPI = true;
            this.competitionPapi = new CompetitionPapi();
            this.statisticsPapi = new StatisticsPapi(plugin);
        }
    }

    @Override
    public void load() {
        if (competitionPapi != null) competitionPapi.register();
        if (statisticsPapi != null) statisticsPapi.register();
    }

    @Override
    public void unload() {
        if (this.competitionPapi != null) competitionPapi.unregister();
        if (this.statisticsPapi != null) statisticsPapi.unregister();
    }

    public String parse(OfflinePlayer player, String text) {
        if (hasPlaceholderAPI) {
            return ParseUtil.setPlaceholders(player, parseInner(player, text));
        } else {
            return parseInner(player, text);
        }
    }

    public String parseInner(OfflinePlayer player, String text) {
        List<String> papis = detectBetterPlaceholders(text);
        for (String papi : papis) {
            text = text.replace(papi, parseSingleInner(player, papi));
        }
        return text;
    }

    public String parseSinglePlaceholder(OfflinePlayer player, String placeholder) {
        if (placeholder.startsWith("{")) {
            return parseSingleInner(player, placeholder);
        } else if (hasPlaceholderAPI) {
            return ParseUtil.setPlaceholders(player, placeholder);
        }
        return placeholder;
    }

    public List<String> detectBasicPlaceholders(String text) {
        List<String> placeholders = new ArrayList<>();
        Matcher matcher = basicPattern.matcher(text);
        while (matcher.find()) placeholders.add(matcher.group());
        return placeholders;
    }

    public List<String> detectBetterPlaceholders(String text) {
        List<String> placeholders = new ArrayList<>();
        Matcher matcher = betterPattern.matcher(text);
        while (matcher.find()) placeholders.add(matcher.group());
        return placeholders;
    }

    public List<String> detectAllPlaceholders(String text) {
        List<String> placeholders = new ArrayList<>();
        Matcher matcher = allPattern.matcher(text);
        while (matcher.find()) placeholders.add(matcher.group());
        return placeholders;
    }

    public List<String> detectPlaceholders(String text) {
        return hasPlaceholderAPI ? detectAllPlaceholders(text) : detectBetterPlaceholders(text);
    }

    public String parseSingleInner(OfflinePlayer player, String placeholder) {
        switch (placeholder) {
            case "{player}" -> {
                return player == null ? placeholder : player.getName();
            }
            case "{date}" -> {
                return LocalDateTime.now().format(DateTimeFormatter.ofPattern(ConfigManager.dateFormat));
            }
            case "{time}" -> {
                return String.valueOf(Competition.currentCompetition.getRemainingTime());
            }
            case "{goal}" -> {
                return Competition.currentCompetition.getGoal().getDisplay();
            }
            case "{minute}" -> {
                return String.format("%02d", Competition.currentCompetition.getRemainingTime() / 60);
            }
            case "{second}" -> {
                return String.format("%02d", Competition.currentCompetition.getRemainingTime() % 60);
            }
            case "{rank}" -> {
                return player == null ? placeholder : Competition.currentCompetition.getPlayerRank(player);
            }
            case "{score}" -> {
                return player == null ? placeholder : String.format("%.1f", Competition.currentCompetition.getScore(player));
            }
            case "{1st_player}" -> {
                return Optional.ofNullable(Competition.currentCompetition.getRanking().getPlayerAt(1)).orElse(MessageManager.noPlayer);
            }
            case "{1st_score}" -> {
                return Competition.currentCompetition.getRanking().getScoreAt(1) <= 0 ? MessageManager.noScore : String.format("%.1f", Competition.currentCompetition.getRanking().getScoreAt(1));
            }
            case "{2nd_player}" -> {
                return Optional.ofNullable(Competition.currentCompetition.getRanking().getPlayerAt(2)).orElse(MessageManager.noPlayer);
            }
            case "{2nd_score}" -> {
                return Competition.currentCompetition.getRanking().getScoreAt(2) <= 0 ? MessageManager.noScore : String.format("%.1f", Competition.currentCompetition.getRanking().getScoreAt(2));
            }
            case "{3rd_player}" -> {
                return Optional.ofNullable(Competition.currentCompetition.getRanking().getPlayerAt(3)).orElse(MessageManager.noPlayer);
            }
            case "{3rd_score}" -> {
                return Competition.currentCompetition.getRanking().getScoreAt(3) <= 0 ? MessageManager.noScore : String.format("%.1f", Competition.currentCompetition.getRanking().getScoreAt(3));
            }
            default -> {
                return placeholder;
            }
        }
    }
}
