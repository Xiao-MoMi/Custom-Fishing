/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.api.mechanic.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.bossbar.BossBar;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.competition.CompetitionConfig;
import net.momirealms.customfishing.api.mechanic.competition.CompetitionGoal;
import net.momirealms.customfishing.api.mechanic.competition.CompetitionSchedule;
import net.momirealms.customfishing.api.mechanic.competition.info.ActionBarConfig;
import net.momirealms.customfishing.api.mechanic.competition.info.BossBarConfig;
import net.momirealms.customfishing.common.util.Pair;
import org.bukkit.entity.Player;

import java.util.*;

public class CompetitionConfigParser {

    private final String id;
    private CompetitionConfig config;

    public CompetitionConfigParser(String id, Section section) {
        this.id = id;
        analyze(section);
    }

    private void analyze(Section section) {
        CompetitionConfig.Builder builder = CompetitionConfig.builder()
                .id(id)
                .goal(CompetitionGoal.index().value(section.getString("goal", "TOTAL_SCORE").toLowerCase(Locale.ENGLISH)))
                .minPlayers(section.getInt("min-players", 0))
                .duration(section.getInt("duration", 300))
                .rewards(getPrizeActions(section.getSection("rewards")))
                .joinRequirements(BukkitCustomFishingPlugin.getInstance().getRequirementManager().parseRequirements(section.getSection("participate-requirements"), false))
                .joinActions(BukkitCustomFishingPlugin.getInstance().getActionManager().parseActions(section.getSection("participate-actions")))
                .startActions(BukkitCustomFishingPlugin.getInstance().getActionManager().parseActions(section.getSection("start-actions")))
                .endActions(BukkitCustomFishingPlugin.getInstance().getActionManager().parseActions(section.getSection("end-actions")))
                .skipActions(BukkitCustomFishingPlugin.getInstance().getActionManager().parseActions(section.getSection("skip-actions")));;
        if (section.getBoolean("bossbar.enable", false)) {
            builder.bossBarConfig(
                    BossBarConfig.builder()
                            .enable(true)
                            .color(BossBar.Color.valueOf(section.getString("bossbar.color", "WHITE").toUpperCase(Locale.ENGLISH)))
                            .overlay(BossBar.Overlay.valueOf(section.getString("bossbar.overlay", "PROGRESS").toUpperCase(Locale.ENGLISH)))
                            .refreshRate(section.getInt("bossbar.refresh-rate", 20))
                            .switchInterval(section.getInt("bossbar.switch-interval", 200))
                            .showToAll(!section.getBoolean("bossbar.only-show-to-participants", true))
                            .text(section.getStringList("bossbar.text").toArray(new String[0]))
                            .build()
            );
        }
        if (section.getBoolean("actionbar.enable", false)) {
            builder.actionBarConfig(
                    ActionBarConfig.builder()
                            .enable(true)
                            .refreshRate(section.getInt("actionbar.refresh-rate", 5))
                            .switchInterval(section.getInt("actionbar.switch-interval", 200))
                            .showToAll(!section.getBoolean("actionbar.only-show-to-participants", true))
                            .text(section.getStringList("actionbar.text").toArray(new String[0]))
                            .build()
            );
        }

        List<Pair<Integer, Integer>> timePairs = section.getStringList("start-time")
                .stream().map(it -> {
                    String[] split = it.split(":");
                    return Pair.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                }).toList();
        List<Integer> weekdays = section.getIntList("start-weekday");
        if (weekdays.isEmpty()) {
            weekdays.addAll(List.of(1,2,3,4,5,6,7));
        }
        List<CompetitionSchedule> schedules = new ArrayList<>();
        for (Integer weekday : weekdays) {
            for (Pair<Integer, Integer> timePair : timePairs) {
                CompetitionSchedule schedule = new CompetitionSchedule(weekday, timePair.left(), timePair.right(), 0);
                schedules.add(schedule);
            }
        }
        builder.schedules(schedules);
        this.config = builder.build();
    }

    public CompetitionConfig getCompetition() {
        return config;
    }


    /**
     * Gets prize actions from a configuration section.
     *
     * @param section The configuration section containing prize actions.
     * @return A HashMap where keys are action names and values are arrays of Action objects.
     */
    public HashMap<String, Action<Player>[]> getPrizeActions(Section section) {
        HashMap<String, Action<Player>[]> map = new HashMap<>();
        if (section == null) return map;
        for (Map.Entry<String, Object> entry : section.getStringRouteMappedValues(false).entrySet()) {
            if (entry.getValue() instanceof Section innerSection) {
                map.put(entry.getKey(), BukkitCustomFishingPlugin.getInstance().getActionManager().parseActions(innerSection));
            }
        }
        return map;
    }
}
