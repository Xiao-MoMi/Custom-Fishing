package net.momirealms.customfishing.integration.season;

import me.casperge.realisticseasons.api.SeasonsAPI;
import net.momirealms.customfishing.integration.SeasonInterface;
import org.bukkit.World;

public class RealisticSeasonsImpl implements SeasonInterface {

    @Override
    public String getSeason(World world) {
        return SeasonsAPI.getInstance().getSeason(world).toString();
    }
}
