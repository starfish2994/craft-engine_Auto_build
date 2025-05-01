package net.momirealms.craftengine.core.util.context;

import net.momirealms.craftengine.core.util.Key;

import java.util.function.Predicate;

public interface Condition<CTX> extends Predicate<CTX> {

    @Override
    default boolean test(CTX ctx) {
        return false;
    }

    Key type();
}
