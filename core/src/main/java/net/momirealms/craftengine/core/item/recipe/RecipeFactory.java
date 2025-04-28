package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public interface RecipeFactory<T> {

    Recipe<T> create(Key id, Map<String, Object> arguments);

    @SuppressWarnings({"unchecked", "rawtypes"})
    default CustomRecipeResult<T> parseResult(Map<String, Object> arguments) {
        Map<String, Object> resultMap = MiscUtils.castToMap(arguments.get("result"), true);
        if (resultMap == null) {
            throw new LocalizedResourceConfigException("warning.config.recipe.lack_result", new IllegalArgumentException("result cannot be empty for recipe"));
        }
        String id = (String) resultMap.get("id");
        if (id == null) {
            throw new LocalizedResourceConfigException("warning.config.recipe.result.lack_id", new IllegalArgumentException("id cannot be empty for result"));
        }
        int count = ResourceConfigUtils.getAsInt(resultMap.getOrDefault("count", 1), "count");
        return new CustomRecipeResult(
                CraftEngine.instance().itemManager().getBuildableItem(Key.of(id)).orElseThrow(
                        () -> new LocalizedResourceConfigException("warning.config.recipe.invalid_item", new IllegalArgumentException("Unknown recipe result item id: " + id), id)),
                count
        );
    }
}
