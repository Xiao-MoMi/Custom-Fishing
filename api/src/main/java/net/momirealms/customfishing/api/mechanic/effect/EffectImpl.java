package net.momirealms.customfishing.api.mechanic.effect;

import net.momirealms.customfishing.common.util.Pair;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class EffectImpl implements Effect {

    private final HashMap<EffectProperties<?>, Object> properties = new HashMap<>();
    private double multipleLootChance = 0;
    private double sizeAdder = 0;
    private double sizeMultiplier = 1;
    private double scoreAdder = 0;
    private double scoreMultiplier = 1;
    private double gameTimeAdder = 0;
    private double gameTimeMultiplier = 1;
    private double waitTimeAdder = 0;
    private double waitTimeMultiplier = 1;
    private double difficultyAdder = 0;
    private double difficultyMultiplier = 1;
    private final List<Pair<String, BiFunction<Player, Double, Double>>> weightOperations = new ArrayList<>();
    private final List<Pair<String, BiFunction<Player, Double, Double>>> weightOperationsIgnored = new ArrayList<>();

    @Override
    public Map<EffectProperties<?>, Object> properties() {
        return properties;
    }

    @Override
    public <C> EffectImpl arg(EffectProperties<C> key, C value) {
        properties.put(key, value);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C> C arg(EffectProperties<C> key) {
        return (C) properties.get(key);
    }

    @Override
    public double multipleLootChance() {
        return multipleLootChance;
    }

    @Override
    public Effect multipleLootChance(double multipleLootChance) {
        this.multipleLootChance = multipleLootChance;
        return this;
    }

    @Override
    public double sizeAdder() {
        return sizeAdder;
    }

    @Override
    public Effect sizeAdder(double sizeAdder) {
        this.sizeAdder = sizeAdder;
        return this;
    }

    @Override
    public double sizeMultiplier() {
        return sizeMultiplier;
    }

    @Override
    public Effect sizeMultiplier(double sizeMultiplier) {
        this.sizeMultiplier = sizeMultiplier;
        return this;
    }

    @Override
    public double scoreAdder() {
        return scoreAdder;
    }

    @Override
    public Effect scoreAdder(double scoreAdder) {
        this.scoreAdder = scoreAdder;
        return this;
    }

    @Override
    public double scoreMultiplier() {
        return scoreMultiplier;
    }

    @Override
    public Effect scoreMultiplier(double scoreMultiplier) {
        this.scoreMultiplier = scoreMultiplier;
        return this;
    }

    @Override
    public double waitTimeAdder() {
        return waitTimeAdder;
    }

    @Override
    public Effect waitTimeAdder(double waitTimeAdder) {
        this.waitTimeAdder = waitTimeAdder;
        return this;
    }

    @Override
    public double waitTimeMultiplier() {
        return waitTimeMultiplier;
    }

    @Override
    public Effect waitTimeMultiplier(double waitTimeMultiplier) {
        this.waitTimeMultiplier = waitTimeMultiplier;
        return this;
    }

    @Override
    public double gameTimeAdder() {
        return gameTimeAdder;
    }

    @Override
    public Effect gameTimeAdder(double gameTimeAdder) {
        this.gameTimeAdder = gameTimeAdder;
        return this;
    }

    @Override
    public double gameTimeMultiplier() {
        return gameTimeMultiplier;
    }

    @Override
    public Effect gameTimeMultiplier(double gameTimeMultiplier) {
        this.gameTimeMultiplier = gameTimeMultiplier;
        return this;
    }

    @Override
    public double difficultyAdder() {
        return difficultyAdder;
    }

    @Override
    public Effect difficultyAdder(double difficultyAdder) {
        this.difficultyAdder = difficultyAdder;
        return this;
    }

    @Override
    public double difficultyMultiplier() {
        return difficultyMultiplier;
    }

    @Override
    public Effect difficultyMultiplier(double difficultyMultiplier) {
        this.difficultyMultiplier = difficultyMultiplier;
        return this;
    }

    @Override
    public List<Pair<String, BiFunction<Player, Double, Double>>> weightOperations() {
        return weightOperations;
    }

    @Override
    public Effect weightOperations(List<Pair<String, BiFunction<Player, Double, Double>>> weightOperations) {
        this.weightOperations.addAll(weightOperations);
        return this;
    }

    @Override
    public List<Pair<String, BiFunction<Player, Double, Double>>> weightOperationsIgnored() {
        return weightOperationsIgnored;
    }

    @Override
    public Effect weightOperationsIgnored(List<Pair<String, BiFunction<Player, Double, Double>>> weightOperations) {
        this.weightOperationsIgnored.addAll(weightOperations);
        return this;
    }

    @Override
    public void combine(Effect another) {
        if (another == null) return;
        this.scoreMultiplier += (another.scoreMultiplier() -1);
        this.scoreAdder += another.scoreAdder();
        this.sizeMultiplier += (another.sizeMultiplier() -1);
        this.sizeAdder += another.sizeAdder();
        this.difficultyMultiplier += (another.difficultyMultiplier() -1);
        this.difficultyAdder += another.difficultyAdder();
        this.gameTimeMultiplier += (another.gameTimeMultiplier() - 1);
        this.gameTimeAdder += another.gameTimeAdder();
        this.waitTimeAdder += (another.waitTimeAdder() -1);
        this.waitTimeMultiplier += (another.waitTimeMultiplier() -1);
        this.multipleLootChance += another.multipleLootChance();
        this.weightOperations.addAll(another.weightOperations());
        this.weightOperationsIgnored.addAll(another.weightOperationsIgnored());
        this.properties.putAll(another.properties());
    }
}
