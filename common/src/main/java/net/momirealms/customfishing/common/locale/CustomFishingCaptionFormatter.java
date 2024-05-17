package net.momirealms.customfishing.common.locale;

import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.minecraft.extras.caption.ComponentCaptionFormatter;

import java.util.List;

public class CustomFishingCaptionFormatter<C> implements ComponentCaptionFormatter<C> {

    @Override
    public @NonNull Component formatCaption(@NonNull Caption captionKey, @NonNull C recipient, @NonNull String caption, @NonNull List<@NonNull CaptionVariable> variables) {
        Component component = ComponentCaptionFormatter.translatable().formatCaption(captionKey, recipient, caption, variables);
        return TranslationManager.render(component);
    }
}
