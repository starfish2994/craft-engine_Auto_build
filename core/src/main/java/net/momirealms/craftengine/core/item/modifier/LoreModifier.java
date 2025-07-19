package net.momirealms.craftengine.core.item.modifier;

import java.util.ArrayList;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.item.ComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.NetworkItemHandler;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.Tag;

import java.util.List;

public class LoreModifier<I> implements ItemDataModifier<I> {
    private final List<LoreModification> argument;

    public LoreModifier(List<LoreModification> argument) {
        argument = new ArrayList<>(argument);
        argument.sort(null);
        this.argument = List.copyOf(argument);
    }

    @Override
    public String name() {
        return "lore";
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        item.loreComponent(argument.stream().reduce(Stream.<Component>empty(), (stream, modification) -> modification.apply(stream, context), Stream::concat).toList());
        return item;
    }

    @Override
    public Item<I> prepareNetworkItem(Item<I> item, ItemBuildContext context, CompoundTag networkData) {
        if (VersionHelper.isOrAbove1_20_5()) {
            Tag previous = item.getSparrowNBTComponent(ComponentKeys.LORE);
            if (previous != null) {
                networkData.put(ComponentKeys.LORE.asString(), NetworkItemHandler.pack(NetworkItemHandler.Operation.ADD, previous));
            } else {
                networkData.put(ComponentKeys.LORE.asString(), NetworkItemHandler.pack(NetworkItemHandler.Operation.REMOVE));
            }
        } else {
            Tag previous = item.getTag("display", "Lore");
            if (previous != null) {
                networkData.put("display.Lore", NetworkItemHandler.pack(NetworkItemHandler.Operation.ADD, previous));
            } else {
                networkData.put("display.Lore", NetworkItemHandler.pack(NetworkItemHandler.Operation.REMOVE));
            }
        }
        return item;
    }
}
