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

package net.momirealms.customfishing.api.mechanic.effect;

import net.momirealms.customfishing.api.mechanic.MechanicType;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.common.util.TriConsumer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EffectModifierImpl implements EffectModifier {

    private final Requirement<Player>[] requirements;
    private final List<TriConsumer<Effect, Context<Player>, Integer>> modifiers;
    private final String id;
    private final MechanicType type;

    public EffectModifierImpl(
            String id,
            MechanicType type,
            Requirement<Player>[] requirements,
            List<TriConsumer<Effect, Context<Player>, Integer>> modifiers
    ) {
        this.requirements = requirements;
        this.modifiers = modifiers;
        this.id = id;
        this.type = type;
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
    public List<TriConsumer<Effect, Context<Player>, Integer>> modifiers() {
        return modifiers;
    }

    @Override
    public MechanicType type() {
        return type;
    }

    public static class BuilderImpl implements Builder {
        private final List<Requirement<Player>> requirements = new ArrayList<>();
        private final List<TriConsumer<Effect, Context<Player>, Integer>> modifiers = new ArrayList<>();
        private String id;
        private MechanicType type;
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
        public Builder modifiers(List<TriConsumer<Effect, Context<Player>, Integer>> modifiers) {
            this.modifiers.addAll(modifiers);
            return this;
        }
        @Override
        public Builder type(MechanicType type) {
            this.type = type;
            return this;
        }
        @Override
        @SuppressWarnings("unchecked")
        public EffectModifier build() {
            return new EffectModifierImpl(id, type, this.requirements.toArray(new Requirement[0]), this.modifiers);
        }
    }
}
