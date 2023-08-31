package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.mechanic.competition.CompetitionConfig;
import net.momirealms.customfishing.api.mechanic.competition.FishingCompetition;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface CompetitionManager {
    Set<String> getAllCompetitions();

    void startCompetition(String competition, boolean force, boolean allServers);

    @Nullable
    FishingCompetition getOnGoingCompetition();

    void startCompetition(CompetitionConfig config, boolean force, boolean allServers);

    int getNextCompetitionSeconds();

    CompletableFuture<Integer> getPlayerCount();

    @Nullable
    CompetitionConfig getConfig(String key);
}
