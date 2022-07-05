package net.momirealms.customfishing.requirements;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import java.util.List;

public record Region(List<String> regions) implements Requirement {

    public List<String> getRegions() {
        return this.regions;
    }

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(fishingCondition.getLocation()));
        for (ProtectedRegion protectedRegion : set) {
            if (regions.contains(protectedRegion.getId())) {
                return true;
            }
        }
        return false;
    }
}
