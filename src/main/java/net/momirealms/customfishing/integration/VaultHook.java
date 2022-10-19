package net.momirealms.customfishing.integration;

import net.milkbowl.vault.economy.Economy;
import net.momirealms.customfishing.CustomFishing;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    public static Economy economy;

    public static boolean initialize() {
        RegisteredServiceProvider<Economy> rsp = CustomFishing.plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }
}
