package net.momirealms.craftengine.core.item.modifier;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.Trim;

public class TrimModifier<I> implements ItemDataModifier<I> {
    private final String material;
    private final String pattern;

    public TrimModifier(String material, String pattern) {
        this.material = material;
        this.pattern = pattern;
    }

    @Override
    public String name() {
        return "trim";
    }

    @Override
    public void apply(Item<I> item, ItemBuildContext context) {
        item.trim(new Trim(this.material, this.pattern));
    }

    @Override
    public void remove(Item<I> item) {
        item.trim(null);
    }
}
