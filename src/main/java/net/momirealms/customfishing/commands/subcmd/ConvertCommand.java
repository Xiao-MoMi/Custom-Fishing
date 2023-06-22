package net.momirealms.customfishing.commands.subcmd;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.commands.AbstractSubCommand;
import net.momirealms.customfishing.util.AdventureUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConvertCommand extends AbstractSubCommand {

    public static final ConvertCommand INSTANCE = new ConvertCommand();

    public ConvertCommand() {
        super("convert");
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        convertItems("loots");
        convertItems("mobs");
        AdventureUtils.sendMessage(sender, "<red>Done! Files are saved to your /loots & /mobs folder");
        AdventureUtils.sendMessage(sender, "<red>Converted files are named by converted-xxx.yml.");
        AdventureUtils.sendMessage(sender, "<red>Do a quick check and delete your old files to make them work");
        return true;
    }

    private void convertItems(String folder) {
        File loot_file = new File(CustomFishing.getInstance().getDataFolder() + File.separator + "contents" + File.separator + folder);
        if (!loot_file.exists()) return;
        File[] files = loot_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.getName().startsWith("converted-")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            for (String key : config.getKeys(false)) {
                ConfigurationSection section = config.getConfigurationSection(key);
                if (section == null || !section.contains("action")) continue;
                ConfigurationSection actionSec = section.getConfigurationSection("action");
                if (actionSec == null) continue;
                for (String event : new String[]{"success", "fail", "failure", "hook", "consume"}) {
                    convertSec(actionSec, event);
                }
                ConfigurationSection successTimesSection = actionSec.getConfigurationSection("success-times");
                if (successTimesSection != null) {
                    for (String times : successTimesSection.getKeys(false)) {
                        convertSec(successTimesSection, times);
                    }
                }
            }
            try {
                config.save(new File(file.getParentFile(), "converted-"+file.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void convertSec(ConfigurationSection actionSec, String event) {
        ConfigurationSection eventSec = actionSec.getConfigurationSection(event);
        if (eventSec == null) return;
        for (String actionType : eventSec.getKeys(false)) {
            switch (actionType) {
                case "message", "command" -> {
                    ConfigurationSection newSec = eventSec.createSection("action" + "_" + actionType);
                    newSec.set("type", actionType);
                    newSec.set("value", eventSec.getStringList(actionType));
                    newSec.set("chance", eventSec.getDouble(actionType + "-chance",1));
                }
                case "exp", "mending" -> {
                    ConfigurationSection newSec = eventSec.createSection("action" + "_" + actionType);
                    newSec.set("type", actionType);
                    newSec.set("value", eventSec.getInt(actionType));
                    newSec.set("chance", eventSec.getDouble(actionType + "-chance",1));
                }
                case "skill-xp", "job-xp" -> {
                    ConfigurationSection newSec = eventSec.createSection("action" + "_" + actionType);
                    newSec.set("type", actionType);
                    newSec.set("value", eventSec.getDouble(actionType));
                    newSec.set("chance", eventSec.getDouble(actionType + "-chance",1));
                }
                case "sound" -> {
                    ConfigurationSection newSec = eventSec.createSection("action" + "_" + actionType);
                    newSec.set("type", actionType);
                    newSec.set("value.source", eventSec.getString(actionType + ".source"));
                    newSec.set("value.key", eventSec.getString(actionType + ".key"));
                    newSec.set("value.volume", eventSec.getDouble(actionType + ".volume"));
                    newSec.set("value.pitch", eventSec.getDouble(actionType + ".pitch"));
                }
                case "potion-effect" -> {
                    ConfigurationSection potionSec = eventSec.getConfigurationSection(actionType);
                    if (potionSec != null) {
                        for (String potion : potionSec.getKeys(false)) {
                            ConfigurationSection newSec = eventSec.createSection("potion" + "_" + potion);
                            newSec.set("type", actionType);
                            newSec.set("value.type", potionSec.getString(potion + ".type"));
                            newSec.set("value.amplifier", potionSec.getInt(potion + ".amplifier"));
                            newSec.set("value.duration", potionSec.getInt(potion + ".duration"));
                        }
                    }
                }
            }
            eventSec.set(actionType, null);
        }
    }
}
