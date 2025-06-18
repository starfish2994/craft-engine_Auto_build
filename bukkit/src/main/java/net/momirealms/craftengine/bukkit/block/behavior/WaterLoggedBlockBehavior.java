package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MFluids;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MItems;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.Callable;

public class WaterLoggedBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();
    @Nullable
    protected final Property<Boolean> waterloggedProperty;

    public WaterLoggedBlockBehavior(CustomBlock block, @Nullable Property<Boolean> waterloggedProperty) {
        super(block);
        this.waterloggedProperty = waterloggedProperty;
    }

    @Override
    public Object pickupBlock(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        if (this.waterloggedProperty == null) return CoreReflections.instance$ItemStack$EMPTY;
        Object blockState;
        Object world;
        Object pos;
        if (VersionHelper.isOrAbove1_20_2()) {
            world = args[1];
            pos = args[2];
            blockState = args[3];
        } else {
            world = args[0];
            pos = args[1];
            blockState = args[2];
        }
        ImmutableBlockState immutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(blockState));
        if (immutableBlockState != null) {
            if (immutableBlockState.get(this.waterloggedProperty)) {
                FastNMS.INSTANCE.method$LevelWriter$setBlock(world, pos, immutableBlockState.with(this.waterloggedProperty, false).customBlockState().handle(), 3);
                return FastNMS.INSTANCE.constructor$ItemStack(MItems.WATER_BUCKET, 1);
            }
        }
        return CoreReflections.instance$ItemStack$EMPTY;
    }

    @Override
    public boolean placeLiquid(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        if (this.waterloggedProperty == null) return false;
        Object blockState = args[2];
        ImmutableBlockState immutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(blockState));
        if (immutableBlockState != null) {
            Object fluidType = FastNMS.INSTANCE.method$FluidState$getType(args[3]);
            if (!immutableBlockState.get(this.waterloggedProperty) && fluidType == MFluids.WATER) {
                FastNMS.INSTANCE.method$LevelWriter$setBlock(args[0], args[1], immutableBlockState.with(this.waterloggedProperty, true).customBlockState().handle(), 3);
                FastNMS.INSTANCE.method$LevelAccessor$scheduleFluidTick(args[0], args[1], fluidType, 5);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canPlaceLiquid(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        if (this.waterloggedProperty == null) return false;
        return (VersionHelper.isOrAbove1_20_2() ? args[4] : args[3]) == MFluids.WATER;
    }

    @SuppressWarnings("unchecked")
    public static class Factory implements BlockBehaviorFactory {
        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Property<Boolean> waterlogged = (Property<Boolean>) block.getProperty("waterlogged");
            return new WaterLoggedBlockBehavior(block, waterlogged);
        }
    }
}
