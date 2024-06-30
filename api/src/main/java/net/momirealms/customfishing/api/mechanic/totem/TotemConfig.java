package net.momirealms.customfishing.api.mechanic.totem;

import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.api.mechanic.totem.block.TotemBlock;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface TotemConfig {

    TotemModel[] totemModels();

    String id();

    boolean isRightPattern(Location location);

    TotemParticle[] particleSettings();

    MathValue<Player> radius();

    MathValue<Player> duration();

    TotemBlock[] totemCore();

    static Builder builder() {
        return new TotemConfigImpl.BuilderImpl();
    }

    interface Builder {

        Builder id(String id);

        Builder totemModels(TotemModel[] totemModels);

        Builder particleSettings(TotemParticle[] particleSettings);

        Builder radius(MathValue<Player> radius);

        Builder duration(MathValue<Player> duration);

        TotemConfig build();
    }
}
