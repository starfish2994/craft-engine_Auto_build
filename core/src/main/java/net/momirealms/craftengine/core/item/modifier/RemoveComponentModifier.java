package net.momirealms.craftengine.core.item.modifier;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;

import java.util.Collections;
import java.util.List;

public class RemoveComponentModifier<I> implements ItemDataModifier<I> {
    private final List<String> arguments;

    public RemoveComponentModifier(List<String> arguments) {
        this.arguments = arguments;
    }

    public List<String> arguments() {
        return Collections.unmodifiableList(this.arguments);
    }

    @Override
    public String name() {
        return "remove-components";
    }

    @Override
    public void apply(Item<I> item, ItemBuildContext context) {
        for (String argument : arguments) {
            item.removeComponent(argument);
        }
    }

    @Override
    public void remove(Item<I> item) {
        // I can't guess
    }
}
