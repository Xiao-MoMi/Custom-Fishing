package net.momirealms.customfishing.object.loot;

import net.momirealms.customfishing.object.fishing.Difficulty;
import org.jetbrains.annotations.Nullable;

public class DroppedItem extends Loot{

    private boolean randomDurability;
    private LeveledEnchantment[] randomEnchants;
    private final String material;

    public DroppedItem(String key, Difficulty difficulty, int time, int weight, String material) {
        super(key, difficulty, time, weight);
        this.material = material;
    }

    public boolean isRandomDurability() {
        return randomDurability;
    }

    public void setRandomDurability(boolean randomDurability) {
        this.randomDurability = randomDurability;
    }

    @Nullable
    public LeveledEnchantment[] getRandomEnchants() {
        return randomEnchants;
    }

    public void setRandomEnchants(LeveledEnchantment[] randomEnchants) {
        this.randomEnchants = randomEnchants;
    }

    public String getMaterial() {
        return material;
    }
}
