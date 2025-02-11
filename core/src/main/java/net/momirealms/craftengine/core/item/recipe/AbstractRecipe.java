package net.momirealms.craftengine.core.item.recipe;

import org.jetbrains.annotations.Nullable;

public abstract class AbstractRecipe<T> implements Recipe<T> {
    protected RecipeCategory category;
    protected String group;

    protected AbstractRecipe(RecipeCategory category, String group) {
        this.category = category;
        this.group = group;
    }

    @Override
    @Nullable
    public RecipeCategory category() {
        return category;
    }

    @Override
    @Nullable
    public String group() {
        return group;
    }
}
