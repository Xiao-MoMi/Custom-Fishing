/*
 * This file is part of InvUI, licensed under the MIT License.
 *
 * Copyright (c) 2021 NichtStudioCode
 */

package net.momirealms.customfishing.adventure.component;

import net.kyori.adventure.text.*;

public class ShadedAdventureShadedComponentLocalizer extends ShadedComponentLocalizer<Component> {

    private static final ShadedAdventureShadedComponentLocalizer INSTANCE = new ShadedAdventureShadedComponentLocalizer();

    private ShadedAdventureShadedComponentLocalizer() {
        super(Component::text);
    }

    public static ShadedAdventureShadedComponentLocalizer getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Component localize(String lang, Component component) {
        if (!(component instanceof BuildableComponent))
            throw new IllegalStateException("Component is not a BuildableComponent");

        return localize(lang, (BuildableComponent) component);
    }

    @SuppressWarnings("NonExtendableApiUsage")
    private <C extends BuildableComponent<C, B>, B extends ComponentBuilder<C, B>> BuildableComponent<?, ?> localize(String lang, BuildableComponent<C, B> component) {
        ComponentBuilder<?, ?> builder;
        if (component instanceof TranslatableComponent) {
            builder = localizeTranslatable(lang, (TranslatableComponent) component).toBuilder();
        } else {
            builder = component.toBuilder();
        }

        builder.mapChildrenDeep(child -> {
            if (child instanceof TranslatableComponent)
                return localizeTranslatable(lang, (TranslatableComponent) child);
            return child;
        });

        return builder.build();
    }

    private BuildableComponent<?, ?> localizeTranslatable(String lang, TranslatableComponent component) {
        var formatString = Languages.getInstance().getFormatString(lang, component.key());
        if (formatString == null)
            return component;

        var children = decomposeFormatString(lang, formatString, component, component.args());
        return Component.textOfChildren(children.toArray(ComponentLike[]::new)).style(component.style());
    }
}
