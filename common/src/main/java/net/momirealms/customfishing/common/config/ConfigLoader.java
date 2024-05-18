package net.momirealms.customfishing.common.config;

import dev.dejvokep.boostedyaml.YamlDocument;

import java.io.File;

public interface ConfigLoader {

    YamlDocument loadConfig(String filePath);

    YamlDocument loadConfig(String filePath, char routeSeparator);

    YamlDocument loadData(File file);

    YamlDocument loadData(File file, char routeSeparator);
}
