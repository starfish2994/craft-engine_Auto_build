package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.util.EnumUtils;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.*;

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
                        () -> new LocalizedResourceConfigException("warning.config.recipe.invalid_item", item)));
            }
        }
        return holders;
    }

    protected Object getIngredientOrThrow(Map<String, Object> arguments) {
        Object ingredient = ResourceConfigUtils.get(arguments, "ingredient", "ingredients");
        if (ingredient == null) {
            throw new LocalizedResourceConfigException("warning.config.recipe.missing_ingredient");
        }
        return ingredient;
    }

    protected CookingRecipeCategory cookingRecipeCategory(Map<String, Object> arguments) {
        CookingRecipeCategory recipeCategory;
        try {
            recipeCategory = arguments.containsKey("category") ? CookingRecipeCategory.valueOf(arguments.get("category").toString().toUpperCase(Locale.ENGLISH)) : null;
        } catch (IllegalArgumentException e) {
            throw new LocalizedResourceConfigException("warning.config.recipe.cooking.invalid_category", e, arguments.get("category").toString(), EnumUtils.toString(CookingRecipeCategory.values()));
        }
        return recipeCategory;
    }

    protected CraftingRecipeCategory craftingRecipeCategory(Map<String, Object> arguments) {
        CraftingRecipeCategory recipeCategory;
        try {
            recipeCategory = arguments.containsKey("category") ? CraftingRecipeCategory.valueOf(arguments.get("category").toString().toUpperCase(Locale.ENGLISH)) : null;
        } catch (IllegalArgumentException e) {
            throw new LocalizedResourceConfigException("warning.config.recipe.crafting.invalid_category", e, arguments.get("category").toString(), EnumUtils.toString(CraftingRecipeCategory.values()));
        }
        return recipeCategory;
    }
}
