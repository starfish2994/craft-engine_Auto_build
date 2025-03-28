package net.momirealms.craftengine.bukkit.compatibility.slimeworld;

import com.infernalsuite.asp.api.world.SlimeChunk;
import com.infernalsuite.asp.api.world.SlimeWorld;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.chunk.storage.WorldDataStorage;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.NBT;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class SlimeWorldDataStorage implements WorldDataStorage {
    private final WeakReference<SlimeWorld> slimeWorld;

    public SlimeWorldDataStorage(SlimeWorld slimeWorld) {
        this.slimeWorld = new WeakReference<>(slimeWorld);
    }

    public SlimeWorld getWorld() {
        return slimeWorld.get();
    }

    @Nullable
    @Override
    public CompoundTag readChunkTagAt(ChunkPos pos) {
        SlimeChunk slimeChunk = getWorld().getChunk(pos.x, pos.z);
        if (slimeChunk == null) return null;
        BinaryTag tag = slimeChunk.getExtraData().get("craftengine");
        if (tag == null) return null;
        ByteArrayBinaryTag byteArrayBinaryTag = (ByteArrayBinaryTag) tag;
        try {
            return NBT.readCompound(new DataInputStream(new ByteArrayInputStream(byteArrayBinaryTag.value())));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read chunk tag from slime world. " + pos, e);
        }
    }

    @Override
    public void writeChunkTagAt(ChunkPos pos, @Nullable CompoundTag nbt) {
        SlimeChunk slimeChunk = getWorld().getChunk(pos.x, pos.z);
        if (slimeChunk == null) return;
        if (nbt == null) {
            slimeChunk.getExtraData().remove("craftengine");
        } else {
            slimeChunk.getExtraData().computeIfAbsent("craftengine", l -> {
                try {
                    return ByteArrayBinaryTag.byteArrayBinaryTag(NBT.toBytes(nbt));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to write chunk tag to slime world. " + pos, e);
                }
            });
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws IOException {
    }
}
