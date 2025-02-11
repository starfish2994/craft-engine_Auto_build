package net.momirealms.craftengine.core.item.recipe;

public abstract class CraftingTableRecipe<T> extends AbstractRecipe<T> {

    protected CraftingTableRecipe(RecipeCategory category, String group) {
        super(category, group);
    }
}
