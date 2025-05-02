package net.momirealms.craftengine.core.block;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.util.context.ContextHolder;
import net.momirealms.craftengine.core.util.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.world.BlockInWorld;
import org.jetbrains.annotations.NotNull;

public class PlayerBlockActionContext extends PlayerOptionalContext {
    private BlockInWorld block;

    public PlayerBlockActionContext(@NotNull Player player, @NotNull ContextHolder contexts) {
        super(player, contexts);
    }
}
