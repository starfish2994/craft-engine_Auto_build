package net.momirealms.craftengine.bukkit.item.recipe;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.jetbrains.annotations.NotNull;

record EmptyRecipeChoice() implements RecipeChoice {

    static final RecipeChoice INSTANCE = new EmptyRecipeChoice();

    @Override
    @NotNull
    public ItemStack getItemStack() {
        throw new UnsupportedOperationException("This is an empty RecipeChoice");
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    @NotNull
    public RecipeChoice clone() {
        return this;
    }

    @Override
    public boolean test(final @NotNull ItemStack itemStack) {
        return false;
    }

    @Override
    @NotNull
    public RecipeChoice validate(final boolean allowEmptyRecipes) {
        if (allowEmptyRecipes) return this;
        throw new IllegalArgumentException("empty RecipeChoice isn't allowed here");
    }
}
