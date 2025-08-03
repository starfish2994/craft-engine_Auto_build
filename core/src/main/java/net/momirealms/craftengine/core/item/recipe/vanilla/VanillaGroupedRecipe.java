package net.momirealms.craftengine.core.item.recipe.vanilla;

import net.momirealms.craftengine.core.item.recipe.DatapackRecipeResult;

public abstract class VanillaGroupedRecipe implements VanillaRecipe {
    protected final String group;
    protected final DatapackRecipeResult result;

    protected VanillaGroupedRecipe(String group, DatapackRecipeResult result) {
        this.group = group;
        this.result = result;
    }

    public String group() {
        return group;
    }

    public DatapackRecipeResult result() {
        return result;
    }
}
