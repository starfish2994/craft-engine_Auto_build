package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.item.recipe.input.RecipeInput;
import net.momirealms.craftengine.core.plugin.Reloadable;
import net.momirealms.craftengine.core.plugin.config.ConfigSectionParser;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface RecipeManager<T> extends Reloadable {

    ConfigSectionParser parser();

    boolean isDataPackRecipe(Key key);

    boolean isCustomRecipe(Key key);

    Optional<Recipe<T>> recipeById(Key id);

    List<Recipe<T>> recipesByType(Key type);

    List<Recipe<T>> recipeByResult(Key result);

    List<Recipe<T>> recipeByIngredient(Key ingredient);

    @Nullable
    Recipe<T> recipeByInput(Key type, RecipeInput input);

    @Nullable
    Recipe<T> recipeByInput(Key type, RecipeInput input, @Nullable Key lastRecipe);

    void runSyncTasks();
}
