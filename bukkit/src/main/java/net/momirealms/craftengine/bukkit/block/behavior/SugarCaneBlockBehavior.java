package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateOption;
import net.momirealms.craftengine.core.block.properties.IntegerProperty;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.parameter.LootParameters;
import net.momirealms.craftengine.core.util.context.ContextHolder;
import net.momirealms.craftengine.core.world.Vec3d;
import org.bukkit.Material;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

public class SugarCaneBlockBehavior extends BushBlockBehavior {
    private final int maxHeight;
    private final boolean nearWater;
    private final boolean nearLava;
    private final IntegerProperty age;

    public SugarCaneBlockBehavior(List<Object> tagsCanSurviveOn, Set<Object> blocksCansSurviveOn, Set<String> customBlocksCansSurviveOn, IntegerProperty age, int maxHeight, boolean nearWater, boolean nearLava) {
        super(tagsCanSurviveOn, blocksCansSurviveOn, customBlocksCansSurviveOn);
        this.nearWater = nearWater;
        this.nearLava = nearLava;
        this.maxHeight = maxHeight;
        this.age = age;
    }

    @Override
    public void tick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object blockState = args[0];
        Object level = args[1];
        Object blockPos = args[2];
        ImmutableBlockState currentState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(blockState));
        if (currentState != null && !currentState.isEmpty()) {
            Reflections.method$Level$removeBlock.invoke(level, blockPos, false);
            Vec3d vec3d = Vec3d.atCenterOf(LocationUtils.fromBlockPos(blockPos));
            net.momirealms.craftengine.core.world.World world = new BukkitWorld(FastNMS.INSTANCE.method$Level$getCraftWorld(level));
            ContextHolder.Builder builder = ContextHolder.builder()
                    .withParameter(LootParameters.LOCATION, vec3d)
                    .withParameter(LootParameters.WORLD, world);
            for (Item<Object> item : currentState.getDrops(builder, world)) {
                world.dropItemNaturally(vec3d, item);
            }
        }
    }
}
