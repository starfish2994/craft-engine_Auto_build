package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractRecipeFactory<T> implements RecipeFactory<T> {

    protected List<String> ingredients(Map<String, Object> arguments) {
        return MiscUtils.getAsStringList(getIngredientOrThrow(arguments));
    }

    protected Map<String, Object> ingredientMap(Map<String, Object> arguments) {
        return MiscUtils.castToMap(getIngredientOrThrow(arguments), true);
    }

    protected Set<Holder<Key>> ingredientHolders(Map<String, Object> arguments) {
        Set<Holder<Key>> holders = new HashSet<>();
        for (String item : ingredients(arguments)) {
            if (item.charAt(0) == '#') {
                holders.addAll(CraftEngine.instance().itemManager().tagToItems(Key.of(item.substring(1))));
            } else {
                holders.add(BuiltInRegistries.OPTIMIZED_ITEM_ID.get(Key.of(item)).orElseThrow(
                        () -> new LocalizedResourceConfigException("warning.config.recipe.invalid_item", new IllegalArgumentException("Invalid vanilla/custom item: " + item), item)));
            }
        }
        return holders;
    }

    protected Object getIngredientOrThrow(Map<String, Object> arguments) {
        Object ingredient = arguments.get("ingredient");
        if (ingredient == null) {
            ingredient = arguments.get("ingredients");
        }
        if (ingredient == null) {
            throw new LocalizedResourceConfigException("warning.config.recipe.lack_ingredient", new NullPointerException("'ingredient' should not be null"));
        }
        return ingredient;
    }
}
