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

package net.momirealms.customfishing.common.dependency;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DependencyProperties {

    private final HashMap<String, String> versionMap;

    private DependencyProperties(HashMap<String, String> versionMap) {
        this.versionMap = versionMap;
    }

    public static String getDependencyVersion(String dependencyID) {
        if (!SingletonHolder.INSTANCE.versionMap.containsKey(dependencyID)) {
            throw new RuntimeException("Unknown dependency: " + dependencyID);
        }
        return SingletonHolder.INSTANCE.versionMap.get(dependencyID);
    }

    private static class SingletonHolder {

        private static final DependencyProperties INSTANCE = getInstance();

        private static DependencyProperties getInstance() {
             try (InputStream inputStream = DependencyProperties.class.getClassLoader().getResourceAsStream("library-version.properties")) {
                 HashMap<String, String> versionMap = new HashMap<>();
                 Properties properties = new Properties();
                 properties.load(inputStream);
                 for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                     if (entry.getKey() instanceof String key && entry.getValue() instanceof String value) {
                         versionMap.put(key, value);
                     }
                 }
                 return new DependencyProperties(versionMap);
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
        }
    }
}
