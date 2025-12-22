package net.momirealms.customfishing.bukkit.integration.quest.bq;

import net.momirealms.customfishing.api.event.FishingResultEvent;
import org.betonquest.betonquest.api.CountingObjective;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Instruction;
import org.betonquest.betonquest.api.instruction.variable.Variable;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class FishingGroupObjective extends CountingObjective implements Listener {

    private final Variable<List<String>> identifiers;

    public FishingGroupObjective(final Instruction instruction, final Variable<Number> targetAmount,
                                 final Variable<List<String>> identifiers) throws QuestException {
        super(instruction, targetAmount, "custom_fish_group_to_fish");
        this.identifiers = identifiers;
    }

    @EventHandler
    public void onFish(FishingResultEvent event) throws QuestException {
        if (event.getResult() != FishingResultEvent.Result.FAILURE) {
            OnlineProfile profile = profileProvider.getProfile(event.getPlayer());
            if (!containsPlayer(profile)) {
                return;
            }
            if (!checkConditions(profile)) {
                return;
            }
            String[] groups = event.getLoot().lootGroup();
            if (groups != null) {
                for (String group : groups) {
                    if (this.identifiers.getValue(profile).contains(group)) {
                        getCountingData(profile).progress();
                        completeIfDoneOrNotify(profile);
                        return;
                    }
                }
            }
        }
    }
}
