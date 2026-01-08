package net.momirealms.customfishing.bukkit.integration.quest.bq.common;

import net.momirealms.customfishing.api.event.TotemActivateEvent;
import org.betonquest.betonquest.api.CountingObjective;
import org.betonquest.betonquest.api.QuestException;
import org.betonquest.betonquest.api.instruction.Argument;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.objective.event.ObjectiveFactoryService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class TotemActivateObjective extends CountingObjective implements Listener {

    private final Argument<List<String>> identifiers;

    public TotemActivateObjective(
            final ObjectiveFactoryService service,
            final Argument<Number> targetAmount,
            final Argument<List<String>> identifiers
    ) throws QuestException {
        super(service, targetAmount, "customfishing.totem_activated");
        this.identifiers = identifiers;
    }

    @EventHandler
    public void onTotem(TotemActivateEvent event) throws QuestException {
        OnlineProfile profile = profileProvider.getProfile(event.getPlayer());
        if (!containsPlayer(profile) || !checkConditions(profile)) {
            return;
        }

        if (this.identifiers.getValue(profile).contains(event.getConfig().id())) {
            getCountingData(profile).progress();
            completeIfDoneOrNotify(profile);
        }
    }
}
