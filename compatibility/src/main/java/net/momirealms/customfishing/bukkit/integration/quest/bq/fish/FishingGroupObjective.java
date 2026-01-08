package net.momirealms.customfishing.bukkit.integration.quest.bq.fish;

import net.momirealms.customfishing.api.event.FishingResultEvent;
import org.betonquest.betonquest.api.CountingObjective;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.objective.event.ObjectiveFactoryService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class FishingGroupObjective extends CountingObjective implements Listener {

    private final Argument<List<String>> identifiers;

    public FishingGroupObjective(
            final ObjectiveFactoryService service,
            final Argument<Number> targetAmount,
            final Argument<List<String>> identifiers
    ) throws QuestException {
        super(service, targetAmount, "customfishing.group_fished");
        this.identifiers = identifiers;
    }

    @EventHandler
    public void onFish(FishingResultEvent event) throws QuestException {
        if (event.getResult() == FishingResultEvent.Result.FAILURE) {
            return;
        }

        OnlineProfile profile = profileProvider.getProfile(event.getPlayer());
        if (!containsPlayer(profile) || !checkConditions(profile)) {
            return;
        }

        String[] groups = event.getLoot().lootGroup();
        if (groups == null || groups.length == 0) {
            return;
        }

        final List<String> requiredGroups = this.identifiers.getValue(profile);
        for (String group : groups) {
            if (requiredGroups.contains(group)) {
                getCountingData(profile).progress();
                completeIfDoneOrNotify(profile);
                return;
            }
        }
    }
}
