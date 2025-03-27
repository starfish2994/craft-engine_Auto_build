package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.util.Tuple;
import net.momirealms.craftengine.shared.block.BlockBehavior;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class HangingBlockBehavior extends BushBlockBehavior {
    public static final Factory FACTORY = new Factory();

    public HangingBlockBehavior(List<Object> tagsCanSurviveOn, Set<Object> blocksCansSurviveOn, Set<String> customBlocksCansSurviveOn) {
        super(tagsCanSurviveOn, blocksCansSurviveOn, customBlocksCansSurviveOn);
    }

    @Override
    protected boolean canSurvive(Object thisBlock, Object state, Object world, Object blockPos) throws ReflectiveOperationException {
        int y = Reflections.field$Vec3i$y.getInt(blockPos);
        int x = Reflections.field$Vec3i$x.getInt(blockPos);
        int z = Reflections.field$Vec3i$z.getInt(blockPos);
        Object belowPos = Reflections.constructor$BlockPos.newInstance(x, y + 1, z);
        Object belowState = Reflections.method$BlockGetter$getBlockState.invoke(world, belowPos);
        return mayPlaceOn(belowState, world, belowPos);
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Tuple<List<Object>, Set<Object>, Set<String>> tuple = readTagsAndState(arguments);
            return new HangingBlockBehavior(tuple.left(), tuple.mid(), tuple.right());
        }
    }
}
