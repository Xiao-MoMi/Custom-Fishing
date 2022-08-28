package net.momirealms.customfishing.hook.season;

import net.momirealms.customcrops.api.CustomCropsAPI;
import org.bukkit.World;

public class CustomCropsSeason implements SeasonInterface{
    public String getSeason(World world){
        return CustomCropsAPI.getSeason(world.getName());
    }
}
