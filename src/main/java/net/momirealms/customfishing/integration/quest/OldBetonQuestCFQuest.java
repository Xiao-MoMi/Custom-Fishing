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

package net.momirealms.customfishing.integration.quest;

import net.momirealms.customfishing.api.event.FishResultEvent;
import net.momirealms.customfishing.fishing.FishResult;
import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.Instruction;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.config.Config;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.objectives.FishObjective;
import pl.betoncraft.betonquest.utils.LogUtils;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.logging.Level;

public class OldBetonQuestCFQuest extends Objective implements Listener {

    private final HashSet<String> loot_ids = new HashSet<>();
    private final int amount;
    private final boolean notify;
    private final int notifyInterval;

    public OldBetonQuestCFQuest(Instruction instruction) throws InstructionParseException {
        super(instruction);
        this.template = FishData.class;
        this.notifyInterval = instruction.getInt(instruction.getOptional("notify"), 1);
        this.notify = instruction.hasArgument("notify") || this.notifyInterval > 1;
        this.amount = instruction.getInt(instruction.getOptional("amount"), 1);
        Collections.addAll(this.loot_ids, instruction.getArray());
    }

    public static void register() {
        BetonQuest.getInstance().registerObjectives("customfishing", OldBetonQuestCFQuest.class);
    }

    @Override
    public void start() {
        Bukkit.getPluginManager().registerEvents(this, BetonQuest.getInstance());
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public String getDefaultDataInstruction() {
        return Integer.toString(this.amount);
    }

    @Override
    public String getProperty(String name, String playerID) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "amount" ->
                    Integer.toString(this.amount - ((OldBetonQuestCFQuest.FishData) this.dataMap.get(playerID)).getAmount());
            case "left" -> Integer.toString(((OldBetonQuestCFQuest.FishData) this.dataMap.get(playerID)).getAmount());
            case "total" -> Integer.toString(this.amount);
            default -> "";
        };
    }

    private boolean isValidPlayer(Player player) {
        if (player == null) {
            return false;
        } else {
            return player.isOnline() && player.isValid();
        }
    }

    @EventHandler
    public void onFish(FishResultEvent event) {
        if (event.getResult() != FishResult.FAILURE) {
            String playerID = PlayerConverter.getID(event.getPlayer());
            if (this.containsPlayer(playerID)) {
                if (this.loot_ids.contains(event.getLoot_id())) {
                    if (this.checkConditions(playerID)) {
                        if (!isValidPlayer(event.getPlayer())) {
                            return;
                        }
                        FishData fishData = (FishData) this.dataMap.get(playerID);
                        fishData.catchFish(event.isDouble() ? 1 : 2);
                        if (fishData.finished()) {
                            this.completeObjective(playerID);
                        }
                        else if (this.notify && fishData.getAmount() % this.notifyInterval == 0) {
                            try {
                                Config.sendNotify(this.instruction.getPackage().getName(), playerID, "loot_to_fish", new String[]{String.valueOf(fishData.getAmount())}, "loot_to_fish,info");
                            } catch (QuestRuntimeException e1) {
                                try {
                                    LogUtils.getLogger().log(Level.WARNING, "The notify system was unable to play a sound for the 'loot_to_fish' category in '" + this.instruction.getObjective().getFullID() + "'. Error was: '" + e1.getMessage() + "'");
                                } catch (InstructionParseException e2) {
                                    LogUtils.logThrowableReport(e2);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static class FishData extends Objective.ObjectiveData {
        private int amount;

        public FishData(String instruction, String playerID, String objID) {
            super(instruction, playerID, objID);
            try {
                this.amount = Integer.parseInt(instruction);
            }
            catch (NumberFormatException e) {
                AdventureUtil.consoleMessage("[CustomFishing] NumberFormatException");
                this.amount = 1;
            }
        }

        public void catchFish(int caughtAmount) {
            this.amount -= caughtAmount;
            this.update();
        }

        public int getAmount() {
            return this.amount;
        }

        public String toString() {
            return String.valueOf(this.amount);
        }

        public boolean finished() {
            return this.amount <= 0;
        }
    }
}