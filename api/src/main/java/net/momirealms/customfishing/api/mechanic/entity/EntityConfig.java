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

import java.util.Map;

public class EntityConfig implements EntitySettings {

    private String entity;
    private double horizontalVector;
    private double verticalVector;
    private Map<String, Object> propertyMap;
    private boolean persist;

    @Override
    public boolean isPersist() {
        return persist;
    }

    @Override
    public double getHorizontalVector() {
        return horizontalVector;
    }

    @Override
    public double getVerticalVector() {
        return verticalVector;
    }

    @Override
    public String getEntityID() {
        return entity;
    }

    @Override
    public Map<String, Object> getPropertyMap() {
        return propertyMap;
    }

    public static class Builder {

        private final EntityConfig config;

        public Builder() {
            this.config = new EntityConfig();
        }

        public Builder entityID(String value) {
            this.config.entity = value;
            return this;
        }

        public Builder persist(boolean value) {
            this.config.persist = value;
            return this;
        }

        public Builder verticalVector(double value) {
            this.config.verticalVector = value;
            return this;
        }

        public Builder horizontalVector(double value) {
            this.config.horizontalVector = value;
            return this;
        }

        public Builder propertyMap(Map<String, Object> value) {
            this.config.propertyMap = value;
            return this;
        }

        public EntityConfig build() {
            return config;
        }
    }
}
