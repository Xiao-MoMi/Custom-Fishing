package net.momirealms.customfishing.manager;

import net.momirealms.customfishing.util.ConfigUtil;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

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
    public static String coolDown;
    public static String possibleLoots;
    public static String splitChar;
    public static String noLoot;
    public static String notOpenWater;
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

    public static void load() {
        YamlConfiguration config = ConfigUtil.getConfig("messages" + File.separator + "messages_" + ConfigManager.lang +".yml");
        prefix = config.getString("messages.prefix", "messages.prefix is missing");
        reload = config.getString("messages.reload", "messages.reload is missing");
        nonArgs = config.getString("messages.none-args", "messages.none-args is missing");
        unavailableArgs = config.getString("messages.invalid-args", "messages.invalid-args is missing");
        escape = config.getString("messages.escape", "messages.escape is missing");
        noPerm = config.getString("messages.no-perm", "messages.no-perm is missing");
        itemNotExist = config.getString("messages.item-not-exist", "messages.item-not-exist is missing");
        playerNotExist = config.getString("messages.player-not-exist", "messages.player-not-exist is missing");
        noConsole = config.getString("messages.no-console", "messages.no-console is missing");
        wrongAmount = config.getString("messages.wrong-amount", "messages.wrong-amount is missing");
        lackArgs = config.getString("messages.lack-args", "messages.lack-args is missing");
        notOnline = config.getString("messages.not-online", "messages.not-online is missing");
        giveItem = config.getString("messages.give-item", "messages.give-item is missing");
        getItem = config.getString("messages.get-item", "messages.get-item is missing");
        coolDown = config.getString("messages.cooldown", "messages.cooldown is missing");
        possibleLoots = config.getString("messages.possible-loots", "messages.possible-loots is missing");
        splitChar = config.getString("messages.split-char", "messages.split-char is missing");
        noLoot = config.getString("messages.no-loot", "messages.no-loot is missing");
        notOpenWater = config.getString("messages.not-open-water", "messages.not-open-water is missing");
        competitionOn = config.getString("messages.competition-ongoing", "messages.competition-ongoing is missing");
        notEnoughPlayers = config.getString("messages.players-not-enough", "messages.players-not-enough is missing");
        noRank = config.getString("messages.no-rank", "messages.no-rank is missing");
        forceSuccess = config.getString("messages.force-competition-success", "messages.force-competition-success is missing");
        forceFailure = config.getString("messages.force-competition-failure", "messages.force-competition-failure is missing");
        forceEnd = config.getString("messages.force-competition-end", "messages.force-competition-end is missing");
        forceCancel = config.getString("messages.force-competition-cancel","messages.force-competition-cancel is messing");
        noPlayer = config.getString("messages.no-player", "messages.no-player is missing");
        noScore = config.getString("messages.no-score", "messages.no-score is missing");
        noRod = config.getString("messages.no-rod", "messages.no-rod is missing");
        hookOther = config.getString("messages.hook-other-entity","messages.hook-other-entity is missing");
    }
}
