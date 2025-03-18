package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.input.RecipeInput;
import net.momirealms.craftengine.core.item.recipe.input.SmithingInput;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CustomSmithingTransformRecipe<T> implements Recipe<T> {
    public static final Factory<?> FACTORY = new Factory<>();
    private final Key id;
    private final CustomRecipeResult<T> result;
    private final Ingredient<T> base;
    private final Ingredient<T> template;
    private final Ingredient<T> addition;

    public CustomSmithingTransformRecipe(Key id,
                                         @Nullable Ingredient<T> addition,
                                         @Nullable Ingredient<T> base,
                                         @Nullable Ingredient<T> template,
                                         CustomRecipeResult<T> result
    ) {
        this.id = id;
        this.result = result;
        this.base = base;
        this.template = template;
        this.addition = addition;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean matches(RecipeInput input) {
        SmithingInput<T> smithingInput = (SmithingInput<T>) input;
        return checkIngredient(this.base, smithingInput.base())
                && checkIngredient(this.template, smithingInput.template())
                && checkIngredient(this.addition, smithingInput.addition());
    }

    private boolean checkIngredient(Ingredient<T> ingredient, OptimizedIDItem<T> item) {
        if (ingredient != null) {
            if (item == null) {
                return false;
            }
            return ingredient.test(item);
        } else {
            return item == null;
        }
    }

    @Override
    public List<Ingredient<T>> ingredientsInUse() {
        List<Ingredient<T>> ingredients = new ArrayList<>();
        ingredients.add(this.base);
        if (this.template != null) {
            ingredients.add(this.template);
        }
        if (this.addition != null) {
            ingredients.add(this.addition);
        }
        return ingredients;
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

    @Nullable
    public Ingredient<T> base() {
        return base;
    }

    @Nullable
    public Ingredient<T> template() {
        return template;
    }

    @Nullable
    public Ingredient<T> addition() {
        return addition;
    }

    @SuppressWarnings({"DuplicatedCode"})
    public static class Factory<A> implements RecipeFactory<A> {

        @Override
        public Recipe<A> create(Key id, Map<String, Object> arguments) {
            List<String> base = MiscUtils.getAsStringList(arguments.get("base"));
            List<String> addition = MiscUtils.getAsStringList(arguments.get("addition"));
            List<String> template = MiscUtils.getAsStringList(arguments.get("template"));
            return new CustomSmithingTransformRecipe<>(
                    id,
                    toIngredient(addition), toIngredient(base), toIngredient(template), parseResult(arguments)
            );
        }

        private Ingredient<A> toIngredient(List<String> items) {
            Set<Holder<Key>> holders = new HashSet<>();
            for (String item : items) {
                if (item.charAt(0) == '#') {
                    holders.addAll(CraftEngine.instance().itemManager().tagToItems(Key.of(item.substring(1))));
                } else {
                    holders.add(BuiltInRegistries.OPTIMIZED_ITEM_ID.get(Key.of(item)).orElseThrow(() -> new IllegalArgumentException("Invalid vanilla/custom item: " + item)));
                }
            }
            return holders.isEmpty() ? null : Ingredient.of(holders);
        }
    }
}
