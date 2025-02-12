package net.momirealms.craftengine.bukkit.api.event;

import net.momirealms.craftengine.bukkit.entity.furniture.LoadedFurniture;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FurniturePlaceEndEvent extends FurnitureEvent {
    public FurniturePlaceEndEvent(@Nullable LoadedFurniture furniture, @NotNull Player player) {
        super(furniture, player);
    }
}
