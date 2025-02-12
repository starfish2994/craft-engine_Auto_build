package net.momirealms.craftengine.bukkit.api.event;

import net.momirealms.craftengine.bukkit.entity.furniture.LoadedFurniture;
import org.bukkit.entity.Player;

public class FurnitureBreakEvent extends FurnitureEvent {
    public FurnitureBreakEvent(LoadedFurniture furniture, Player player) {
        super(furniture, player);
    }
}
