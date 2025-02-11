package net.momirealms.craftengine.core.item.recipe.vanilla.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.item.recipe.vanilla.RecipeResult;
import net.momirealms.craftengine.core.item.recipe.vanilla.VanillaShapedRecipe;
import net.momirealms.craftengine.core.item.recipe.vanilla.VanillaShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VanillaRecipeReader1_20 extends AbstractRecipeReader {

    @Override
    public VanillaShapedRecipe readShaped(JsonObject json) {
        return new VanillaShapedRecipe(
                readCategory(json),
                readGroup(json),
                readShapedIngredientMap(json.getAsJsonObject("key")),
                readPattern(json),
                readResult(json.getAsJsonObject("result"))
        );
    }

    @Override
    public VanillaShapelessRecipe readShapeless(JsonObject json) {
        return new VanillaShapelessRecipe(
                readCategory(json),
                readGroup(json),
                readShapelessIngredients(json.getAsJsonArray("ingredients")),
                readResult(json.getAsJsonObject("result"))
        );
    }

    @NotNull
    protected RecipeResult readResult(JsonObject object) {
        String item = object.get("item").getAsString();
        int count = object.has("count") ? object.get("count").getAsInt() : 1;
        return new RecipeResult(item, count, null);
    }

    protected List<List<String>> readShapelessIngredients(JsonArray json) {
        List<List<String>> ingredients = new ArrayList<>();
        for (JsonElement element : json) {
            if (element.isJsonObject()) {
                JsonObject jsonObject = element.getAsJsonObject();
                if (jsonObject.has("item")) {
                    ingredients.add(List.of(jsonObject.get("item").getAsString()));
                } else if (jsonObject.has("tag")) {
                    ingredients.add(List.of("#" + jsonObject.get("tag").getAsString()));
                }
            } else if (element.isJsonArray()) {
                List<String> ingredient = readIngredientList((JsonArray) element);
                ingredients.add(ingredient);
            }
        }
        return ingredients;
    }

    protected Map<Character, List<String>> readShapedIngredientMap(JsonObject json) {
        Map<Character, List<String>> ingredients = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            char c = entry.getKey().charAt(0);
            if (entry.getValue().isJsonObject()) {
                JsonObject argument = entry.getValue().getAsJsonObject();
                if (argument.has("item")) {
                    ingredients.put(c, List.of(argument.get("item").getAsString()));
                } else if (argument.has("tag")) {
                    ingredients.put(c, List.of("#" + argument.get("tag").getAsString()));
                }
            } else if (entry.getValue().isJsonArray()) {
                List<String> items = readIngredientList((JsonArray) entry.getValue());
                ingredients.put(c, items);
            }
        }
        return ingredients;
    }

    protected @NotNull List<String> readIngredientList(JsonArray array) {
        List<String> items = new ArrayList<>();
        for (JsonElement element : array) {
            if (element.isJsonObject()) {
                JsonObject argument = element.getAsJsonObject();
                if (argument.has("item")) {
                    items.add(argument.get("item").getAsString());
                } else if (argument.has("tag")) {
                    items.add("#" + argument.get("tag").getAsString());
                }
            }
        }
        return items;
    }
}
