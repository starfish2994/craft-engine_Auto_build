package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Factory;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;
import java.util.function.Function;

public class InvertedCondition<CTX extends Context> implements Condition<CTX> {
    protected final Condition<CTX> condition;

    public InvertedCondition(Condition<CTX> condition) {
        this.condition = condition;
    }

    @Override
    public boolean test(CTX ctx) {
        return !this.condition.test(ctx);
    }

    @Override
    public Key type() {
        return SharedConditions.INVERTED;
    }

    public static class FactoryImpl<CTX extends Context> implements Factory<Condition<CTX>> {
        private final Function<Map<String, Object>, Condition<CTX>> factory;

        public FactoryImpl(Function<Map<String, Object>, Condition<CTX>> factory) {
            this.factory = factory;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            Map<String, Object> term = (Map<String, Object>) arguments.get("term");
            return new InvertedCondition<>(this.factory.apply(term));
        }
    }
}
