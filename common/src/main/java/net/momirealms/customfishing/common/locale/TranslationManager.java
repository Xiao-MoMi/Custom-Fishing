/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.common.locale;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.Translator;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.plugin.CustomFishingPlugin;
import net.momirealms.customfishing.common.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TranslationManager {

    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private static final List<String> locales = List.of("en", "zh_cn");
    private static TranslationManager instance;

    private final CustomFishingPlugin plugin;
    private final Set<Locale> installed = ConcurrentHashMap.newKeySet();
    private MiniMessageTranslationRegistry registry;
    private final Path translationsDirectory;

    public TranslationManager(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.translationsDirectory = this.plugin.getConfigDirectory().resolve("translations");
        instance = this;
    }

    public void reload() {
        // remove any previous registry
        if (this.registry != null) {
            MiniMessageTranslator.translator().removeSource(this.registry);
            this.installed.clear();
        }
        for (String lang : locales) {
            this.plugin.getConfigManager().saveResource("translations/" + lang + ".yml");
        }

        this.registry = MiniMessageTranslationRegistry.create(Key.key("customfishing", "main"), AdventureHelper.getMiniMessage());
        this.registry.defaultLocale(DEFAULT_LOCALE);
        this.loadFromFileSystem(this.translationsDirectory, false);
        MiniMessageTranslator.translator().addSource(this.registry);
    }

    public static String miniMessageTranslation(String key) {
        return miniMessageTranslation(key, null);
    }

    public static String miniMessageTranslation(String key, @Nullable Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
            if (locale == null) {
                locale = DEFAULT_LOCALE;
            }
        }
        return instance.registry.miniMessageTranslation(key, locale);
    }

    public static Component render(Component component) {
        return render(component, null);
    }

    public static Component render(Component component, @Nullable Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
            if (locale == null) {
                locale = DEFAULT_LOCALE;
            }
        }
        return MiniMessageTranslator.render(component, locale);
    }

    public void loadFromFileSystem(Path directory, boolean suppressDuplicatesError) {
        List<Path> translationFiles;
        try (Stream<Path> stream = Files.list(directory)) {
            translationFiles = stream.filter(TranslationManager::isTranslationFile).collect(Collectors.toList());
        } catch (IOException e) {
            translationFiles = Collections.emptyList();
        }

        if (translationFiles.isEmpty()) {
            return;
        }

        Map<Locale, Map<String, String>> loaded = new HashMap<>();
        for (Path translationFile : translationFiles) {
            try {
                Pair<Locale, Map<String, String>> result = loadTranslationFile(translationFile);
                loaded.put(result.left(), result.right());
            } catch (Exception e) {
                if (!suppressDuplicatesError || !isAdventureDuplicatesException(e)) {
                    this.plugin.getPluginLogger().warn("Error loading locale file: " + translationFile.getFileName(), e);
                }
            }
        }

        // try registering the locale without a country code - if we don't already have a registration for that
        loaded.forEach((locale, bundle) -> {
            Locale localeWithoutCountry = new Locale(locale.getLanguage());
            if (!locale.equals(localeWithoutCountry) && !localeWithoutCountry.equals(DEFAULT_LOCALE) && this.installed.add(localeWithoutCountry)) {
                try {
                    this.registry.registerAll(localeWithoutCountry, bundle);
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
        });

        Locale localLocale = Locale.getDefault();
        if (!this.installed.contains(localLocale)) {
            plugin.getPluginLogger().warn(localLocale.toString().toLowerCase(Locale.ENGLISH) + ".yml not exists, using en.yml as default locale.");
        }
    }

    public static boolean isTranslationFile(Path path) {
        return path.getFileName().toString().endsWith(".yml");
    }

    private static boolean isAdventureDuplicatesException(Exception e) {
        return e instanceof IllegalArgumentException && (e.getMessage().startsWith("Invalid key") || e.getMessage().startsWith("Translation already exists"));
    }

    @SuppressWarnings("unchecked")
    private Pair<Locale, Map<String, String>> loadTranslationFile(Path translationFile) {
        String fileName = translationFile.getFileName().toString();
        String localeString = fileName.substring(0, fileName.length() - ".yml".length());
        Locale locale = parseLocale(localeString);

        if (locale == null) {
            throw new IllegalStateException("Unknown locale '" + localeString + "' - unable to register.");
        }

        Map<String, String> bundle = new HashMap<>();
        YamlDocument document = plugin.getConfigManager().loadConfig("translations/" + translationFile.getFileName(), '@');
        try {
            document.save(new File(plugin.getDataDirectory().toFile(), "translations/" + translationFile.getFileName()));
        } catch (IOException e) {
            throw new IllegalStateException("Could not update translation file: " + translationFile.getFileName(), e);
        }
        Map<String, Object> map = document.getStringRouteMappedValues(false);
        map.remove("config-version");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof List<?> list) {
                List<String> strList = (List<String>) list;
                StringJoiner stringJoiner = new StringJoiner("<reset><newline>");
                for (String str : strList) {
                    stringJoiner.add(str);
                }
                bundle.put(entry.getKey(), stringJoiner.toString());
            } else if (entry.getValue() instanceof String str) {
                bundle.put(entry.getKey(), str);
            }
        }

        this.registry.registerAll(locale, bundle);
        this.installed.add(locale);

        return Pair.of(locale, bundle);
    }

    public static @Nullable Locale parseLocale(@Nullable String locale) {
        return locale == null ? null : Translator.parseLocale(locale);
    }
}
