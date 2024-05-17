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

package net.momirealms.customfishing.bukkit.compatibility.quest;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.event.FishingResultEvent;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.Nullable;
import rocks.gravili.notquests.paper.NotQuests;
import rocks.gravili.notquests.paper.structs.ActiveObjective;
import rocks.gravili.notquests.paper.structs.ActiveQuest;
import rocks.gravili.notquests.paper.structs.QuestPlayer;
import rocks.gravili.notquests.paper.structs.objectives.Objective;

import java.util.Map;

public class NotQuestHook implements Listener {

    private final NotQuests notQuestsInstance;

    public NotQuestHook() {
        this.notQuestsInstance = NotQuests.getInstance();
    }

    @EventHandler
    public void onFish(FishingResultEvent event) {
        if (event.isCancelled() || event.getResult() == FishingResultEvent.Result.FAILURE)
            return;
        Loot loot = event.getLoot();
        Player player = event.getPlayer();
        final QuestPlayer questPlayer = notQuestsInstance.getQuestPlayerManager().getActiveQuestPlayer(player.getUniqueId());
        if (questPlayer != null) {
            if (questPlayer.getActiveQuests().size() > 0) {
                for (final ActiveQuest activeQuest : questPlayer.getActiveQuests()) {
                    for (final ActiveObjective activeObjective : activeQuest.getActiveObjectives()) {
                        if (activeObjective.getObjective() instanceof GroupObjective groupObjective) {
                            if (activeObjective.isUnlocked()) {
                                final String[] groups = loot.getLootGroup();
                                if (groups != null)
                                    for (String group : groups) {
                                        if (group.equals(groupObjective.getGroupToFish())) {
                                            activeObjective.addProgress(event.getAmount());
                                        }
                                    }
                            }
                        } else if (activeObjective.getObjective() instanceof LootObjective lootObjective) {
                            if (activeObjective.isUnlocked()) {
                                if (lootObjective.getLootID().equals(loot.getID()) || lootObjective.getLootID().equals("any")) {
                                    activeObjective.addProgress(event.getAmount());
                                }
                            }
                        }
                    }
                    activeQuest.removeCompletedObjectives(true);
                }
                questPlayer.removeCompletedQuests();
            }
        }
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, BukkitCustomFishingPlugin.get());
        notQuestsInstance.getObjectiveManager().registerObjective("CustomFishingGroup", GroupObjective.class);
        notQuestsInstance.getObjectiveManager().registerObjective("CustomFishingGroup", GroupObjective.class);
    }

    public static class GroupObjective extends Objective {

        private String group;

        public GroupObjective(NotQuests main) {
            super(main);
        }

        @Override
        protected String getTaskDescriptionInternal(QuestPlayer questPlayer, @Nullable ActiveObjective activeObjective) {
            return main.getLanguageManager()
                    .getString(
                            "chat.objectives.taskDescription.customfishingGroup.base",
                            questPlayer,
                            activeObjective,
                            Map.of("%CUSTOMFISHINGGROUP%", getGroupToFish()));
        }

        @Override
        public void save(FileConfiguration fileConfiguration, String initialPath) {
            fileConfiguration.set(initialPath + ".specifics.group", getGroupToFish());
        }

        @Override
        public void load(FileConfiguration fileConfiguration, String initialPath) {
            group = fileConfiguration.getString(initialPath + ".specifics.group");
        }

        @Override
        public void onObjectiveUnlock(ActiveObjective activeObjective, boolean b) {
        }

        @Override
        public void onObjectiveCompleteOrLock(ActiveObjective activeObjective, boolean b, boolean b1) {
        }

        public String getGroupToFish() {
            return group;
        }
    }

    public static class LootObjective extends Objective {

        private String loot;

        public LootObjective(NotQuests main) {
            super(main);
        }

        @Override
        protected String getTaskDescriptionInternal(QuestPlayer questPlayer, @Nullable ActiveObjective activeObjective) {
            String toReturn;
            if (!getLootID().isBlank() && !getLootID().equals("any")) {
                toReturn =
                        main.getLanguageManager()
                                .getString(
                                        "chat.objectives.taskDescription.customfishingLoot.base",
                                        questPlayer,
                                        activeObjective,
                                        Map.of("%CUSTOMFISHINGLOOT%", getLootID()));
            } else {
                toReturn =
                        main.getLanguageManager()
                                .getString(
                                        "chat.objectives.taskDescription.customfishingLoot.any",
                                        questPlayer,
                                        activeObjective);
            }
            return toReturn;
        }

        @Override
        public void save(FileConfiguration fileConfiguration, String initialPath) {
            fileConfiguration.set(initialPath + ".specifics.id", getLootID());
        }

        @Override
        public void load(FileConfiguration fileConfiguration, String initialPath) {
            loot = fileConfiguration.getString(initialPath + ".specifics.id");
        }

        @Override
        public void onObjectiveUnlock(ActiveObjective activeObjective, boolean b) {
        }

        @Override
        public void onObjectiveCompleteOrLock(ActiveObjective activeObjective, boolean b, boolean b1) {
        }

        public String getLootID() {
            return loot;
        }
    }
}
