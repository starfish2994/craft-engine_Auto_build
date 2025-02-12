package net.momirealms.craftengine.bukkit.api.event;

import net.momirealms.craftengine.bukkit.entity.furniture.LoadedFurniture;
import org.bukkit.entity.Player;

public class FurniturePlaceEvent extends FurnitureEvent {
    public FurniturePlaceEvent(LoadedFurniture furniture, Player player) {
        super(furniture, player);
    }
}
