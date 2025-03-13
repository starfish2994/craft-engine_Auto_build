package net.momirealms.craftengine.core.plugin.config.template;

import net.momirealms.craftengine.core.util.Key;

public class ObjectTemplateArgument implements TemplateArgument {
    private final Object value;

    public ObjectTemplateArgument(Object value) {
        this.value = value;
    }

    @Override
    public Key type() {
        return TemplateArguments.OBJECT;
    }

    @Override
    public Object get() {
        return this.value;
    }
}
