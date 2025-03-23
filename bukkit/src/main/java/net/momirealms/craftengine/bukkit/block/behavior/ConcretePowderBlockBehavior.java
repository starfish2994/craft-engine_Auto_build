package net.momirealms.craftengine.bukkit.block.behavior;

public class ConcretePowderBlockBehavior extends FallingBlockBehavior {

    public ConcretePowderBlockBehavior(float hurtAmount, int maxHurt) {
        super(hurtAmount, maxHurt);
    }

    @Override
    public void onLand(Object thisBlock, Object[] args) throws Exception {
        super.onLand(thisBlock, args);
    }
}
