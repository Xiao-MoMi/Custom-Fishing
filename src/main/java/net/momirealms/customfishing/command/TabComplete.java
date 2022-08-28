package net.momirealms.customfishing.command;

import net.momirealms.customfishing.ConfigReader;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabComplete implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        if (!(sender.hasPermission("customfishing.admin") || sender.isOp())) {
            return null;
        }
        if (args.length == 1){
            List<String> arrayList = new ArrayList<>();
            for (String cmd : Arrays.asList("competition","reload", "items","import")) {
                if (cmd.startsWith(args[0]))
                    arrayList.add(cmd);
            }
            return arrayList;
        }
        if (args.length == 2){
            if (args[0].equalsIgnoreCase("items")){
                List<String> arrayList = new ArrayList<>();
                for (String cmd : Arrays.asList("loot","bait","rod","util")) {
                    if (cmd.startsWith(args[1]))
                        arrayList.add(cmd);
                }
                return arrayList;
            }
            if (args[0].equalsIgnoreCase("import")){
                return List.of("FileName");
            }
            if (args[0].equalsIgnoreCase("competition")){
                List<String> arrayList = new ArrayList<>();
                for (String cmd : List.of("start","end","cancel")) {
                    if (cmd.startsWith(args[1]))
                        arrayList.add(cmd);
                }
                return arrayList;
            }
        }
        if (args.length == 3){
            if (args[0].equalsIgnoreCase("items")){
                if (args[1].equalsIgnoreCase("loot") || args[1].equalsIgnoreCase("util") || args[1].equalsIgnoreCase("rod") || args[1].equalsIgnoreCase("bait")){
                    List<String> arrayList = new ArrayList<>();
                    for (String cmd : Arrays.asList("get","give")) {
                        if (cmd.startsWith(args[2]))
                            arrayList.add(cmd);
                    }
                    return arrayList;
                }
            }
            if (args[0].equalsIgnoreCase("competition")){
                if (args[1].equalsIgnoreCase("start")){
                    List<String> arrayList = new ArrayList<>();
                    for (String cmd : competitions()) {
                        if (cmd.startsWith(args[2]))
                            arrayList.add(cmd);
                    }
                    return arrayList;
                }
            }
        }
        if (args.length == 4){
            if (args[0].equalsIgnoreCase("items")){
                if (args[1].equalsIgnoreCase("loot")){
                    if (args[2].equalsIgnoreCase("give")){
                        List<String> arrayList = new ArrayList<>();
                        for (String cmd : online_players()) {
                            if (cmd.startsWith(args[3]))
                                arrayList.add(cmd);
                        }
                        return arrayList;
                    }
                    if (args[2].equalsIgnoreCase("get")){
                        List<String> arrayList = new ArrayList<>();
                        for (String cmd : loots()) {
                            if (cmd.startsWith(args[3]))
                                arrayList.add(cmd);
                        }
                        return arrayList;
                    }
                }else if (args[1].equalsIgnoreCase("util")){
                    if (args[2].equalsIgnoreCase("give")){
                        List<String> arrayList = new ArrayList<>();
                        for (String cmd : online_players()) {
                            if (cmd.startsWith(args[3]))
                                arrayList.add(cmd);
                        }
                        return arrayList;
                    }
                    if (args[2].equalsIgnoreCase("get")){
                        List<String> arrayList = new ArrayList<>();
                        for (String cmd : utils()) {
                            if (cmd.startsWith(args[3]))
                                arrayList.add(cmd);
                        }
                        return arrayList;
                    }
                }else if (args[1].equalsIgnoreCase("rod")){
                    if (args[2].equalsIgnoreCase("give")){
                        List<String> arrayList = new ArrayList<>();
                        for (String cmd : online_players()) {
                            if (cmd.startsWith(args[3]))
                                arrayList.add(cmd);
                        }
                        return arrayList;
                    }
                    if (args[2].equalsIgnoreCase("get")){
                        List<String> arrayList = new ArrayList<>();
                        for (String cmd : rods()) {
                            if (cmd.startsWith(args[3]))
                                arrayList.add(cmd);
                        }
                        return arrayList;
                    }
                }else if (args[1].equalsIgnoreCase("bait")){
                    if (args[2].equalsIgnoreCase("give")){
                        List<String> arrayList = new ArrayList<>();
                        for (String cmd : online_players()) {
                            if (cmd.startsWith(args[3]))
                                arrayList.add(cmd);
                        }
                        return arrayList;
                    }
                    if (args[2].equalsIgnoreCase("get")){
                        List<String> arrayList = new ArrayList<>();
                        for (String cmd : baits()) {
                            if (cmd.startsWith(args[3]))
                                arrayList.add(cmd);
                        }
                        return arrayList;
                    }
                }
            }
        }
        if (args.length == 5){
            if (args[0].equalsIgnoreCase("items")){
                if (args[1].equalsIgnoreCase("loot")){
                    if (args[2].equalsIgnoreCase("give")){
                        List<String> arrayList = new ArrayList<>();
                        for (String cmd : loots()) {
                            if (cmd.startsWith(args[4]))
                                arrayList.add(cmd);
                        }
                        return arrayList;
                    }
                    if (args[2].equalsIgnoreCase("get")){
                        return Arrays.asList("1","16","64");
                    }
                }else if (args[1].equalsIgnoreCase("util")){
                    if (args[2].equalsIgnoreCase("give")){
                        List<String> arrayList = new ArrayList<>();
                        for (String cmd : utils()) {
                            if (cmd.startsWith(args[4]))
                                arrayList.add(cmd);
                        }
                        return arrayList;
                    }
                    if (args[2].equalsIgnoreCase("get")){
                        return Arrays.asList("1","16","64");
                    }
                }else if (args[1].equalsIgnoreCase("rod")){
                    if (args[2].equalsIgnoreCase("give")){
                        List<String> arrayList = new ArrayList<>();
                        for (String cmd : rods()) {
                            if (cmd.startsWith(args[4]))
                                arrayList.add(cmd);
                        }
                        return arrayList;
                    }
                    if (args[2].equalsIgnoreCase("get")){
                        return Arrays.asList("1","16","64");
                    }
                }
                else if (args[1].equalsIgnoreCase("bait")){
                    if (args[2].equalsIgnoreCase("give")){
                        List<String> arrayList = new ArrayList<>();
                        for (String cmd : baits()) {
                            if (cmd.startsWith(args[4]))
                                arrayList.add(cmd);
                        }
                        return arrayList;
                    }
                    if (args[2].equalsIgnoreCase("get")){
                        return Arrays.asList("1","16","64");
                    }
                }
            }
        }
        if (args.length == 6){
            if (args[0].equalsIgnoreCase("items")){
                if (args[1].equalsIgnoreCase("loot")){
                    if (args[2].equalsIgnoreCase("give")){
                        return Arrays.asList("1","16","64");
                    }
                }else if (args[1].equalsIgnoreCase("util")){
                    if (args[2].equalsIgnoreCase("give")){
                        return Arrays.asList("1","16","64");
                    }
                }else if (args[1].equalsIgnoreCase("rod")){
                    if (args[2].equalsIgnoreCase("give")){
                        return List.of("1");
                    }
                }
                else if (args[1].equalsIgnoreCase("bait")){
                    if (args[2].equalsIgnoreCase("give")){
                        return Arrays.asList("1","16","64");
                    }
                }
            }
        }
        return null;
    }

    private List<String> online_players(){
        List<String> online = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach((player -> online.add(player.getName())));
        return online;
    }

    private List<String> loots(){
        return new ArrayList<>(ConfigReader.LootItem.keySet());
    }
    private List<String> utils(){
        return new ArrayList<>(ConfigReader.UtilItem.keySet());
    }
    private List<String> rods() {
        return new ArrayList<>(ConfigReader.RodItem.keySet());
    }
    private List<String> baits() {
        return new ArrayList<>(ConfigReader.BaitItem.keySet());
    }
    private List<String> competitions() {
        return new ArrayList<>(ConfigReader.CompetitionsC.keySet());
    }
}
