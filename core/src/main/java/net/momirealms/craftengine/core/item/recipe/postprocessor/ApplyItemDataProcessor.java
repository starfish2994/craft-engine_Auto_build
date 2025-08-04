package net.momirealms.craftengine.core.item.recipe.postprocessor;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.modifier.ItemDataModifier;
import net.momirealms.craftengine.core.item.recipe.CustomRecipeResult;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ApplyItemDataProcessor<T> implements CustomRecipeResult.PostProcessor<T> {
    public static final Type<?> TYPE = new Type<>();
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

    public static class Type<T> implements CustomRecipeResult.PostProcessor.Type<T> {

        @Override
        @SuppressWarnings("unchecked")
        public CustomRecipeResult.PostProcessor<T> create(Map<String, Object> args) {
            List<ItemDataModifier<?>> modifiers = new ArrayList<>();
            Map<String, Object> data = ResourceConfigUtils.getAsMap(args.get("data"), "data");
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                Optional.ofNullable(BuiltInRegistries.ITEM_DATA_MODIFIER_FACTORY.getValue(Key.withDefaultNamespace(entry.getKey(), "craftengine")))
                        .ifPresent(factory -> modifiers.add(factory.create(entry.getValue())));
            }
            return new ApplyItemDataProcessor<>(modifiers.toArray(new ItemDataModifier[0]));
        }
    }
}
