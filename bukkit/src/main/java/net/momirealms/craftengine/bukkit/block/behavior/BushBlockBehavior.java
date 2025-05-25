package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.BlockTags;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.Tuple;
import net.momirealms.craftengine.shared.block.BlockBehavior;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.util.*;

public class BushBlockBehavior extends AbstractCanSurviveBlockBehavior {
    public static final Factory FACTORY = new Factory();
    protected final List<Object> tagsCanSurviveOn;
    protected final Set<Object> blocksCansSurviveOn;
    protected final Set<String> customBlocksCansSurviveOn;
    protected final boolean any;
    protected final boolean stackable;

    public BushBlockBehavior(CustomBlock block, boolean stackable, List<Object> tagsCanSurviveOn, Set<Object> blocksCansSurviveOn, Set<String> customBlocksCansSurviveOn) {
        super(block);
        this.stackable = stackable;
        this.tagsCanSurviveOn = tagsCanSurviveOn;
        this.blocksCansSurviveOn = blocksCansSurviveOn;
        this.customBlocksCansSurviveOn = customBlocksCansSurviveOn;
        this.any = this.tagsCanSurviveOn.isEmpty() && this.blocksCansSurviveOn.isEmpty() && this.customBlocksCansSurviveOn.isEmpty();
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Tuple<List<Object>, Set<Object>, Set<String>> tuple = readTagsAndState(arguments, false);
            boolean stackable = (boolean) arguments.getOrDefault("stackable", false);
            return new BushBlockBehavior(block, stackable, tuple.left(), tuple.mid(), tuple.right());
        }
    }

    public static Tuple<List<Object>, Set<Object>, Set<String>> readTagsAndState(Map<String, Object> arguments, boolean aboveOrBelow) {
        List<Object> mcTags = new ArrayList<>();
        for (String tag : MiscUtils.getAsStringList(arguments.getOrDefault((aboveOrBelow ? "above" : "bottom") + "-block-tags", List.of()))) {
            mcTags.add(BlockTags.getOrCreate(Key.of(tag)));
        }
        Set<Object> mcBlocks = new HashSet<>();
        Set<String> customBlocks = new HashSet<>();
        for (String blockStateStr : MiscUtils.getAsStringList(arguments.getOrDefault((aboveOrBelow ? "above" : "bottom") + "-blocks", List.of()))) {
            int index = blockStateStr.indexOf('[');
            Key blockType = index != -1 ? Key.from(blockStateStr.substring(0, index)) : Key.from(blockStateStr);
            Material material = Registry.MATERIAL.get(new NamespacedKey(blockType.namespace(), blockType.value()));
            if (material != null) {
                if (index == -1) {
                    // vanilla
                    mcBlocks.addAll(BlockStateUtils.getAllVanillaBlockStates(blockType));
                } else {
                    mcBlocks.add(BlockStateUtils.blockDataToBlockState(Bukkit.createBlockData(blockStateStr)));
                }
            } else {
                // custom maybe
                customBlocks.add(blockStateStr);
            }
        }
        return new Tuple<>(mcTags, mcBlocks, customBlocks);
    }

    @Override
    protected boolean canSurvive(Object thisBlock, Object state, Object world, Object blockPos) throws Exception {
        int y = FastNMS.INSTANCE.field$Vec3i$y(blockPos);
        int x = FastNMS.INSTANCE.field$Vec3i$x(blockPos);
        int z = FastNMS.INSTANCE.field$Vec3i$z(blockPos);
        Object belowPos = FastNMS.INSTANCE.constructor$BlockPos(x, y - 1, z);
        Object belowState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(world, belowPos);
        return mayPlaceOn(belowState, world, belowPos);
    }

    protected boolean mayPlaceOn(Object belowState, Object world, Object belowPos) throws ReflectiveOperationException {
        if (this.any) {
            return belowState != Reflections.instance$Blocks$AIR$defaultState;
        }
        for (Object tag : this.tagsCanSurviveOn) {
            if ((boolean) Reflections.method$BlockStateBase$hasTag.invoke(belowState, tag)) {
                return true;
            }
        }
        int id = BlockStateUtils.blockStateToId(belowState);
        if (BlockStateUtils.isVanillaBlock(id)) {
            if (!this.blocksCansSurviveOn.isEmpty() && this.blocksCansSurviveOn.contains(belowState)) {
                return true;
            }
        } else {
            ImmutableBlockState belowCustomState = BukkitBlockManager.instance().getImmutableBlockState(id);
            if (belowCustomState != null && !belowCustomState.isEmpty()) {
                if (stackable) {
                    if (belowCustomState.owner().value() == super.customBlock) {
                        return true;
                    }
                }
                if (this.customBlocksCansSurviveOn.contains(belowCustomState.owner().value().id().toString())) {
                    return true;
                }
                if (this.customBlocksCansSurviveOn.contains(belowCustomState.toString())) {
                    return true;
                }
            }
        }
        return false;
    }
}
