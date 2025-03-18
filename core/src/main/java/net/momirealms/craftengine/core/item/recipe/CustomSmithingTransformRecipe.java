package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.input.RecipeInput;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CustomSmithingTransformRecipe<T> implements Recipe<T> {
    private final Key id;
    private final CustomRecipeResult<T> result;
    private final Ingredient<T> template;
    private final Ingredient<T> base;
    private final Ingredient<T> addition;

    public CustomSmithingTransformRecipe(Key id,
                                         CustomRecipeResult<T> result,
                                         Ingredient<T> template,
                                         Ingredient<T> base,
                                         Ingredient<T> addition
    ) {
        this.id = id;
        this.result = result;
        this.template = template;
        this.base = base;
        this.addition = addition;
    }

    @Override
    public boolean matches(RecipeInput input) {
        return false;
    }

    @Override
    public List<Ingredient<T>> ingredientsInUse() {
        return List.of();
    }

    @Override
    public @NotNull Key type() {
        return RecipeTypes.SMITHING_TRANSFORM;
    }

    @Override
    public Key id() {
        return id;
    }

    @Override
    public T result(ItemBuildContext context) {
        return result.buildItemStack(context);
    }

    @Override
    public CustomRecipeResult<T> result() {
        return this.result;
    }
}
