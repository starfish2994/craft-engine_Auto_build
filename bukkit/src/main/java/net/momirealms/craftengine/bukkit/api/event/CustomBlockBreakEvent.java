package net.momirealms.craftengine.bukkit.api.event;

import net.momirealms.craftengine.core.block.BreakReason;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.world.BlockPos;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomBlockBreakEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private boolean cancelled;
    private final CustomBlock block;
    private final ImmutableBlockState state;
    private final Location location;
    private final Player optionalPlayer;
    private final BreakReason reason;

    public CustomBlockBreakEvent(ImmutableBlockState state, Location location, BreakReason reason, Player optionalPlayer) {
        this.block = state.owner().value();
        this.state = state;
        this.location = location;
        this.optionalPlayer = optionalPlayer;
        this.reason = reason;
    }

    public CustomBlock block() {
        return this.block;
    }

    public BreakReason reason() {
        return this.reason;
    }

    public BlockPos blockPos() {
        return new BlockPos(this.location.getBlockX(), this.location.getBlockY(), this.location.getBlockZ());
    }

    public Location location() {
        return this.location;
    }

    @Nullable
    public Player optionalPlayer() {
        return this.optionalPlayer;
    }

    public ImmutableBlockState state() {
        return this.state;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}

