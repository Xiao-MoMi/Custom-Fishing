/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.object.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdventureUtils {

    /**
     * Get component from text
     * @param text text
     * @return component
     */
    public static Component getComponentFromMiniMessage(String text) {
        return MiniMessage.miniMessage().deserialize(replaceLegacy(text));
    }

    /**
     * Send a message to a command sender
     * @param sender sender
     * @param s message
     */
    public static void sendMessage(CommandSender sender, String s) {
        if (s == null) return;
        if (sender instanceof Player player) playerMessage(player, s);
        else consoleMessage(s);
    }

    /**
     * Send a message to console
     * @param s message
     */
    public static void consoleMessage(String s) {
        if (s == null) return;
        Audience au = CustomFishing.getAdventure().sender(Bukkit.getConsoleSender());
        au.sendMessage(getComponentFromMiniMessage(s));
    }

    /**
     * Send a message to a player
     * @param player player
     * @param s message
     */
    public static void playerMessage(Player player, String s) {
        if (s == null) return;
        Audience au = CustomFishing.getAdventure().player(player);
        au.sendMessage(getComponentFromMiniMessage(s));
    }

    /**
     * Send a title to a player
     * @param player player
     * @param s1 title
     * @param s2 subtitle
     * @param in in (ms)
     * @param duration duration (ms)
     * @param out out (ms)
     */
    public static void playerTitle(Player player, String s1, String s2, int in, int duration, int out) {
        Audience au = CustomFishing.getAdventure().player(player);
        Title.Times times = Title.Times.times(Duration.ofMillis(in), Duration.ofMillis(duration), Duration.ofMillis(out));
        Title title = Title.title(getComponentFromMiniMessage(s1), getComponentFromMiniMessage(s2), times);
        au.showTitle(title);
    }

    /**
     * Send a title to a player
     * @param player player
     * @param s1 title
     * @param s2 subtitle
     * @param in in (ms)
     * @param duration duration (ms)
     * @param out out (ms)
     */
    public static void playerTitle(Player player, Component s1, Component s2, int in, int duration, int out) {
        Audience au = CustomFishing.getAdventure().player(player);
        Title.Times times = Title.Times.times(Duration.ofMillis(in), Duration.ofMillis(duration), Duration.ofMillis(out));
        Title title = Title.title(s1, s2, times);
        au.showTitle(title);
    }

    /**
     * Send an actionbar to a player
     * @param player player
     * @param s actionbar
     */
    public static void playerActionbar(Player player, String s) {
        Audience au = CustomFishing.getAdventure().player(player);
        au.sendActionBar(getComponentFromMiniMessage(s));
    }

    /**
     * Play a sound to a player
     * @param player player
     * @param source sound source
     * @param key sound key
     * @param volume volume
     * @param pitch pitch
     */
    public static void playerSound(Player player, Sound.Source source, Key key, float volume, float pitch) {
        Sound sound = Sound.sound(key, source, volume, pitch);
        Audience au = CustomFishing.getAdventure().player(player);
        au.playSound(sound);
    }

    public static String replaceLegacy(String legacy) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = legacy.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (isColorCode(chars[i])) {
                if (i + 1 < chars.length) {
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
                        case 'r' -> stringBuilder.append("<reset><!italic>");
                        case 'l' -> stringBuilder.append("<bold>");
                        case 'm' -> stringBuilder.append("<strikethrough>");
                        case 'o' -> stringBuilder.append("<italic>");
                        case 'n' -> stringBuilder.append("<underlined>");
                        case 'k' -> stringBuilder.append("<obfuscated>");
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
                } else {
                    stringBuilder.append(chars[i]);
                }
            }
            else {
                stringBuilder.append(chars[i]);
            }
        }
        return stringBuilder.toString();
    }

    private static boolean isColorCode(char c) {
        return c == '§' || c == '&';
    }

    public static String replaceMiniMessage(String str) {
        String result = str.replace("&","§");
        List<String> miniFormat = new ArrayList<>();
        Pattern pattern = Pattern.compile("<.*?>");
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) miniFormat.add(matcher.group());
        for (String mini : miniFormat) {
            StringBuilder replacer = new StringBuilder();
            switch (mini) {
                case "<black>" -> replacer = new StringBuilder("§0");
                case "<dark_blue>" -> replacer = new StringBuilder("§1");
                case "<dark_green>" -> replacer = new StringBuilder("§2");
                case "<dark_aqua>" -> replacer = new StringBuilder("§3");
                case "<dark_red>" -> replacer = new StringBuilder("§4");
                case "<dark_purple>" -> replacer = new StringBuilder("§5");
                case "<gold>" -> replacer = new StringBuilder("§6");
                case "<gray>" -> replacer = new StringBuilder("§7");
                case "<dark_gray>" -> replacer = new StringBuilder("§8");
                case "<blue>" -> replacer = new StringBuilder("§9");
                case "<green>" -> replacer = new StringBuilder("§a");
                case "<aqua>" -> replacer = new StringBuilder("§b");
                case "<red>" -> replacer = new StringBuilder("§c");
                case "<light_purple>" -> replacer = new StringBuilder("§d");
                case "<yellow>" -> replacer = new StringBuilder("§e");
                case "<white>" -> replacer = new StringBuilder("§f");
                case "<reset>" -> replacer = new StringBuilder("§r");
                case "<bold>" -> replacer = new StringBuilder("§l");
                case "<strikethrough>" -> replacer = new StringBuilder("§m");
                case "<italic>" -> replacer = new StringBuilder("§o");
                case "<underlined>" -> replacer = new StringBuilder("§n");
                case "<obfuscated>" -> replacer = new StringBuilder("§k");
                default -> {
                    if (mini.length() == 9 && mini.charAt(1) == '#') {
                        replacer = new StringBuilder("§x");
                        for (int i = 2; i < 8; i++) {
                            replacer.append("§").append(mini.charAt(i));
                        }
                    }
                }
            }
            result = result.replace(mini, replacer.toString());
        }
        return result;
    }

    public static Object getPaperComponent(Component component) {
        try {
            Object newComponent;
            if (component instanceof TextComponent textComponent) {
                Method textComponentMethod = Reflection.componentClass.getMethod("text", String.class);
                newComponent = textComponentMethod.invoke(null, textComponent.content());
                TextColor textColor = textComponent.color();
                if (textColor != null) {
                    String hex = textColor.asHexString();
                    Method setColorMethod = Reflection.textComponentClass.getMethod("color", Reflection.textColorClass);
                    Method getColorFromHex = Reflection.textColorClass.getMethod("fromHexString", String.class);
                    Object hexColor = getColorFromHex.invoke(null, hex);
                    newComponent = setColorMethod.invoke(newComponent, hexColor);
                }
                Key fontKey = textComponent.font();
                if (fontKey != null) {
                    String namespacedKey = fontKey.asString();
                    Method setKeyMethod = Reflection.textComponentClass.getMethod("font", Reflection.keyClass);
                    Method keyMethod = Reflection.keyClass.getMethod("key", String.class);
                    Object key = keyMethod.invoke(null, namespacedKey);
                    newComponent = setKeyMethod.invoke(newComponent, key);
                }
                for (Map.Entry<TextDecoration, TextDecoration.State> entry : textComponent.decorations().entrySet()) {
                    String dec = entry.getKey().name();
                    Method getTextDecoration = Reflection.textDecorationClass.getDeclaredMethod("valueOf", String.class);
                    Object textDecoration = getTextDecoration.invoke(null, dec);
                    String stat = entry.getValue().name();
                    Method getState = Reflection.textDecorationStateClass.getDeclaredMethod("valueOf", String.class);
                    Object state = getState.invoke(null, stat);
                    Method applyDecorationMethod = Reflection.textComponentClass.getMethod("decoration", Reflection.textDecorationClass, Reflection.textDecorationStateClass);
                    newComponent = applyDecorationMethod.invoke(newComponent, textDecoration, state);
                }
                newComponent = setChildrenComponents(textComponent, newComponent);
            } else {
                Method textComponentMethod = Reflection.componentClass.getMethod("text", String.class);
                newComponent = textComponentMethod.invoke(null, "");
                newComponent = setChildrenComponents(component, newComponent);
            }
            return newComponent;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private static Object setChildrenComponents(Component component, Object newComponent) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        List<Object> children = new ArrayList<>();
        for (Component child : component.children()) {
            children.add(getPaperComponent(child));
        }
        if (children.size() != 0) {
            Method childrenMethod = Reflection.componentClass.getMethod("children", List.class);
            newComponent = childrenMethod.invoke(newComponent, children);
        }
        return newComponent;
    }
}
