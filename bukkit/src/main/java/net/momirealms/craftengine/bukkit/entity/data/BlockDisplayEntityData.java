package net.momirealms.craftengine.bukkit.entity.data;

import net.momirealms.craftengine.bukkit.util.Reflections;

public class BlockDisplayEntityData<T> extends DisplayEntityData<T> {
    // Block display only
    public static final DisplayEntityData<Object> DisplayedBlock = of(23, EntityDataValue.Serializers$BLOCK_STATE, Reflections.instance$Blocks$AIR$defaultState);

    public BlockDisplayEntityData(int id, Object serializer, T defaultValue) {
        super(id, serializer, defaultValue);
    }
}
