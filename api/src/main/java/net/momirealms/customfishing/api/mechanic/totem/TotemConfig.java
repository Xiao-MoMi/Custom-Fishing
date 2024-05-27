package net.momirealms.customfishing.api.mechanic.totem;

import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.api.mechanic.totem.block.TotemBlock;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface TotemConfig
{
    TotemModel[] totemModels();

    Requirement<Player>[] activateRequirements();

    String id();

    boolean isRightPattern(Location location);

    TotemParticle[] particleSettings();

    double radius();

    int duration();

    TotemBlock[] totemCore();

    static Builder builder() {
        return new TotemConfigImpl.BuilderImpl();
    }

    interface Builder {

    }
}
