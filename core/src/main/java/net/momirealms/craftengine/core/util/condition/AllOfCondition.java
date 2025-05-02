package net.momirealms.craftengine.core.util.condition;

import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.context.Condition;

import java.util.List;

public abstract class AllOfCondition<CTX> implements Condition<CTX> {
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
        return CommonConditions.ALL_OF;
    }
}
