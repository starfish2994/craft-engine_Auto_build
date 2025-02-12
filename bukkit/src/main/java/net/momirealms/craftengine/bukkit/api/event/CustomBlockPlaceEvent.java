package net.momirealms.craftengine.bukkit.api.event;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CustomBlockPlaceEvent extends CustomBlockEvent {
    public CustomBlockPlaceEvent(ImmutableBlockState state, Location location, Player optionalPlayer) {
        super(state, location, optionalPlayer);
    }
}
