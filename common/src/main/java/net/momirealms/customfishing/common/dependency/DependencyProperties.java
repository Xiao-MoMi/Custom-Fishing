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
