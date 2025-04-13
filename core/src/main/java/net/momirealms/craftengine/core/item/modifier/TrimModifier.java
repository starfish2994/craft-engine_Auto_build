package net.momirealms.craftengine.core.item.modifier;

import net.momirealms.craftengine.core.item.ComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.Trim;
import net.momirealms.craftengine.core.util.VersionHelper;

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

        if (VersionHelper.isVersionNewerThan1_20_5()) {

        } else {

        }
    }

    @Override
    public void remove(Item<I> item) {
        item.trim(null);
        if (VersionHelper.isVersionNewerThan1_20_5()) {
            item.removeComponent(ComponentKeys.TRIM);
        } else {
            item.removeTag("Trim");
        }
    }
}
