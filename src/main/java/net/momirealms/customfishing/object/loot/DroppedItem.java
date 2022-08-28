package net.momirealms.customfishing.object.loot;

import net.momirealms.customfishing.object.LeveledEnchantment;

import java.util.List;

public class DroppedItem extends Loot{

    boolean randomDurability;
    List<LeveledEnchantment> randomEnchants;
    String type;
    String id;

    public DroppedItem(String key) {
        super(key);
    }

    public boolean isRandomDurability() {
        return randomDurability;
    }

    public void setRandomDurability(boolean randomDurability) {
        this.randomDurability = randomDurability;
    }

    public List<LeveledEnchantment> getRandomEnchants() {
        return randomEnchants;
    }

    public void setRandomEnchants(List<LeveledEnchantment> randomEnchants) {
        this.randomEnchants = randomEnchants;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
