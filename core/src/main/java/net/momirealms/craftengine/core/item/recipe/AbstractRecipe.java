package net.momirealms.craftengine.core.item.recipe;

import org.jetbrains.annotations.Nullable;

public abstract class AbstractRecipe<T> implements Recipe<T> {
    protected String group;

    protected AbstractRecipe(String group) {
        this.group = group;
    }

    @Override
    @Nullable
    public String group() {
        return group;
    }
}
