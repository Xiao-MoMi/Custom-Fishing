package net.momirealms.customfishing.integration.season;

import net.momirealms.customcrops.api.utils.SeasonUtils;
import net.momirealms.customfishing.integration.SeasonInterface;
import org.bukkit.World;

public class CustomCropsSeasonImpl implements SeasonInterface {
    @Override
    public String getSeason(World world) {
        return SeasonUtils.getSeason(world).name();
    }
}
