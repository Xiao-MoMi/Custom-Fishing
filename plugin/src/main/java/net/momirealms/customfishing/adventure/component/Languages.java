/*
 * This file is part of InvUI, licensed under the MIT License.
 *
 * Copyright (c) 2021 NichtStudioCode
 */

package net.momirealms.customfishing.adventure.component;

import com.google.gson.stream.JsonReader;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class Languages {

    private static final Languages INSTANCE = new Languages();
    private final Map<String, Map<String, String>> translations = new HashMap<>();
    private Function<Player, Locale> languageProvider = Player::locale;
    private boolean serverSideTranslations = true;

    private Languages() {
    }

    public static Languages getInstance() {
        return INSTANCE;
    }

    public void addLanguage(@NotNull String lang, @NotNull Map<String, String> translations) {
        this.translations.put(lang, translations);
    }

    public void loadLanguage(@NotNull String lang, @NotNull Reader reader) throws IOException {
        var translations = new HashMap<String, String>();
        try (var jsonReader = new JsonReader(reader)) {
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                var key = jsonReader.nextName();
                var value = jsonReader.nextString();
                translations.put(key, value);
            }

            addLanguage(lang, translations);
        }
    }

    public void loadLanguage(@NotNull String lang, @NotNull File file, @NotNull Charset charset) throws IOException {
        try (var reader = new FileReader(file, charset)) {
            loadLanguage(lang, reader);
        }
    }

    public @Nullable String getFormatString(@NotNull String lang, @NotNull String key) {
        var map = translations.get(lang);
        if (map == null)
            return null;
        return map.get(key);
    }

    public void setLanguageProvider(@NotNull Function<Player, Locale> languageProvider) {
        this.languageProvider = languageProvider;
    }

    public @NotNull Locale getLanguage(@NotNull Player player) {
        return languageProvider.apply(player);
    }

    public void enableServerSideTranslations(boolean enable) {
        serverSideTranslations = enable;
    }

    public boolean doesServerSideTranslations() {
        return serverSideTranslations;
    }
}