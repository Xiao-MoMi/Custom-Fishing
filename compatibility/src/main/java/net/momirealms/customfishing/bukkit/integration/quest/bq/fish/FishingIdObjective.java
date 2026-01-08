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

public class FishingIdObjective extends CountingObjective implements Listener {

    private final Argument<List<String>> identifiers;

    public FishingIdObjective(
            final ObjectiveFactoryService service,
            final Argument<Number> targetAmount,
            final Argument<List<String>> identifiers
    ) throws QuestException {
        super(service, targetAmount, "customfishing.fish_fished");
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

        if (this.identifiers.getValue(profile).contains(event.getLoot().id())) {
            getCountingData(profile).progress();
            completeIfDoneOrNotify(profile);
        }
    }
}
