package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.item.modifier.ItemDataModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record NetworkItemDataProcessor<I>(@Nullable ItemDataModifier<I> server, @NotNull ItemDataModifier<I> client) {

    public static <I> NetworkItemDataProcessor<I> clientOnly(ItemDataModifier<I> client) {
        return new NetworkItemDataProcessor<>(null, client);
    }

    public static <I> NetworkItemDataProcessor<I> both(ItemDataModifier<I> server, ItemDataModifier<I> client) {
        return new NetworkItemDataProcessor<>(server, client);
    }

    public void toClient(Item<I> item, ItemBuildContext context) {
        this.client.apply(item, context);
    }

    public void toServer(Item<I> item, ItemBuildContext context) {
        this.client.remove(item);
        if (this.server != null) {
            this.server.apply(item, context);
        }
    }
}
