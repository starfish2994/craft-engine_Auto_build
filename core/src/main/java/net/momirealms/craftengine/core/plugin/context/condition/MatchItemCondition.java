package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.CommonParameters;
import net.momirealms.craftengine.core.util.Factory;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.*;

public class MatchItemCondition<CTX extends Context> implements Condition<CTX> {
    private final Set<String> ids;
    private final boolean regexMatch;

    public MatchItemCondition(Collection<String> ids, boolean regexMatch) {
        this.ids = new HashSet<>(ids);
        this.regexMatch = regexMatch;
    }

    @Override
    public Key type() {
        return SharedConditions.MATCH_ITEM;
    }

    @Override
    public boolean test(CTX ctx) {
        Optional<Item<?>> item = ctx.getOptionalParameter(CommonParameters.TOOL);
        if (item.isEmpty()) return false;
        Key key = item.get().id();
        String itemId = key.toString();
        if (this.regexMatch) {
            for (String regex : ids) {
                if (itemId.matches(regex)) {
                    return true;
                }
            }
        } else {
            return this.ids.contains(itemId);
        }
        return false;
    }

    public static class FactoryImpl<CTX extends Context> implements Factory<Condition<CTX>> {

        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            List<String> ids = MiscUtils.getAsStringList(arguments.get("id"));
            boolean regex = (boolean) arguments.getOrDefault("regex", false);
            return new MatchItemCondition<>(ids, regex);
        }
    }
}
