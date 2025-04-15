package net.momirealms.craftengine.core.item.modifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.momirealms.craftengine.core.item.ComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;

import java.util.Map;
import java.util.function.BiConsumer;

public class IdModifier<I> implements ItemDataModifier<I> {
    public static final String CRAFT_ENGINE_ID = "craftengine:id";
    private static final BiConsumer<Item<?>, String> ID_SETTER = VersionHelper.isVersionNewerThan1_20_5() ?
            ((item, id) -> {
                JsonElement element = item.getJsonTypeComponent(ComponentKeys.CUSTOM_DATA);
                if (element instanceof JsonObject jo) {
                    jo.add(CRAFT_ENGINE_ID, new JsonPrimitive(id));
                    item.setComponent(ComponentKeys.CUSTOM_DATA, jo);
                } else {
                    item.setComponent(ComponentKeys.CUSTOM_DATA, Map.of(CRAFT_ENGINE_ID, id));
                }
            }) : ((item, id) -> item.setTag(id, CRAFT_ENGINE_ID));
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
        ID_SETTER.accept(item, argument.toString());
    }

    @Override
    public void remove(Item<I> item) {
        item.removeTag(CRAFT_ENGINE_ID);
    }
}
