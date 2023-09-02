package net.momirealms.customfishing.api.manager;

import java.util.concurrent.CompletableFuture;

public interface VersionManager {

    boolean isVersionNewerThan1_19_R2();

    CompletableFuture<Boolean> checkUpdate();

    boolean isVersionNewerThan1_20();

    boolean isSpigot();

    public boolean isFolia();

    String getPluginVersion();

    String getServerVersion();
}
