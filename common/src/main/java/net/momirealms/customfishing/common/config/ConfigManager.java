package net.momirealms.customfishing.common.config;

import dev.dejvokep.boostedyaml.YamlDocument;

public interface ConfigManager {

    YamlDocument loadConfig(String filePath);

    YamlDocument loadConfig(String filePath, char routeSeparator);
}
