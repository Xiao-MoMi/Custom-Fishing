package net.momirealms.customfishing.api.mechanic.effect;

import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import org.bukkit.entity.Player;

import java.util.Map;

public class BaseEffect {

    private final MathValue<Player> waitTime;
    private final MathValue<Player> waitTimeMultiplier;
    private final MathValue<Player> difficulty;
    private final MathValue<Player> difficultyMultiplier;
    private final MathValue<Player> gameTime;
    private final MathValue<Player> gameTimeMultiplier;

    public BaseEffect(MathValue<Player> waitTime, MathValue<Player> waitTimeMultiplier, MathValue<Player> difficulty, MathValue difficultyMultiplier, MathValue gameTime, MathValue gameTimeMultiplier) {
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
