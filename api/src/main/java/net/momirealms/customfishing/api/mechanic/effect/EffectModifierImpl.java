package net.momirealms.customfishing.api.mechanic.effect;

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class EffectModifierImpl implements EffectModifier {

    private final Requirement<Player>[] requirements;
    private final List<BiConsumer<Effect, Context<Player>>> modifiers;
    private final String id;

    public EffectModifierImpl(String id, Requirement<Player>[] requirements, List<BiConsumer<Effect, Context<Player>>> modifiers) {
        this.requirements = requirements;
        this.modifiers = modifiers;
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Requirement<Player>[] requirements() {
        return requirements;
    }

    @Override
    public List<BiConsumer<Effect, Context<Player>>> modifiers() {
        return modifiers;
    }

    public static class BuilderImpl implements Builder {
        private final List<Requirement<Player>> requirements = new ArrayList<>();
        private final List<BiConsumer<Effect, Context<Player>>> modifiers = new ArrayList<>();
        private String id;
        @Override
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        @Override
        public Builder requirements(List<Requirement<Player>> requirements) {
            this.requirements.addAll(requirements);
            return this;
        }
        @Override
        public Builder modifiers(List<BiConsumer<Effect, Context<Player>>> modifiers) {
            this.modifiers.addAll(modifiers);
            return this;
        }
        @Override
        @SuppressWarnings("unchecked")
        public EffectModifier build() {
            return new EffectModifierImpl(id, this.requirements.toArray(new Requirement[0]), this.modifiers);
        }
    }
}
