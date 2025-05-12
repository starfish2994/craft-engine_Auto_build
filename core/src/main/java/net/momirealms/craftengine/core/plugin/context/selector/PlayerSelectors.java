package net.momirealms.craftengine.core.plugin.context.selector;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PlayerSelectors {
    public static final Key ALL = Key.of("craftengine:all");
    public static final Key SELF = Key.of("craftengine:self");

    public static <CTX extends Context> PlayerSelector<CTX> fromObject(Object object, Function<Map<String, Object>, Condition<CTX>> conditionFactory) {
        if (object == null) return null;
        if (object instanceof String string) {
            if (string.equals("self") || string.equals("@self") || string.equals("@s")) {
                return new SelfPlayerSelector<>();
            } else if (string.equals("all") || string.equals("@all") || string.equals("@a")) {
                return new AllPlayerSelector<>();
            }
        } else if (object instanceof Map<?,?> map) {
            Map<String, Object> selectorMap = MiscUtils.castToMap(map, false);
            Object typeObj = selectorMap.get("type");
            Object conditionObj = ResourceConfigUtils.get(selectorMap, "conditions");
            if (!(typeObj instanceof String typeString)) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            if (typeString.equals("all") || typeString.equals("@all") || typeString.equals("@a")) {
                List<Condition<CTX>> conditions = new ArrayList<>();
                if (conditionObj instanceof List<?> list) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> conditionList = (List<Map<String, Object>>) list;
                    for (Map<String, Object> condition : conditionList) {
                        conditions.add(conditionFactory.apply(condition));
                    }
                } else if (conditionObj instanceof Map<?,?>) {
                    conditions.add(conditionFactory.apply(MiscUtils.castToMap(conditionObj, false)));
                } else {
                    return new AllPlayerSelector<>();
                }
                return new AllPlayerSelector<>(conditions);
            }
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
