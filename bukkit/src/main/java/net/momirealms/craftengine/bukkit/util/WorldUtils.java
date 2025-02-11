package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.core.world.SectionPos;
import org.bukkit.World;

public class WorldUtils {

    public static int getSectionCount(World world) {
        int min = SectionPos.blockToSectionCoord(world.getMinHeight());
        int max = SectionPos.blockToSectionCoord(world.getMaxHeight() - 1) + 1;
        return max - min;
    }
}
