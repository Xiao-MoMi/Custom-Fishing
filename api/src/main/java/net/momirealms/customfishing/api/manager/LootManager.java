package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.mechanic.loot.Loot;
import org.jetbrains.annotations.Nullable;

public interface LootManager {
    @Nullable Loot getLoot(String key);
}
