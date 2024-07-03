package net.momirealms.customfishing.common.util;

public record Key(String namespace, String value) {

    public static Key of(String namespace, String value) {
        return new Key(namespace, value);
    }

    public static Key fromString(String key) {
        String[] split = key.split(":", 2);
        return of(split[0], split[1]);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Key key)) return false;
        return this.namespace.equals(key.namespace()) && this.value.equals(key.value());
    }

    @Override
    public String toString() {
        return namespace + ":" + value;
    }
}