package net.momirealms.craftengine.core.util.condition;

import net.momirealms.craftengine.core.util.context.Condition;

public abstract class InvertedCondition<CTX> implements Condition<CTX> {
    protected final Condition<CTX> condition;

    public InvertedCondition(Condition<CTX> condition) {
        this.condition = condition;
    }

    @Override
    public boolean test(CTX ctx) {
        return !this.condition.test(ctx);
    }
}
