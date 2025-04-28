package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CustomSmokingRecipe<T> extends CustomCookingRecipe<T> {
    public static final Factory<?> FACTORY = new Factory<>();

    public CustomSmokingRecipe(Key id, CookingRecipeCategory category, String group, Ingredient<T> ingredient, int cookingTime, float experience, CustomRecipeResult<T> result) {
        super(id, category, group, ingredient, cookingTime, experience, result);
    }

    @Override
    public @NotNull Key type() {
        return RecipeTypes.SMOKING;
    }

    public static class Factory<A> extends AbstractRecipeFactory<A> {

        @SuppressWarnings({"unchecked", "rawtypes", "DuplicatedCode"})
        @Override
        public Recipe<A> create(Key id, Map<String, Object> arguments) {
            String group = arguments.containsKey("group") ? arguments.get("group").toString() : null;
            int cookingTime = MiscUtils.getAsInt(arguments.getOrDefault("time", 80));
            float experience = MiscUtils.getAsFloat(arguments.getOrDefault("experience", 0.0f));
            Set<Holder<Key>> holders = ingredientHolders(arguments);
            return new CustomSmokingRecipe(id, cookingRecipeCategory(arguments), group, Ingredient.of(holders), cookingTime, experience, parseResult(arguments));
        }
    }
}
