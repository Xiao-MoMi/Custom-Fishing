package net.momirealms.customfishing;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;

public class AdventureManager {

    public static void consoleMessage(String s) {
        Audience au = CustomFishing.adventure.sender(Bukkit.getConsoleSender());
        MiniMessage mm = MiniMessage.miniMessage();
        Component parsed = mm.deserialize(s);
        au.sendMessage(parsed);
    }

    public static void playerMessage(Player player, String s) {
        Audience au = CustomFishing.adventure.player(player);
        MiniMessage mm = MiniMessage.miniMessage();
        Component parsed = mm.deserialize(s);
        au.sendMessage(parsed);
    }

    public static void playerTitle(Player player, String s1, String s2, int in, int duration, int out) {
        Audience au = CustomFishing.adventure.player(player);
        MiniMessage mm = MiniMessage.miniMessage();
        Title.Times times = Title.Times.times(Duration.ofMillis(in), Duration.ofMillis(duration), Duration.ofMillis(out));
        Title title = Title.title(mm.deserialize(s1), mm.deserialize(s2), times);
        au.showTitle(title);
    }
}
