package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Factory;
import net.momirealms.craftengine.core.util.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class AllOfCondition<CTX extends Context> implements Condition<CTX> {
    protected final List<? extends Condition<CTX>> conditions;

    public AllOfCondition(List<? extends Condition<CTX>> conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean test(CTX ctx) {
        for (Condition<CTX> condition : conditions) {
            if (!condition.test(ctx)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Key type() {
        return SharedConditions.ALL_OF;
    }

    public static class FactoryImpl<CTX extends Context> implements Factory<Condition<CTX>> {
        private final Function<Map<String, Object>, Condition<CTX>> factory;

        public FactoryImpl(Function<Map<String, Object>, Condition<CTX>> factory) {
            this.factory = factory;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            List<Map<String, Object>> terms = (List<Map<String, Object>>) arguments.get("terms");
            List<Condition<CTX>> conditions = new ArrayList<>();
            for (Map<String, Object> term : terms) {
                conditions.add(factory.apply(term));
            }
            return new AllOfCondition<>(conditions);
        }
    }
}
