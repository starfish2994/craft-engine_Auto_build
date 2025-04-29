package net.momirealms.craftengine.core.world.chunk.serialization;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.momirealms.craftengine.core.block.BlockEntityState;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.ListTag;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

public final class DefaultBlockEntitySerializer {

    @ApiStatus.Experimental
    public static ListTag serialize(Map<Integer, BlockEntityState> tiles) {
        ListTag result = new ListTag();
        Map<CompoundTag, int[]> nbtToPosMap = new Object2ObjectOpenHashMap<>(Math.max(tiles.size(), 10), 0.75f);
        for (Map.Entry<Integer, BlockEntityState> entry : tiles.entrySet()) {
            int pos = entry.getKey();
            CompoundTag tag = entry.getValue().nbt();
            int[] previous = nbtToPosMap.computeIfAbsent(tag, k -> new int[] {pos});
            int[] newPoses = new int[previous.length + 1];
            System.arraycopy(previous, 0, newPoses, 0, previous.length);
            newPoses[previous.length] = pos;
            nbtToPosMap.put(tag, newPoses);
        }
        for (Map.Entry<CompoundTag, int[]> entry : nbtToPosMap.entrySet()) {
            CompoundTag blockEntityTag = new CompoundTag();
            blockEntityTag.put("data", entry.getKey());
            blockEntityTag.putIntArray("pos", entry.getValue());
            result.add(blockEntityTag);
        }
        return result;
    }

    @ApiStatus.Experimental
    public static Map<Integer, BlockEntityState> deserialize(ListTag tag) {
        Map<Integer, BlockEntityState> result = new Object2ObjectOpenHashMap<>(Math.max(tag.size(), 16), 0.5f);
        for (int i = 0; i < tag.size(); i++) {
            CompoundTag blockEntityTag = tag.getCompound(i);
            CompoundTag data = blockEntityTag.getCompound("data");
            int[] pos = blockEntityTag.getIntArray("pos");
            for (int j = 0; j < pos.length; j++) {
                result.put(j, new BlockEntityState(data));
            }
        }
        return result;
    }
}
