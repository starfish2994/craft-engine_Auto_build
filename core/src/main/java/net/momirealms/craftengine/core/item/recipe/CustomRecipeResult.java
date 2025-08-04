package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.item.BuildableItem;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public record CustomRecipeResult<T>(BuildableItem<T> item, int count, PostProcessor<T>[] postProcessors) {

    public T buildItemStack(ItemBuildContext context) {
        return buildItem(context).getItem();
    }

    public Item<T> buildItem(ItemBuildContext context) {
        Item<T> builtItem = this.item.buildItem(context, count);
        if (this.postProcessors != null) {
            for (PostProcessor<T> postProcessor : this.postProcessors) {
                builtItem = postProcessor.process(builtItem, context);
            }
        }
        return builtItem;
    }

    @FunctionalInterface
    @SuppressWarnings("unchecked")
    public interface PostProcessor<T> {

        static <T> PostProcessor<T> fromMap(Map<String, Object> map) {
            String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.recipe.result.post_processor.missing_type");
            Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
            PostProcessor.Type<T> processor = (PostProcessor.Type<T>) BuiltInRegistries.RECIPE_POST_PROCESSOR_TYPE.getValue(key);
            if (processor == null) {
                throw new LocalizedResourceConfigException("warning.config.recipe.result.post_processor.invalid_type", type);
            }
            return processor.create(map);
        }

        Item<T> process(Item<T> item, ItemBuildContext context);

        interface Type<T> {

            PostProcessor<T> create(Map<String, Object> args);
        }
    }
}
