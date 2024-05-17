/*
 *  Copyright (C) <2022> <XiaoMoMi>
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

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class EntityConfigImpl implements EntityConfig {

    private final String id;
    private final double horizontalVector;
    private final double verticalVector;
    private final Map<String, Object> propertyMap;

    public EntityConfigImpl(String id, double horizontalVector, double verticalVector, Map<String, Object> propertyMap) {
        this.id = id;
        this.horizontalVector = horizontalVector;
        this.verticalVector = verticalVector;
        this.propertyMap = propertyMap;
    }

    @Override
    public double getHorizontalVector() {
        return horizontalVector;
    }

    @Override
    public double getVerticalVector() {
        return verticalVector;
    }

    @NotNull
    @Override
    public String getEntityID() {
        return id;
    }

    @NotNull
    @Override
    public Map<String, Object> getPropertyMap() {
        return propertyMap;
    }

    public static class BuilderImpl implements Builder {
        private String entity = DEFAULT_ENTITY_ID;
        private double horizontalVector = DEFAULT_HORIZONTAL_VECTOR;
        private double verticalVector = DEFAULT_VERTICAL_VECTOR;
        private Map<String, Object> propertyMap = DEFAULT_PROPERTY_MAP;
        @Override
        public BuilderImpl entityID(String value) {
            this.entity = value;
            return this;
        }
        @Override
        public BuilderImpl verticalVector(double value) {
            this.verticalVector = value;
            return this;
        }
        @Override
        public BuilderImpl horizontalVector(double value) {
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
            return new EntityConfigImpl(entity, horizontalVector, verticalVector, propertyMap);
        }
    }
}
