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

package net.momirealms.customfishing.api.mechanic.entity;

import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class EntityConfigImpl implements EntityConfig {

    private final String id;
    private final String entityID;
    private final MathValue<Player> horizontalVector;
    private final MathValue<Player> verticalVector;
    private final Map<String, Object> propertyMap;

    public EntityConfigImpl(String id, String entityID, MathValue<Player> horizontalVector, MathValue<Player> verticalVector, Map<String, Object> propertyMap) {
        this.id = id;
        this.entityID = entityID;
        this.horizontalVector = horizontalVector;
        this.verticalVector = verticalVector;
        this.propertyMap = propertyMap;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public MathValue<Player> horizontalVector() {
        return horizontalVector;
    }

    @Override
    public MathValue<Player> verticalVector() {
        return verticalVector;
    }

    @NotNull
    @Override
    public String entityID() {
        return entityID;
    }

    @NotNull
    @Override
    public Map<String, Object> propertyMap() {
        return propertyMap;
    }

    public static class BuilderImpl implements Builder {
        private String entity = DEFAULT_ENTITY_ID;
        private MathValue<Player> horizontalVector = DEFAULT_HORIZONTAL_VECTOR;
        private MathValue<Player> verticalVector = DEFAULT_VERTICAL_VECTOR;
        private Map<String, Object> propertyMap = DEFAULT_PROPERTY_MAP;
        private String id;
        @Override
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        @Override
        public BuilderImpl entityID(String value) {
            this.entity = value;
            return this;
        }
        @Override
        public BuilderImpl verticalVector(MathValue<Player> value) {
            this.verticalVector = value;
            return this;
        }
        @Override
        public BuilderImpl horizontalVector(MathValue<Player> value) {
            this.horizontalVector = value;
            return this;
        }
        @Override
        public BuilderImpl propertyMap(Map<String, Object> value) {
            this.propertyMap = value;
            return this;
        }
        @Override
        public EntityConfigImpl build() {
            return new EntityConfigImpl(id, entity, horizontalVector, verticalVector, propertyMap);
        }
    }
}
