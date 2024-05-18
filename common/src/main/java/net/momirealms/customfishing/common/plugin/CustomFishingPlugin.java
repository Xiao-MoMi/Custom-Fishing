package net.momirealms.customfishing.common.plugin;

import net.momirealms.customfishing.common.config.ConfigLoader;
import net.momirealms.customfishing.common.dependency.DependencyManager;
import net.momirealms.customfishing.common.locale.TranslationManager;
import net.momirealms.customfishing.common.plugin.classpath.ClassPathAppender;
import net.momirealms.customfishing.common.plugin.logging.PluginLogger;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerAdapter;

import java.io.InputStream;
import java.nio.file.Path;

public interface CustomFishingPlugin {

    InputStream getResourceStream(String filePath);

    PluginLogger getPluginLogger();

    ClassPathAppender getClassPathAppender();

    SchedulerAdapter<?> getScheduler();

    Path getDataDirectory();

    default Path getConfigDirectory() {
        return getDataDirectory();
    }

    DependencyManager getDependencyManager();

    TranslationManager getTranslationManager();

    ConfigLoader getConfigManager();

    String getServerVersion();

    String getPluginVersion();
}
