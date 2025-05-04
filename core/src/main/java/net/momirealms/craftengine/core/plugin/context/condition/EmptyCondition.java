package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Factory;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public class EmptyCondition<CTX extends Context> implements Condition<CTX> {

    @Override
    public Key type() {
        return SharedConditions.EMPTY;
    }

    @Override
    public boolean test(CTX ctx) {
        return true;
    }

    public static class FactoryImpl<CTX extends Context> implements Factory<Condition<CTX>> {

        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            return new EmptyCondition<>();
        }
    }
}
