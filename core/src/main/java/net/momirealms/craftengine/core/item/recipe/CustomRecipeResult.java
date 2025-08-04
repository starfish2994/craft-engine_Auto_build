package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.item.BuildableItem;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.modifier.ItemDataModifier;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unchecked")
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

    static {
        registerPostProcessorType(Key.of("apply_data"), args -> {
            List<ItemDataModifier<?>> modifiers = new ArrayList<>();
            Map<String, Object> data = ResourceConfigUtils.getAsMap(args.get("data"), "data");
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                Optional.ofNullable(BuiltInRegistries.ITEM_DATA_MODIFIER_FACTORY.getValue(Key.withDefaultNamespace(entry.getKey(), "craftengine")))
                        .ifPresent(factory -> modifiers.add(factory.create(entry.getValue())));
            }
            return new ApplyItemDataProcessor<>(modifiers.toArray(new ItemDataModifier[0]));
        });
    }

    public static void registerPostProcessorType(Key id, PostProcessor.Type<?> type) {
        ((WritableRegistry<PostProcessor.Type<?>>) BuiltInRegistries.RECIPE_POST_PROCESSOR_TYPE)
                .register(ResourceKey.create(Registries.RECIPE_POST_PROCESSOR_TYPE.location(), id), type);
    }

    @FunctionalInterface
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

    public static class ApplyItemDataProcessor<T> implements PostProcessor<T> {
        private final ItemDataModifier<T>[] modifiers;

        public ApplyItemDataProcessor(ItemDataModifier<T>[] modifiers) {
            this.modifiers = modifiers;
        }

        @Override
        public Item<T> process(Item<T> item, ItemBuildContext context) {
            for (ItemDataModifier<T> modifier : this.modifiers) {
                item.apply(modifier, context);
            }
            return item;
        }
    }
}
