package net.momirealms.customfishing.api.util;

public class FontUtils {

    public static String surroundWithFont(String text, String font) {
        return "<font:" + font + ">" + text + "</font>";
    }
}
