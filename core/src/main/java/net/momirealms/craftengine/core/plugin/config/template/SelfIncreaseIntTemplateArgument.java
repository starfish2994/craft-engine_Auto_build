package net.momirealms.craftengine.core.plugin.config.template;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.Map;

public class SelfIncreaseIntTemplateArgument implements TemplateArgument {
    public static final Factory FACTORY = new Factory();
    private final int min;
    private final int max;
    private int current;

    public SelfIncreaseIntTemplateArgument(int min, int max) {
        this.min = min;
        this.max = max;
        this.current = min;
    }

    @Override
    public String get() {
        String value = String.valueOf(current);
        if (current < max) current += 1;
        return value;
    }

    @Override
    public Key type() {
        return TemplateArguments.SELF_INCREASE_INT;
    }

    public int current() {
        return current;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    public static class Factory implements TemplateArgumentFactory {
        @Override
        public TemplateArgument create(Map<String, Object> arguments) {
            int from = ResourceConfigUtils.getAsInt(arguments.get("from"), "from");
            int to = ResourceConfigUtils.getAsInt(arguments.get("to"), "to");
            if (from > to) throw new LocalizedResourceConfigException("warning.config.template.from_larger_than_to", String.valueOf(from), String.valueOf(to));
            return new SelfIncreaseIntTemplateArgument(from, to);
        }
    }
}
