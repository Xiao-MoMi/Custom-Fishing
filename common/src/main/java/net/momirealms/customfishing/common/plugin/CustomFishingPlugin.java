/*
 *  Copyright (C) <2022> <XiaoMoMi>
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
