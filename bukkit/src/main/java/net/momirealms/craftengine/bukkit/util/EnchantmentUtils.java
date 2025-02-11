package net.momirealms.craftengine.bukkit.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EnchantmentUtils {

    @SuppressWarnings("unchecked")
    public static Map<String, Integer> toMap(Object itemEnchantments) throws ReflectiveOperationException {
        Map<String, Integer> map = new HashMap<>();
        for (Object2IntMap.Entry<Object> entry : (Set<Object2IntMap.Entry<Object>>) Reflections.method$ItemEnchantments$entrySet.invoke(itemEnchantments)) {
            Object holder = entry.getKey();
            String name = (String) Reflections.method$Holder$getRegisteredName.invoke(holder);
            int level = entry.getIntValue();
            map.put(name, level);
        }
        return map;
    }
}
