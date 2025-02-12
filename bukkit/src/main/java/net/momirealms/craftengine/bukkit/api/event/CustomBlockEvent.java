package net.momirealms.craftengine.bukkit.api.event;

import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.world.BlockPos;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CustomBlockEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private boolean cancelled;
    private final CustomBlock block;
    private final ImmutableBlockState state;
    private final Location location;
    private final Player player;

    public CustomBlockEvent(ImmutableBlockState state, Location location, @NotNull Player player) {
        this.block = state.owner().value();
        this.state = state;
        this.location = location;
        this.player = player;
    }

    public CustomBlock block() {
        return this.block;
    }

    public BlockPos blockPos() {
        return new BlockPos(this.location.getBlockX(), this.location.getBlockY(), this.location.getBlockZ());
    }

    public Location location() {
        return this.location;
    }

    public @NotNull Player player() {
        return this.player;
    }

    public ImmutableBlockState state() {
        return this.state;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
