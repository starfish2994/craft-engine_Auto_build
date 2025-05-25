package net.momirealms.craftengine.bukkit.block;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.shared.block.BlockShape;

public class BukkitBlockShape implements BlockShape {
    private final Object rawBlockState;

    public BukkitBlockShape(Object rawBlockState) {
        this.rawBlockState = rawBlockState;
    }

    @Override
    public Object getShape(Object thisObj, Object[] args) {
        return FastNMS.INSTANCE.method$BlockState$getShape(this.rawBlockState, args[1], args[2], args[3]);
    }

    @Override
    public Object getCollisionShape(Object thisObj, Object[] args) {
        return FastNMS.INSTANCE.method$BlockState$getCollisionShape(this.rawBlockState, args[1], args[2], args[3]);
    }
}
