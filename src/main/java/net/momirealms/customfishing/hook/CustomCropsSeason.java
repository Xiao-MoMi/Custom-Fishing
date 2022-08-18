package net.momirealms.customfishing.hook;

import net.momirealms.customcrops.api.CustomCropsAPI;
import org.bukkit.World;

public class CustomCropsSeason {

    public static String getSeason(World world){
        return CustomCropsAPI.getSeason(world.getName());
    }
}
