package net.momirealms.craftengine.core.item.recipe.postprocessor;

import net.momirealms.craftengine.core.item.recipe.CustomRecipeResult;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

public class PostProcessors {
    public static final Key APPLY_DATA = Key.of("craftengine", "apply_data");

    public static void init() {
        register(APPLY_DATA, ApplyItemDataProcessor.TYPE);
    }

    public static void register(Key id, CustomRecipeResult.PostProcessor.Type<?> type) {
        ((WritableRegistry<CustomRecipeResult.PostProcessor.Type<?>>) BuiltInRegistries.RECIPE_POST_PROCESSOR_TYPE)
                .register(ResourceKey.create(Registries.RECIPE_POST_PROCESSOR_TYPE.location(), id), type);
    }
}
