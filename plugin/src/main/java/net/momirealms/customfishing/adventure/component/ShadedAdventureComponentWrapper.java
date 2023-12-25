/*
 * This file is part of InvUI, licensed under the MIT License.
 *
 * Copyright (c) 2021 NichtStudioCode
 */

package net.momirealms.customfishing.adventure.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.inventoryaccess.component.ComponentWrapper;

public class ShadedAdventureComponentWrapper implements ComponentWrapper {

    public static final ShadedAdventureComponentWrapper EMPTY = new ShadedAdventureComponentWrapper(Component.empty());

    private final Component component;

    public ShadedAdventureComponentWrapper(Component component) {
        this.component = component;
    }

    @Override
    public @NotNull String serializeToJson() {
        return GsonComponentSerializer.gson().serialize(component);
    }

    @Override
    public @NotNull ComponentWrapper localized(@NotNull String lang) {
        if (!Languages.getInstance().doesServerSideTranslations())
            return this;

        return new ShadedAdventureComponentWrapper(ShadedAdventureShadedComponentLocalizer.getInstance().localize(lang, component));
    }

    @Override
    public @NotNull ComponentWrapper withoutPreFormatting() {
        return new ShadedAdventureComponentWrapper(ShadedAdventureComponentUtils.withoutPreFormatting(component));
    }

    @Override
    public @NotNull ShadedAdventureComponentWrapper clone() {
        try {
            return (ShadedAdventureComponentWrapper) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
