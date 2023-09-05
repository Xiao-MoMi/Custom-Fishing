package net.momirealms.customfishing.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ClassUtils {

    @Nullable
    public static <T> Class<? extends T> findClass(
            @NotNull File file,
            @NotNull Class<T> clazz
    ) throws IOException, ClassNotFoundException {
        if (!file.exists()) {
            return null;
        }

        URL jar = file.toURI().toURL();
        URLClassLoader loader = new URLClassLoader(new URL[]{jar}, clazz.getClassLoader());
        List<String> matches = new ArrayList<>();

        try (JarInputStream stream = new JarInputStream(jar.openStream())) {
            JarEntry entry;
            while ((entry = stream.getNextJarEntry()) != null) {
                final String name = entry.getName();
                if (!name.endsWith(".class")) {
                    continue;
                }
                matches.add(name.substring(0, name.lastIndexOf('.')).replace('/', '.'));
            }

            for (String match : matches) {
                try {
                    Class<?> loaded = loader.loadClass(match);
                    if (clazz.isAssignableFrom(loaded)) {
                        loader.close();
                        return loaded.asSubclass(clazz);
                    }
                } catch (NoClassDefFoundError ignored) {
                }
            }
        }
        loader.close();
        return null;
    }
}
