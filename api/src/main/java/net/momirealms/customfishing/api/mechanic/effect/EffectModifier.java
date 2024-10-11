package net.momirealms.customfishing.api.mechanic.effect;

import net.momirealms.customfishing.api.event.FishingEffectApplyEvent;
import net.momirealms.customfishing.api.mechanic.MechanicType;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.common.util.TriConsumer;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * EffectModifier interface for modifying effects.
 * This interface allows defining conditions and modifications for effects applied to players.
 */
public interface EffectModifier {

    /**
     * Gets the ID
     *
     * @return the ID.
     */
    String id();

    /**
     * Returns an array of requirements that must be met by a Player for the effect to be applied.
     *
     * @return an array of requirements
     */
    Requirement<Player>[] requirements();

    /**
     * Returns a list of modifiers that apply changes to an effect within a given context.
     *
     * @return a list of effect modifiers
     */
    List<TriConsumer<Effect, Context<Player>, Integer>> modifiers();

    /**
     * Creates and returns a new Builder instance for constructing EffectModifier instances.
     *
     * @return a new Builder instance
     */
    static Builder builder() {
        return new EffectModifierImpl.BuilderImpl();
    }

    /**
     * Get the type of the mechanic
     *
     * @return the type of the mechanic
     */
    MechanicType type();

    /**
     * Applies the effect modifier to the effect
     *
     * @param effect effect
     * @param stage stage
     * @param context context
     */
    default void apply(Effect effect, FishingEffectApplyEvent.Stage stage, Context<Player> context) {
        for (TriConsumer<Effect, Context<Player>, Integer> consumer : modifiers()) {
            consumer.accept(effect, context, stage.getId());
        }
    }

    /**
     * Builder interface for constructing EffectModifier instances.
     */
    interface Builder {

        /**
         * Sets the ID
         *
         * @return the current Builder instance
         */
        Builder id(String id);

        /**
         * Sets the requirements for the EffectModifier being built.
         *
         * @param requirements a list of requirements
         * @return the current Builder instance
         */
        Builder requirements(List<Requirement<Player>> requirements);

        /**
         * Sets the modifiers for the EffectModifier being built.
         *
         * @param modifiers a list of effect modifiers
         * @return the current Builder instance
         */
        Builder modifiers(List<TriConsumer<Effect, Context<Player>, Integer>> modifiers);

        /**
         * Set the type of the item
         *
         * @param type type
         * @return the Builder instance.
         */
        Builder type(MechanicType type);

        /**
         * Builds and returns the EffectModifier instance.
         *
         * @return the built EffectModifier instance
         */
        EffectModifier build();
    }
}
