package net.momirealms.craftengine.bukkit.item.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateOption;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorFactory;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.BlockPos;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

import java.nio.file.Path;
import java.util.Map;

public class BucketItemBehavior extends ItemBehavior {
    public static final BucketItemBehavior INSTANCE = new BucketItemBehavior();
    public static final Factory FACTORY = new Factory();

    @Override
    public InteractionResult useOnBlock(UseOnContext context) {
        return use(context.getLevel(), context.getPlayer(), context.getHand());
    }

    @Override
    public InteractionResult use(net.momirealms.craftengine.core.world.World world, net.momirealms.craftengine.core.entity.player.Player player, InteractionHand hand) {
        if (player.isAdventureMode()) return InteractionResult.PASS;
        Player bukkitPlayer = (Player) player.platformPlayer();
        RayTraceResult result = bukkitPlayer.rayTraceBlocks(player.getCachedInteractionRange(), FluidCollisionMode.SOURCE_ONLY);
        if (result == null) return InteractionResult.PASS;
        Block block = result.getHitBlock();
        if (block == null) return InteractionResult.PASS;
        return tryFillBucket(world, player, hand, new BlockPos(block.getX(), block.getY(), block.getZ()));
    }

    @SuppressWarnings("unchecked")
    private InteractionResult tryFillBucket(net.momirealms.craftengine.core.world.World world,
                                            net.momirealms.craftengine.core.entity.player.Player player,
                                            InteractionHand hand,
                                            BlockPos pos) {
        Object nmsPos = LocationUtils.toBlockPos(pos.x(), pos.y(), pos.z());
        Object blockState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(world.serverWorld(), nmsPos);
        ImmutableBlockState customState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(blockState));
        if (customState == null || customState.isEmpty()) return InteractionResult.PASS;
        CustomBlock customBlock = customState.owner().value();
        Property<Boolean> waterlogged = (Property<Boolean>) customBlock.getProperty("waterlogged");
        if (waterlogged == null) return InteractionResult.PASS;
        boolean waterloggedState = customState.get(waterlogged);
        if (!waterloggedState) return InteractionResult.PASS;
        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;
        FastNMS.INSTANCE.method$LevelWriter$setBlock(world.serverWorld(), nmsPos, customState.with(waterlogged, false).customBlockState().handle(), UpdateOption.UPDATE_ALL.flags());
        Player bukkitPlayer = (Player) player.platformPlayer();
        if (player.isSurvivalMode()) {
            // to prevent dupe in moment
            bukkitPlayer.getInventory().setItem(slot, new ItemStack(Material.AIR));
            if (VersionHelper.isFolia()) {
                bukkitPlayer.getScheduler().run(BukkitCraftEngine.instance().javaPlugin(), (t) -> bukkitPlayer.getInventory().setItem(slot, new ItemStack(Material.WATER_BUCKET)), () -> {});
            } else {
                BukkitCraftEngine.instance().scheduler().sync().runDelayed(() -> bukkitPlayer.getInventory().setItem(slot, new ItemStack(Material.WATER_BUCKET)));
            }
        }
        bukkitPlayer.setStatistic(Statistic.USE_ITEM, Material.BUCKET, bukkitPlayer.getStatistic(Statistic.USE_ITEM, Material.BUCKET) + 1);
        // client will assume it has sounds
        // context.getPlayer().level().playBlockSound(Vec3d.atCenterOf(context.getClickedPos()), ITEM_BUCKET_FILL, 1, 1);
        return InteractionResult.SUCCESS_AND_CANCEL;
    }

    public static class Factory implements ItemBehaviorFactory {
        @Override
        public ItemBehavior create(Pack pack, Path path, Key id, Map<String, Object> arguments) {
            return INSTANCE;
        }
    }
}
