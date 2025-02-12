package net.momirealms.craftengine.bukkit.api.event;

import net.momirealms.craftengine.bukkit.entity.furniture.LoadedFurniture;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FurniturePlaceEndEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    private final LoadedFurniture furniture;
    private final Player player;

    public FurniturePlaceEndEvent(LoadedFurniture furniture, @NotNull Player player) {
        this.furniture = furniture;
        this.player = player;
    }

    public @NotNull LoadedFurniture furniture() {
        return this.furniture;
    }

    public @NotNull Player player() {
        return this.player;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }
}
