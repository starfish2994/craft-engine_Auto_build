package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.util.context.ContextHolder;
import net.momirealms.craftengine.core.util.context.PlayerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemBuildContext extends PlayerContext {
    public static final ItemBuildContext EMPTY = new ItemBuildContext(null, ContextHolder.EMPTY);

    public ItemBuildContext(@Nullable Player player, @NotNull ContextHolder contexts) {
        super(player, contexts);
    }

    @NotNull
    public static ItemBuildContext of(@Nullable Player player, @NotNull ContextHolder contexts) {
        return new ItemBuildContext(player, contexts);
    }
}
