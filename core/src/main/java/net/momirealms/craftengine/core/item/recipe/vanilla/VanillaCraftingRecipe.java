package net.momirealms.craftengine.core.item.recipe.vanilla;

import net.momirealms.craftengine.core.item.recipe.CraftingRecipeCategory;
import net.momirealms.craftengine.core.item.recipe.DatapackRecipeResult;

public abstract class VanillaCraftingRecipe extends VanillaGroupedRecipe {
    protected final CraftingRecipeCategory category;

    protected VanillaCraftingRecipe(CraftingRecipeCategory category, String group, DatapackRecipeResult result) {
        super(group, result);
        this.category = category;
    }

    public CraftingRecipeCategory category() {
        return category;
    }
}
