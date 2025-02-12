package net.momirealms.craftengine.bukkit.api.event;

import net.momirealms.craftengine.bukkit.entity.furniture.LoadedFurniture;
import org.bukkit.entity.Player;

public class FurnitureInteractEvent extends FurnitureEvent {
    public FurnitureInteractEvent(LoadedFurniture furniture, Player player) {
        super(furniture, player);
    }
}
