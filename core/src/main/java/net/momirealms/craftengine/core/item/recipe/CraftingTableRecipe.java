package net.momirealms.craftengine.core.item.recipe;

public abstract class CraftingTableRecipe<T> extends AbstractRecipe<T> {
    protected final CraftingRecipeCategory category;

    protected CraftingTableRecipe(CraftingRecipeCategory category, String group) {
        super(group);
        this.category = category;
    }

    public CraftingRecipeCategory category() {
        return category;
    }
}
