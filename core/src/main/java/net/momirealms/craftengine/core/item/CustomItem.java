package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.modifier.ItemDataModifier;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.context.ContextHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface CustomItem<I> extends BuildableItem<I> {

    Key id();

    Key material();

    List<ItemDataModifier<I>> dataModifiers();

    Map<String, ItemDataModifier<I>> dataModifierMap();

    boolean hasClientBoundDataModifier();

    List<ItemDataModifier<I>> clientBoundDataModifiers();

    Map<String, ItemDataModifier<I>> clientBoundDataModifierMap();

    ItemSettings settings();

    default boolean is(Key tag) {
        return settings().tags().contains(tag);
    }

    default Item<I> buildItem(Player player) {
        return buildItem(new ItemBuildContext(player, ContextHolder.EMPTY));
    }

    Item<I> buildItem(ItemBuildContext context);

    @NotNull
    List<ItemBehavior> behaviors();

    interface Builder<I> {
        Builder<I> id(Key id);

        Builder<I> material(Key material);

        Builder<I> dataModifier(ItemDataModifier<I> modifier);

        Builder<I> dataModifiers(List<ItemDataModifier<I>> modifiers);

        Builder<I> clientBoundDataModifier(ItemDataModifier<I> modifier);

        Builder<I> clientBoundDataModifiers(List<ItemDataModifier<I>> modifiers);

        Builder<I> behavior(ItemBehavior behavior);

        Builder<I> behaviors(List<ItemBehavior> behaviors);

        Builder<I> settings(ItemSettings settings);

        CustomItem<I> build();
    }
}
