package net.momirealms.customfishing.api.mechanic.mob;

import java.util.Map;

public class MobConfig implements MobSettings {

    private String mob;
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
    public String getMobID() {
        return mob;
    }

    @Override
    public Map<String, Object> getPropertyMap() {
        return propertyMap;
    }

    public static class Builder {

        private final MobConfig config;

        public Builder() {
            this.config = new MobConfig();
        }

        public Builder mobID(String value) {
            this.config.mob = value;
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

        public MobConfig build() {
            return config;
        }
    }
}
