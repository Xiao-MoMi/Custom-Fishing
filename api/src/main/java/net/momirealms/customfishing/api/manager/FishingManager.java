package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.mechanic.TempFishingState;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.game.Game;
import net.momirealms.customfishing.api.mechanic.game.GameSettings;
import net.momirealms.customfishing.api.mechanic.game.GamingPlayer;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public interface FishingManager {
    boolean removeHook(UUID uuid);

    void setTempFishingState(Player player, TempFishingState tempFishingState);

    void removeHookCheckTask(Player player);

    Optional<FishHook> getHook(UUID uuid);

    void removeTempFishingState(Player player);

    void processGameResult(GamingPlayer gamingPlayer);

    void startFishingGame(Player player, Loot loot, Effect effect);

    void startFishingGame(Player player, GameSettings settings, Game game);
}
