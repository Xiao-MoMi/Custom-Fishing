package net.momirealms.customfishing.api.mechanic.game;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.util.WeightUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GameGroups implements GameConfig {

    private final List<Pair<String, Double>> gamesWithWeight;

    public GameGroups(List<Pair<String, Double>> gamesWithWeight) {
        this.gamesWithWeight = gamesWithWeight;
    }

    @Override
    public @Nullable Pair<Game, GameSettings> getRandomGame(Effect effect) {
        String group = WeightUtils.getRandom(gamesWithWeight);
        GameConfig gameConfig = CustomFishingPlugin.get().getGameManager().getGameConfig(group);
        if (gameConfig == null) {
            CustomFishingPlugin.get().getLogger().warning(String.format("Game config %s doesn't exist!", group));
            return null;
        }
        if (!(gameConfig instanceof GameGroup gameGroup)) {
            CustomFishingPlugin.get().getLogger().warning(String.format("%s is not a game group!", group));
            return null;
        }
        return gameGroup.getRandomGame(effect);
    }
}
