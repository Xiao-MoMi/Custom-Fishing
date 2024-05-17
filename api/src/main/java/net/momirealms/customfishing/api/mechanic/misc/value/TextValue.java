package net.momirealms.customfishing.api.mechanic.misc.value;

import net.momirealms.customfishing.api.mechanic.context.Context;

import java.util.regex.Pattern;

/**
 * The TextValue interface represents a text value that can be rendered
 * within a specific context. This interface allows for the rendering of
 * placeholder-based or plain text values in the context of custom fishing mechanics.
 *
 * @param <T> the type of the holder object for the context
 */
public interface TextValue<T> {

    Pattern pattern = Pattern.compile("\\{[^{}]+}");

    /**
     * Renders the text value within the given context.
     *
     * @param context the context in which the text value is rendered
     * @return the rendered text as a String
     */
    String render(Context<T> context);

    /**
     * Creates a TextValue based on a placeholder text.
     * Placeholders can be dynamically replaced with context-specific values.
     *
     * @param text the placeholder text to render
     * @param <T> the type of the holder object for the context
     * @return a TextValue instance representing the given placeholder text
     */
    static <T> TextValue<T> placeholder(String text) {
        return new PlaceholderTextValueImpl<>(text);
    }

    /**
     * Creates a TextValue based on plain text.
     *
     * @param text the plain text to render
     * @param <T> the type of the holder object for the context
     * @return a TextValue instance representing the given plain text
     */
    static <T> TextValue<T> plain(String text) {
        return new PlainTextValueImpl<>(text);
    }

    /**
     * Automatically creates a TextValue based on the given argument.
     * If the argument contains placeholders (detected by a regex pattern),
     * a PlaceholderTextValueImpl instance is created. Otherwise, a PlainTextValueImpl
     * instance is created.
     *
     * @param arg the text to evaluate and create a TextValue from
     * @param <T> the type of the holder object for the context
     * @return a TextValue instance representing the given text, either as a placeholder or plain text
     */
    static <T> TextValue<T> auto(String arg) {
        if (pattern.matcher(arg).find())
            return placeholder(arg);
        else
            return plain(arg);
    }
}
