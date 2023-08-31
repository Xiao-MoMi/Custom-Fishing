package net.momirealms.customfishing.adventure;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.manager.AdventureManager;
import net.momirealms.customfishing.api.util.ReflectionUtils;
import net.momirealms.customfishing.setting.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;

public class AdventureManagerImpl implements AdventureManager {

    private final BukkitAudiences adventure;
    private static AdventureManager instance;

    public AdventureManagerImpl(CustomFishingPlugin plugin) {
        this.adventure = BukkitAudiences.create(plugin);
        instance = this;
    }

    public static AdventureManager getInstance() {
        return instance;
    }

    public void close() {
        if (adventure != null)
            adventure.close();
    }

    @Override
    public Component getComponentFromMiniMessage(String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }

    @Override
    public void sendMessage(CommandSender sender, String s) {
        if (s == null) return;
        if (sender instanceof Player player) sendPlayerMessage(player, s);
        else if (sender instanceof ConsoleCommandSender) sendConsoleMessage(s);
    }

    @Override
    public void sendMessageWithPrefix(CommandSender sender, String s) {
        if (s == null) return;
        if (sender instanceof Player player) sendPlayerMessage(player, Locale.MSG_Prefix + s);
        else if (sender instanceof ConsoleCommandSender) sendConsoleMessage(Locale.MSG_Prefix + s);
    }

    @Override
    public void sendConsoleMessage(String s) {
        if (s == null) return;
        Audience au = adventure.sender(Bukkit.getConsoleSender());
        au.sendMessage(getComponentFromMiniMessage(s));
    }

    @Override
    public void sendPlayerMessage(Player player, String s) {
        if (s == null) return;
        Audience au = adventure.player(player);
        au.sendMessage(getComponentFromMiniMessage(s));
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int in, int duration, int out) {
        Audience au = adventure.player(player);
        Title.Times times = Title.Times.times(Duration.ofMillis(in), Duration.ofMillis(duration), Duration.ofMillis(out));
        au.showTitle(Title.title(getComponentFromMiniMessage(title), getComponentFromMiniMessage(subtitle), times));
    }

    @Override
    public void sendTitle(Player player, Component title, Component subtitle, int in, int duration, int out) {
        Audience au = adventure.player(player);
        Title.Times times = Title.Times.times(Duration.ofMillis(in), Duration.ofMillis(duration), Duration.ofMillis(out));
        au.showTitle(Title.title(title, subtitle, times));
    }

    @Override
    public void sendActionbar(Player player, String s) {
        Audience au = adventure.player(player);
        au.sendActionBar(getComponentFromMiniMessage(s));
    }

    @Override
    public void sendSound(Player player, Sound.Source source, Key key, float volume, float pitch) {
        Sound sound = Sound.sound(key, source, volume, pitch);
        Audience au = adventure.player(player);
        au.playSound(sound);
    }

    @Override
    public void sendSound(Player player, Sound sound) {
        Audience au = adventure.player(player);
        au.playSound(sound);
    }

    @Override
    public String legacyToMiniMessage(String legacy) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = legacy.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!isColorCode(chars[i])) {
                stringBuilder.append(chars[i]);
                continue;
            }
            if (i + 1 >= chars.length) {
                stringBuilder.append(chars[i]);
                continue;
            }
            switch (chars[i+1]) {
                case '0' -> stringBuilder.append("<black>");
                case '1' -> stringBuilder.append("<dark_blue>");
                case '2' -> stringBuilder.append("<dark_green>");
                case '3' -> stringBuilder.append("<dark_aqua>");
                case '4' -> stringBuilder.append("<dark_red>");
                case '5' -> stringBuilder.append("<dark_purple>");
                case '6' -> stringBuilder.append("<gold>");
                case '7' -> stringBuilder.append("<gray>");
                case '8' -> stringBuilder.append("<dark_gray>");
                case '9' -> stringBuilder.append("<blue>");
                case 'a' -> stringBuilder.append("<green>");
                case 'b' -> stringBuilder.append("<aqua>");
                case 'c' -> stringBuilder.append("<red>");
                case 'd' -> stringBuilder.append("<light_purple>");
                case 'e' -> stringBuilder.append("<yellow>");
                case 'f' -> stringBuilder.append("<white>");
                case 'r' -> stringBuilder.append("<r><!i>");
                case 'l' -> stringBuilder.append("<b>");
                case 'm' -> stringBuilder.append("<s>");
                case 'o' -> stringBuilder.append("<i>");
                case 'n' -> stringBuilder.append("<u>");
                case 'k' -> stringBuilder.append("<o>");
                case 'x' -> {
                    if (i + 13 >= chars.length
                            || !isColorCode(chars[i+2])
                            || !isColorCode(chars[i+4])
                            || !isColorCode(chars[i+6])
                            || !isColorCode(chars[i+8])
                            || !isColorCode(chars[i+10])
                            || !isColorCode(chars[i+12])) {
                        stringBuilder.append(chars[i]);
                        continue;
                    }
                    stringBuilder
                            .append("<#")
                            .append(chars[i+3])
                            .append(chars[i+5])
                            .append(chars[i+7])
                            .append(chars[i+9])
                            .append(chars[i+11])
                            .append(chars[i+13])
                            .append(">");
                    i += 13;
                }
                default -> {
                    stringBuilder.append(chars[i]);
                    continue;
                }
            }
            i++;
        }
        return stringBuilder.toString();
    }

    @Override
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isColorCode(char c) {
        return c == '§' || c == '&';
    }

    @Override
    public String componentToLegacy(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    @Override
    public String componentToJson(Component component) {
        return GsonComponentSerializer.gson().serialize(component);
    }

    @Override
    public Object shadedComponentToPaperComponent(Component component) {
        Object cp;
        try {
            cp = ReflectionUtils.gsonDeserializeMethod.invoke(ReflectionUtils.gsonInstance, GsonComponentSerializer.gson().serialize(component));
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
        return cp;
    }
}
