package net.momirealms.customfishing.requirements;

import java.util.List;

public record Biome(List<String> biomes) implements Requirement {

    public List<String> getBiomes() {
        return this.biomes;
    }

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
        String currentBiome = fishingCondition.getLocation().getBlock().getBiome().getKey().toString();
        for (String biome : biomes) {
            if (currentBiome.equalsIgnoreCase(biome)) {
                return true;
            }
        }
        return false;
    }
}
