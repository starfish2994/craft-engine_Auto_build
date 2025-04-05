package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.core.util.Key;

import java.util.HashMap;
import java.util.Map;

public class ItemTags {
    private static final Map<Key, Object> CACHE = new HashMap<>();

    public static final Key AXES = Key.of("minecraft:axes");
    public static final Key SWORDS = Key.of("minecraft:swords");

    private ItemTags() {}

    public static Object getOrCreate(Key key) {
        Object value = CACHE.get(key);
        if (value == null) {
            try {
                value = Reflections.method$TagKey$create.invoke(null, Reflections.instance$Registries$ITEM, KeyUtils.toResourceLocation(key));
                CACHE.put(key, value);
                return value;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create block tag: " + key, e);
            }
        } else {
            return value;
        }
    }
}
