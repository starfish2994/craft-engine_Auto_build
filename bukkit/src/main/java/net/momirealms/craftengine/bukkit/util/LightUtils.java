package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import org.bukkit.World;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

public class LightUtils {

    private LightUtils() {}

    public static void updateChunkLight(World world, Map<Long, BitSet> sectionPosSet) {
        try {
            Object serverLevel = FastNMS.INSTANCE.field$CraftWorld$ServerLevel(world);
            Object chunkSource = FastNMS.INSTANCE.method$ServerLevel$getChunkSource(serverLevel);
            for (Map.Entry<Long, BitSet> entry : sectionPosSet.entrySet()) {
                long chunkKey = entry.getKey();
                Object chunkHolder = FastNMS.INSTANCE.method$ServerChunkCache$getVisibleChunkIfPresent(chunkSource, chunkKey);
                if (chunkHolder == null) continue;
                List<Object> players = FastNMS.INSTANCE.method$ChunkHolder$getPlayers(chunkHolder);
                if (players.isEmpty()) continue;
                Object lightEngine = Reflections.field$ChunkHolder$lightEngine.get(chunkHolder);
                BitSet blockChangedLightSectionFilter = (BitSet) Reflections.field$ChunkHolder$blockChangedLightSectionFilter.get(chunkHolder);
                blockChangedLightSectionFilter.or(entry.getValue());
                BitSet skyChangedLightSectionFilter = (BitSet) Reflections.field$ChunkHolder$skyChangedLightSectionFilter.get(chunkHolder);
                Object chunkPos = FastNMS.INSTANCE.constructor$ChunkPos((int) chunkKey, (int) (chunkKey >> 32));
                Object lightPacket = FastNMS.INSTANCE.constructor$ClientboundLightUpdatePacket(chunkPos, lightEngine, skyChangedLightSectionFilter, blockChangedLightSectionFilter);
                for (Object player : players) {
                    FastNMS.INSTANCE.sendPacket(player, lightPacket);
                }
                blockChangedLightSectionFilter.clear();
                skyChangedLightSectionFilter.clear();
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Could not update light for world " + world.getName());
        }
    }
}
