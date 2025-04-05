package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.core.util.Key;

public class FeatureUtils {

    private FeatureUtils() {}

    public static Object createFeatureKey(Key id) {
        try {
            return Reflections.method$ResourceKey$create.invoke(null, Reflections.instance$Registries$CONFIGURED_FEATURE, KeyUtils.toResourceLocation(id));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
