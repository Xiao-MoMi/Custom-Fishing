package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.integration.EnchantmentInterface;
import net.momirealms.customfishing.api.integration.LevelInterface;
import net.momirealms.customfishing.api.integration.SeasonInterface;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface IntegrationManager {

    boolean registerLevelPlugin(String plugin, LevelInterface level);

    boolean unregisterLevelPlugin(String plugin);

    boolean registerEnchantment(String plugin, EnchantmentInterface enchantment);

    boolean unregisterEnchantment(String plugin);

    LevelInterface getLevelHook(String plugin);

    List<String> getEnchantments(ItemStack rod);

    SeasonInterface getSeasonInterface();

    void setSeasonInterface(SeasonInterface season);
}
