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

package net.momirealms.customfishing.manager;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.util.ConfigUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class MessageManager {

    public static String prefix;
    public static String reload;
    public static String nonArgs;
    public static String unavailableArgs;
    public static String escape;
    public static String noPerm;
    public static String itemNotExist;
    public static String playerNotExist;
    public static String noConsole;
    public static String wrongAmount;
    public static String lackArgs;
    public static String notOnline;
    public static String giveItem;
    public static String getItem;
    public static String possibleLoots;
    public static String splitChar;
    public static String noLoot;
    public static String competitionOn;
    public static String notEnoughPlayers;
    public static String noRank;
    public static String forceSuccess;
    public static String forceFailure;
    public static String forceEnd;
    public static String forceCancel;
    public static String noPlayer;
    public static String noScore;
    public static String noRod;
    public static String hookOther;
    public static String reachSellLimit;
    public static String setStatistics;
    public static String resetStatistics;
    public static String negativeStatistics;
    public static String statisticsNotExists;

    public static void load() {
        YamlConfiguration config = ConfigUtils.getConfig("messages" + File.separator + "messages_" + ConfigManager.lang +".yml");
        prefix = getOrSet(config, "prefix", "<gradient:#0070B3:#A0EACF>[CustomFishing] </gradient>");
        reload = getOrSet(config, "reload", "<white>Reloaded. Took <green>{time}ms.");
        nonArgs = getOrSet(config, "none-args", "Arguments cannot be none.");
        unavailableArgs = getOrSet(config, "invalid-args", "Invalid arguments.");
        escape = getOrSet(config, "escape", "It has been too long since the fish is hooked. Oh my god, it escaped.");
        noPerm = getOrSet(config, "no-perm", "You don''t have permission.");
        itemNotExist = getOrSet(config, "item-not-exist", "That item does not exist.");
        playerNotExist = getOrSet(config, "player-not-exist", "That player does not exist.");
        noConsole = getOrSet(config, "no-console", "This command cannot be executed from the console.");
        wrongAmount = getOrSet(config, "wrong-amount", "You can''t set an negative amount of items.");
        lackArgs = getOrSet(config, "lack-args", "Insufficient arguments.");
        notOnline = getOrSet(config, "not-online", "That player is not online.");
        giveItem = getOrSet(config, "give-item", "Successfully given player {Player} {Amount}x {Item}.");
        getItem = getOrSet(config, "get-item", "Successfully got {Amount}x {Item}.");
        possibleLoots = getOrSet(config, "possible-loots", "Possible loots here: ");
        splitChar = getOrSet(config, "split-char", ", ");
        noLoot = getOrSet(config, "no-loot", "There''s no fish in this place.");
        competitionOn = getOrSet(config, "competition-ongoing", "There is currently a fishing competition in progress! Start fishing to join the competition for a prize.");
        notEnoughPlayers = getOrSet(config, "players-not-enough", "The number of players is not enough for the fishing competition to be started as scheduled.");
        noRank = getOrSet(config, "no-rank", "No Rank");
        forceSuccess = getOrSet(config, "force-competition-success", "Forced to start a fishing competition.");
        forceFailure = getOrSet(config, "force-competition-failure", "The competition does not exist.");
        forceEnd = getOrSet(config, "force-competition-end", "Forced to end the current competition.");
        forceCancel = getOrSet(config, "force-competition-cancel", "Forced to cancel the competition");
        noPlayer = getOrSet(config, "no-player", "No player");
        noScore = getOrSet(config, "no-score", "No score");
        noRod = getOrSet(config, "no-rod", "You have to fish with a special rod to get loots.");
        hookOther = getOrSet(config, "hook-other-entity", "The hook is hooked on another entity.");
        reachSellLimit = getOrSet(config, "reach-sell-limit", "You have earned too much from selling fish! Come tomorrow.");
        setStatistics = getOrSet(config, "set-statistics", "Successfully set {Player}''s {Loot} statistics to {Amount}.");
        resetStatistics = getOrSet(config, "reset-statistics", "Successfully reset {Player}''s statistics.");
        negativeStatistics = getOrSet(config, "negative-statistics", "Amount should be a value no lower than zero.");
        statisticsNotExists = getOrSet(config, "statistics-not-exist", "The statistics does not exist.");
        try {
            config.save(new File(CustomFishing.getInstance().getDataFolder(), "messages" + File.separator + "messages_" + ConfigManager.lang +".yml"));
        } catch (IOException ignore) {
        }
    }

    private static String getOrSet(ConfigurationSection section, String path, String defaultValue) {
        path = "messages." + path;
        if (!section.contains(path)) {
            section.set(path, defaultValue);
        }
        return section.getString(path);
    }
}
