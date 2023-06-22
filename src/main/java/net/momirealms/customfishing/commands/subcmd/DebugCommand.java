package net.momirealms.customfishing.commands.subcmd;

import net.momirealms.biomeapi.BiomeAPI;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.api.CustomFishingAPI;
import net.momirealms.customfishing.commands.AbstractSubCommand;
import net.momirealms.customfishing.fishing.Effect;
import net.momirealms.customfishing.fishing.loot.Loot;
import net.momirealms.customfishing.integration.SeasonInterface;
import net.momirealms.customfishing.util.AdventureUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DebugCommand extends AbstractSubCommand {

    public static final DebugCommand INSTANCE = new DebugCommand();

    public DebugCommand() {
        super("debug");
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (lackArgs(sender, 1, args.size()) || noConsoleExecute(sender)) return true;
        Player player = (Player) sender;
        switch (args.get(0)) {
            case "biome" -> {
                AdventureUtils.playerMessage(player, BiomeAPI.getBiome(player.getLocation()));
            }
            case "time" -> {
                AdventureUtils.playerMessage(player, String.valueOf(player.getWorld().getTime()));
            }
            case "world" -> {
                AdventureUtils.playerMessage(player, player.getWorld().getName());
            }
            case "season" -> {
                SeasonInterface seasonInterface = CustomFishing.getInstance().getIntegrationManager().getSeasonInterface();
                if (seasonInterface == null) return true;
                AdventureUtils.playerMessage(player, seasonInterface.getSeason(player.getLocation().getWorld()));
            }
            case "loot-chance" -> {
                Effect initial = CustomFishing.getInstance().getFishingManager().getInitialEffect(player);
                List<String> lootProbability = getLootProbability(initial, CustomFishingAPI.getLootsAt(player.getLocation(), player));
                for (String msg : lootProbability) {
                    AdventureUtils.playerMessage(player, msg);
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, List<String> args) {
        if (args.size() == 1) {
            return filterStartingWith(List.of("biome", "time", "world", "season", "loot-chance"), args.get(0));
        }
        return null;
    }

    public ArrayList<String> getLootProbability(Effect initialEffect, List<Loot> possibleLoots) {
        List<Loot> availableLoots = new ArrayList<>();
        HashMap<String, Integer> as = initialEffect.getWeightAS();
        HashMap<String, Double> md = initialEffect.getWeightMD();
        double[] weights = new double[possibleLoots.size()];
        int index = 0;
        for (Loot loot : possibleLoots){
            double weight = loot.getWeight();
            String group = loot.getGroup();
            if (group != null){
                if (as.get(group) != null){
                    weight += as.get(group);
                }
                if (md.get(group) != null){
                    weight *= md.get(group);
                }
            }
            if (weight <= 0) continue;
            availableLoots.add(loot);
            weights[index++] = weight;
        }
        ArrayList<String> lootWithChance = new ArrayList<>(availableLoots.size());
        double total = Arrays.stream(weights).sum();
        for (int i = 0; i < index; i++){
            lootWithChance.add(availableLoots.get(i).getKey() + ": <gold>" + String.format("%.2f", weights[i]*100/total) + "%");
        }
        return lootWithChance;
    }
}
