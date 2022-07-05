package net.momirealms.customfishing.requirements;

import net.momirealms.customfishing.AdventureManager;

import java.util.List;

public record World(List<String> worlds) implements Requirement {

    public List<String> getWorlds() {
        return this.worlds;
    }

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
        org.bukkit.World world = fishingCondition.getLocation().getWorld();
        if (world != null) {
            return worlds.contains(world.getName());
        }
        AdventureManager.consoleMessage("<red>[CustomFishing] 这条消息不应该出现,玩家钓鱼时所处的世界并不存在!</red>");
        return false;
    }
}