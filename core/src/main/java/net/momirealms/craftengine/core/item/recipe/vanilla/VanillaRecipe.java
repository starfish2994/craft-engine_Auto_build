package net.momirealms.craftengine.core.item.recipe.vanilla;

import net.momirealms.craftengine.core.item.recipe.RecipeCategory;

public abstract class VanillaRecipe {
    protected final String group;
    protected final RecipeCategory category;
    protected final RecipeResult result;

    protected VanillaRecipe(RecipeCategory category, String group, RecipeResult result) {
        this.category = category;
        this.group = group;
        this.result = result;
    }

    public RecipeCategory category() {
        return category;
    }

    public String group() {
        return group;
    }

    public RecipeResult result() {
        return result;
    }
}
