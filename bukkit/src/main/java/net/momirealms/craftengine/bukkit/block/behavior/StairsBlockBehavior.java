package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;

import java.util.Map;

public class StairsBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();

    public StairsBlockBehavior(CustomBlock block) {
        super(block);
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            return new StairsBlockBehavior(block);
        }
    }
}
