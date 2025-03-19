package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.item.Item;
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
import java.util.function.BiFunction;

public class CustomSmithingTransformRecipe<T> implements Recipe<T> {
    public static final Factory<?> FACTORY = new Factory<>();
    private final Key id;
    private final CustomRecipeResult<T> result;
    private final Ingredient<T> base;
    private final Ingredient<T> template;
    private final Ingredient<T> addition;
    private final List<ItemDataProcessor> processors;

    public CustomSmithingTransformRecipe(Key id,
                                         @Nullable Ingredient<T> base,
                                         @Nullable Ingredient<T> template,
                                         @Nullable Ingredient<T> addition,
                                         CustomRecipeResult<T> result,
                                         List<ItemDataProcessor> processors
    ) {
        this.id = id;
        this.result = result;
        this.base = base;
        this.template = template;
        this.addition = addition;
        this.processors = processors;
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
            if (item == null || item.isEmpty()) {
                return false;
            }
            return ingredient.test(item);
        } else {
            return item == null || item.isEmpty();
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

    @SuppressWarnings("unchecked")
    public T assemble(ItemBuildContext context, Item<T> base) {
        T result = this.result(context);
        Item<T> wrappedResult = (Item<T>) CraftEngine.instance().itemManager().wrap(result);
        Item<T> finalResult = wrappedResult;
        for (ItemDataProcessor processor : this.processors) {
            finalResult = (Item<T>) processor.apply(base, wrappedResult);
        }
        return finalResult.getItem();
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
            List<String> template = MiscUtils.getAsStringList(arguments.get("template-type"));
            return new CustomSmithingTransformRecipe<>(
                    id,
                    toIngredient(base), toIngredient(template),toIngredient(addition), parseResult(arguments),
                    List.of(ItemDataProcessor.MERGE_ALL)
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

    @FunctionalInterface
    public interface ItemDataProcessor extends BiFunction<Item<?>, Item<?>, Item<?>> {
        MergeAllDataProcessor MERGE_ALL = new MergeAllDataProcessor();
    }

    public static class MergeAllDataProcessor implements ItemDataProcessor {

        @Override
        public Item<?> apply(Item<?> item1, Item<?> item2) {
            return item1.merge(item2);
        }
    }
}
