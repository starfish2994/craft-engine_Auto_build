package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.parameter.LootParameters;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.util.context.ContextHolder;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.shared.block.BlockBehavior;
import org.bukkit.World;

import java.util.Map;
import java.util.concurrent.Callable;

public class FallingBlockBehavior extends BlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final float hurtAmount;
    private final int maxHurt;

    public FallingBlockBehavior(float hurtAmount, int maxHurt) {
        this.hurtAmount = hurtAmount;
        this.maxHurt = maxHurt;
    }

    @Override
    public void onPlace(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object world = args[1];
        Object blockPos = args[2];
        Reflections.method$LevelAccessor$scheduleTick.invoke(world, blockPos, thisBlock, 2);
    }

    @Override
    public Object updateShape(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object world;
        Object blockPos;
        if (VersionHelper.isVersionNewerThan1_21_2()) {
            world = args[1];
            blockPos = args[3];
        } else {
            world = args[3];
            blockPos = args[4];
        }
        Reflections.method$LevelAccessor$scheduleTick.invoke(world, blockPos, thisBlock, 2);
        return super.updateShape(thisBlock, args, superMethod);
    }

    @Override
    public void tick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object blockPos = args[2];
        int y = Reflections.field$Vec3i$y.getInt(blockPos);
        Object world = args[1];
        Object dimension = Reflections.method$$LevelReader$dimensionType.invoke(world);
        int minY = Reflections.field$DimensionType$minY.getInt(dimension);
        if (y < minY) {
            return;
        }
        int x = Reflections.field$Vec3i$x.getInt(blockPos);
        int z = Reflections.field$Vec3i$z.getInt(blockPos);
        Object belowPos = Reflections.constructor$BlockPos.newInstance(x, y - 1, z);
        Object belowState = Reflections.method$BlockGetter$getBlockState.invoke(world, belowPos);
        boolean isFree = (boolean) Reflections.method$FallingBlock$isFree.invoke(null, belowState);
        if (!isFree) {
            return;
        }
        Object blockState = args[0];
        Object fallingBlockEntity = Reflections.method$FallingBlockEntity$fall.invoke(null, world, blockPos, blockState);
        if (this.hurtAmount > 0 && this.maxHurt > 0) {
            Reflections.method$FallingBlockEntity$setHurtsEntities.invoke(fallingBlockEntity, this.hurtAmount, this.maxHurt);
        }
    }

    @Override
    public void onBrokenAfterFall(Object thisBlock, Object[] args) throws Exception {
        // Use EntityRemoveEvent for 1.20.3+
        if (VersionHelper.isVersionNewerThan1_20_3()) return;
        Object level = args[0];
        Object fallingBlockEntity = args[2];
        boolean cancelDrop = (boolean) Reflections.field$FallingBlockEntity$cancelDrop.get(fallingBlockEntity);
        if (cancelDrop) return;
        Object blockState = Reflections.field$FallingBlockEntity$blockState.get(fallingBlockEntity);
        int stateId = BlockStateUtils.blockStateToId(blockState);
        ImmutableBlockState immutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(stateId);
        if (immutableBlockState == null || immutableBlockState.isEmpty()) return;
        ContextHolder.Builder builder = ContextHolder.builder();
        builder.withParameter(LootParameters.FALLING_BLOCK, true);
        double x = Reflections.field$Entity$xo.getDouble(fallingBlockEntity);
        double y = Reflections.field$Entity$yo.getDouble(fallingBlockEntity);
        double z = Reflections.field$Entity$zo.getDouble(fallingBlockEntity);
        Vec3d vec3d = new Vec3d(x, y, z);
        net.momirealms.craftengine.core.world.World world = new BukkitWorld((World) Reflections.method$Level$getCraftWorld.invoke(level));
        builder.withParameter(LootParameters.LOCATION, vec3d);
        builder.withParameter(LootParameters.WORLD, world);
        for (Item<Object> item : immutableBlockState.getDrops(builder, world)) {
            world.dropItemNaturally(vec3d, item);
        }
        Object entityData = Reflections.field$Entity$entityData.get(fallingBlockEntity);
        boolean isSilent = (boolean) Reflections.method$SynchedEntityData$get.invoke(entityData, Reflections.instance$Entity$DATA_SILENT);
        if (!isSilent) {
            world.playBlockSound(vec3d, immutableBlockState.sounds().destroySound());
        }
    }

    @Override
    public void onLand(Object thisBlock, Object[] args) throws Exception {
        Object fallingBlock = args[4];
        Object entityData = Reflections.field$Entity$entityData.get(fallingBlock);
        boolean isSilent = (boolean) Reflections.method$SynchedEntityData$get.invoke(entityData, Reflections.instance$Entity$DATA_SILENT);
        if (!isSilent) {
            Object blockState = args[2];
            int stateId = BlockStateUtils.blockStateToId(blockState);
            ImmutableBlockState immutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(stateId);
            if (immutableBlockState == null || immutableBlockState.isEmpty()) return;
            Object level = args[0];
            Object pos = args[1];
            net.momirealms.craftengine.core.world.World world = new BukkitWorld((World) Reflections.method$Level$getCraftWorld.invoke(level));
            world.playBlockSound(Vec3d.atCenterOf(LocationUtils.fromBlockPos(pos)), immutableBlockState.sounds().landSound());
        }
    }

    public static class Factory implements BlockBehaviorFactory {
        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            float hurtAmount = MiscUtils.getAsFloat(arguments.getOrDefault("hurt-amount", -1f));
            int hurtMax = MiscUtils.getAsInt(arguments.getOrDefault("max-hurt", -1));
            return new FallingBlockBehavior(hurtAmount, hurtMax);
        }
    }
}
