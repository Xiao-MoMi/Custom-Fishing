package net.momirealms.customfishing.api.mechanic.effect;

import net.momirealms.customfishing.api.mechanic.misc.Value;
import org.bukkit.entity.Player;

import java.util.Map;

public class BaseEffect {

    private final Value waitTime;
    private final Value waitTimeMultiplier;
    private final Value difficulty;
    private final Value difficultyMultiplier;
    private final Value gameTime;
    private final Value gameTimeMultiplier;

    public BaseEffect(Value waitTime, Value waitTimeMultiplier, Value difficulty, Value difficultyMultiplier, Value gameTime, Value gameTimeMultiplier) {
        this.waitTime = waitTime;
        this.waitTimeMultiplier = waitTimeMultiplier;
        this.difficulty = difficulty;
        this.difficultyMultiplier = difficultyMultiplier;
        this.gameTime = gameTime;
        this.gameTimeMultiplier = gameTimeMultiplier;
    }

    public Effect build(Player player, Map<String, String> values) {
        return new FishingEffect(
                waitTime.get(player, values),
                waitTimeMultiplier.get(player, values),
                difficulty.get(player, values),
                difficultyMultiplier.get(player, values),
                gameTime.get(player, values),
                gameTimeMultiplier.get(player, values)
        );
    }
}
