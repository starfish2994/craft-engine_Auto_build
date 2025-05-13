package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.CommonParameters;
import net.momirealms.craftengine.core.util.ClickType;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class ClickTypeCondition<CTX extends Context> implements Condition<CTX> {
    private final ClickType clickType;

    public ClickTypeCondition(ClickType clickType) {
        this.clickType = clickType;
    }

    @Override
    public Key type() {
        return CommonConditions.CLICK_TYPE;
    }

    @Override
    public boolean test(CTX ctx) {
        Optional<ClickType> clickTypeOptional = ctx.getOptionalParameter(CommonParameters.CLICK_TYPE);
        if (clickTypeOptional.isPresent()) {
            ClickType clickType = clickTypeOptional.get();
            return clickType.equals(this.clickType);
        }
        return false;
    }

    public static class FactoryImpl<CTX extends Context> implements ConditionFactory<CTX> {

        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            ClickType clickType = ClickType.valueOf(ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("click-type"), "warning.config.condition.click_type.missing_click_type").toUpperCase(Locale.ENGLISH));
            return new ClickTypeCondition<>(clickType);
        }
    }
}
