package net.momirealms.craftengine.core.item.modifier.lore;

import net.momirealms.craftengine.core.item.ComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.NetworkItemHandler;
import net.momirealms.craftengine.core.item.modifier.ItemDataModifier;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.Tag;

import java.util.Map;
import java.util.Optional;

public final class DynamicLoreModifier<I> implements ItemDataModifier<I> {
    public static final String CONTEXT_TAG_KEY = "craftengine:display_context";
    private final Map<String, LoreModifier<I>> displayContexts;
    private final LoreModifier<I> defaultModifier;

    public DynamicLoreModifier(Map<String, LoreModifier<I>> displayContexts) {
        this.displayContexts = displayContexts;
        this.defaultModifier = displayContexts.values().iterator().next();
    }

    public Map<String, LoreModifier<I>> displayContexts() {
        return displayContexts;
    }

    @Override
    public String name() {
        return "dynamic-lore";
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        String displayContext = Optional.ofNullable(item.getJavaTag(CONTEXT_TAG_KEY)).orElse(this.defaultModifier).toString();
        LoreModifier<I> lore = this.displayContexts.get(displayContext);
        if (lore == null) {
            lore = this.defaultModifier;
        }
        return lore.apply(item, context);
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
