package net.momirealms.craftengine.core.item.modifier;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ComponentModifier<I> implements ItemDataModifier<I> {
    private final List<Pair<String, Object>> arguments;

    public ComponentModifier(Map<String, Object> arguments) {
        List<Pair<String, Object>> pairs = new ArrayList<>(arguments.size());
        for (Map.Entry<String, Object> entry : arguments.entrySet()) {
            pairs.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        this.arguments = pairs;
    }

    @Override
    public String name() {
        return "components";
    }

    @Override
    public void apply(Item<I> item, ItemBuildContext context) {
        for (Pair<String, Object> entry : this.arguments) {
            item.setComponent(entry.left(), entry.right());
        }
    }

    @Override
    public void remove(Item<I> item) {
        for (Pair<String, Object> entry : this.arguments) {
            item.removeComponent(entry.left());
        }
    }
}
