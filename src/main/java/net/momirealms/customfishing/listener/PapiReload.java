package net.momirealms.customfishing.listener;

import net.momirealms.customfishing.CustomFishing;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PapiReload implements Listener {

    @EventHandler
    public void onReload(me.clip.placeholderapi.events.ExpansionUnregisterEvent event){
        if (CustomFishing.placeholders != null){
            if (event.getExpansion().equals(CustomFishing.placeholders)){
                CustomFishing.placeholders.register();
            }
        }
    }
}
