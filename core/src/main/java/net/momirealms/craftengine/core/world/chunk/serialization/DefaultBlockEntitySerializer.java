package net.momirealms.craftengine.core.world.chunk.serialization;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DefaultBlockEntitySerializer {

    public static ListTag serialize(Map<BlockPos, BlockEntity> tiles) {
        ListTag result = new ListTag();
        for (Map.Entry<BlockPos, BlockEntity> entry : tiles.entrySet()) {
            result.add(entry.getValue().saveAsTag());
        }
        return result;
    }

    public static List<BlockEntity> deserialize(CEChunk chunk, ListTag tag) {
        List<BlockEntity> blockEntities = new ArrayList<>(tag.size());
        for (int i = 0; i < tag.size(); i++) {
            CompoundTag data = tag.getCompound(i);
            BlockPos pos = BlockEntity.readPosAndVerify(data, chunk.chunkPos());
            ImmutableBlockState blockState = chunk.getBlockState(pos);
        }
        return blockEntities;
    }
}
