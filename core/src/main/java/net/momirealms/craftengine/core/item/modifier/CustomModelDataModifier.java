package net.momirealms.craftengine.core.item.modifier;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;

public class CustomModelDataModifier<I> implements ItemDataModifier<I> {
    private final int argument;

    public CustomModelDataModifier(int argument) {
        this.argument = argument;
    }

    @Override
    public String name() {
        return "custom-model-data";
    }

    @Override
    public void apply(Item<I> item, ItemBuildContext context) {
        item.customModelData(argument);
    }

    @Override
    public void remove(Item<I> item) {
        item.customModelData(null);
    }
}
