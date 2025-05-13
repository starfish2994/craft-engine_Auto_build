package net.momirealms.craftengine.core.plugin.context.parameter;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.context.ContextKey;

public final class ChainContextParameters {
    private ChainContextParameters() {}

    public static final ContextKey<Item<?>> PLAYER_MAIN_HAND_ITEM = ContextKey.chain("player.main_hand_item");
}
