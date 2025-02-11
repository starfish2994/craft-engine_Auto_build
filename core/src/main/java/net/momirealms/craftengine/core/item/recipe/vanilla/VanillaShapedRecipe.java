package net.momirealms.craftengine.core.item.recipe.vanilla;

import net.momirealms.craftengine.core.item.recipe.RecipeCategory;

import java.util.List;
import java.util.Map;

public class VanillaShapedRecipe extends VanillaRecipe {
    private final String[] pattern;
    private final Map<Character, List<String>> key;

    public VanillaShapedRecipe(RecipeCategory category,
                               String group,
                               Map<Character, List<String>> key,
                               String[] pattern,
                               RecipeResult result) {
        super(category, group, result);
        this.key = key;
        this.pattern = pattern;
    }

    public Map<Character, List<String>> ingredients() {
        return key;
    }

    public String[] pattern() {
        return pattern;
    }
}
