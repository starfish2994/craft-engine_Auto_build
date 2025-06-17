package net.momirealms.craftengine.core.entity.data;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.momirealms.craftengine.core.plugin.CraftEngine;

public class ClassTreeIdRegistry {
    private final Object2IntMap<Class<?>> classToLastIdCache = new Object2IntOpenHashMap<>();

    public ClassTreeIdRegistry() {
        classToLastIdCache.defaultReturnValue(-1);
    }

    public int getLastIdFor(Class<?> clazz) {
        int cachedId = this.classToLastIdCache.getInt(clazz);
        if (cachedId == -1) {
            Class<?> currentClass = clazz;
            while ((currentClass = currentClass.getSuperclass()) != Object.class) {
                int parentCachedId = this.classToLastIdCache.getInt(currentClass);
                if (parentCachedId != -1) {
                    return parentCachedId;
                }
            }
            return -1;
        } else {
            return cachedId;
        }
    }

    public int define(Class<?> clazz) {
        int lastId = this.getLastIdFor(clazz);
        int nextId = lastId == -1 ? 0 : lastId + 1;
        this.classToLastIdCache.put(clazz, nextId);
        CraftEngine.instance().debug(() -> "Defined " + clazz.getSimpleName() + " with id " + nextId);
        return nextId;
    }
}