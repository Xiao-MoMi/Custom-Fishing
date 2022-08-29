package net.momirealms.customfishing.api;

import net.momirealms.customfishing.ConfigReader;
import net.momirealms.customfishing.object.loot.Loot;
import org.jetbrains.annotations.Nullable;

public class CustomFishingAPI {

    @Nullable
    public static Loot getLoot(String key){
        return ConfigReader.LOOT.get(key);
    }
}
