package net.momirealms.craftengine.core.item.recipe.network.legacy;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Obsolete
public class LegacyIngredient {
    private final Item<Object>[] items;

    public LegacyIngredient(Item<Object>[] items) {
        this.items = items;
    }

    public Item<?>[] items() {
        return items;
    }

    @SuppressWarnings("unchecked")
    public static LegacyIngredient read(FriendlyByteBuf buf) {
        Item<Object>[] items = buf.readArray(byteBuf -> CraftEngine.instance().itemManager().decode(byteBuf), Item.class);
        return new LegacyIngredient(items);
    }

    public void applyClientboundData(Player player) {
        for (int i = 0; i < items.length; i++) {
            Item<Object> item = items[i];
            this.items[i] = CraftEngine.instance().itemManager().s2c(item, player);
        }
    }
}
