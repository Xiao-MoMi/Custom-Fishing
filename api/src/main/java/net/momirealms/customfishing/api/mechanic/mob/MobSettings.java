package net.momirealms.customfishing.api.mechanic.mob;

import java.util.Map;

public interface MobSettings {
    boolean isPersist();

    double getHorizontalVector();

    double getVerticalVector();

    String getMobID();

    Map<String, Object> getPropertyMap();
}
