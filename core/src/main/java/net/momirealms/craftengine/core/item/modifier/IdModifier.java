package net.momirealms.craftengine.core.item.modifier;

import net.momirealms.craftengine.core.item.ComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;

import java.util.Map;

public class IdModifier<I> implements ItemDataModifier<I> {
    public static final String CRAFT_ENGINE_ID = "craftengine:id";
    private final Key argument;

    public IdModifier(Key argument) {
        this.argument = argument;
    }

    @Override
    public String name() {
        return "id";
    }

    @Override
    public void apply(Item<I> item, ItemBuildContext context) {
        if (VersionHelper.isVersionNewerThan1_20_5()) {
            item.setComponent(ComponentKeys.CUSTOM_DATA, Map.of(CRAFT_ENGINE_ID, argument.toString()));
        } else {
            item.setTag(argument.toString(), CRAFT_ENGINE_ID);
        }
    }

    @Override
    public void remove(Item<I> item) {
        item.removeTag(CRAFT_ENGINE_ID);
    }
}
