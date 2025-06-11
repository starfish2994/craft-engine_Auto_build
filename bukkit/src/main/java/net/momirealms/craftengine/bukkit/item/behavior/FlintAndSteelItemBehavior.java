package net.momirealms.craftengine.bukkit.item.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.bukkit.util.InteractUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.world.BukkitBlockInWorld;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorFactory;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.sound.SoundSource;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.RandomUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.nio.file.Path;
import java.util.Map;

public class FlintAndSteelItemBehavior extends ItemBehavior {
    public static final FlintAndSteelItemBehavior INSTANCE = new FlintAndSteelItemBehavior();
    public static final Factory FACTORY = new Factory();
    private static final Key FLINT_SOUND = Key.of("item.flintandsteel.use");

    @SuppressWarnings("unchecked")
    @Override
    public InteractionResult useOnBlock(UseOnContext context) {
        BlockPos clickedPos = context.getClickedPos();
        BukkitBlockInWorld clicked = (BukkitBlockInWorld) context.getLevel().getBlockAt(clickedPos);
        Block block = clicked.block();
        BlockPos firePos = clickedPos.relative(context.getClickedFace());
        Direction direction = context.getHorizontalDirection();

        // 最基础的判断能不能着火，不能着火都是扯蛋
        try {
            if (!(boolean) CoreReflections.method$BaseFireBlock$canBePlacedAt.invoke(null, context.getLevel().serverWorld(), LocationUtils.toBlockPos(firePos), DirectionUtils.toNMSDirection(direction))) {
                return InteractionResult.PASS;
            }
        } catch (ReflectiveOperationException e) {
            CraftEngine.instance().logger().warn("Failed to run BaseFireBlock$canBePlacedAt", e);
            return InteractionResult.PASS;
        }

        net.momirealms.craftengine.core.entity.player.Player player = context.getPlayer();
        ImmutableBlockState state = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockDataToId(block.getBlockData()));
        if (state == null || state.isEmpty()) {
            return InteractionResult.PASS;
        } else {
            // 玩家交互目标是自定义方块
            if (context.getClickedFace() == Direction.UP) {
                // 客户端层面必须可交互
                if (!InteractUtils.isInteractable((Player) player.platformPlayer(), BlockStateUtils.fromBlockData(state.vanillaBlockState().handle()), context.getHitResult(), (Item<ItemStack>) context.getItem())) {
                    return InteractionResult.PASS;
                }
                // 且没有shift
                if (!player.isSecondaryUseActive()) {
                    player.playSound(FLINT_SOUND, SoundSource.BLOCK, 1f, RandomUtils.generateRandomFloat(0.8f, 1.2f));
                }
            } else {
                BlockData vanillaBlockState = BlockStateUtils.fromBlockData(state.vanillaBlockState().handle());
                // 原版状态可燃烧，则跳过
                if (BlockStateUtils.isBurnable(BlockStateUtils.blockDataToBlockState(vanillaBlockState))) {
                    return InteractionResult.PASS;
                }

                // 客户端一定觉得这个东西不可燃烧
                BlockPos belowFirePos = firePos.relative(Direction.DOWN);
                BukkitBlockInWorld belowFireBlock = (BukkitBlockInWorld) context.getLevel().getBlockAt(belowFirePos);
                boolean belowCanBurn;
                try {
                    Block belowBlock = belowFireBlock.block();
                    belowCanBurn = BlockStateUtils.isBurnable(BlockStateUtils.blockDataToBlockState(belowBlock.getBlockData())) ||
                            (boolean) CoreReflections.method$BlockStateBase$isFaceSturdy.invoke(
                                    BlockStateUtils.blockDataToBlockState(belowFireBlock.block().getBlockData()), context.getLevel().serverWorld(), LocationUtils.toBlockPos(belowFirePos), CoreReflections.instance$Direction$UP, CoreReflections.instance$SupportType$FULL);
                } catch (ReflectiveOperationException e) {
                    CraftEngine.instance().logger().warn("Failed to call method$BlockStateBase$isFaceSturdy", e);
                    return InteractionResult.PASS;
                }

                // 客户端觉得这玩意可交互，就会忽略声音
                if (InteractUtils.isInteractable((Player) player.platformPlayer(), vanillaBlockState, context.getHitResult(), (Item<ItemStack>) context.getItem())) {
                    // 如果按住了shift，则不会挥手，补发
                    if (player.isSecondaryUseActive()) {
                        // 如果底部不能燃烧，则燃烧点位为侧面，需要补发
                        if (!belowCanBurn) {
                            player.playSound(FLINT_SOUND, SoundSource.BLOCK, 1f, RandomUtils.generateRandomFloat(0.8f, 1.2f));
                            player.swingHand(context.getHand());
                        }
                    } else {
                        player.playSound(FLINT_SOUND, SoundSource.BLOCK, 1f, RandomUtils.generateRandomFloat(0.8f, 1.2f));
                    }
                } else {
                    if (!belowCanBurn) {
                        player.playSound(FLINT_SOUND, SoundSource.BLOCK, 1f, RandomUtils.generateRandomFloat(0.8f, 1.2f));
                        player.swingHand(context.getHand());
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    public static class Factory implements ItemBehaviorFactory {
        @Override
        public ItemBehavior create(Pack pack, Path path, Key id, Map<String, Object> arguments) {
            return INSTANCE;
        }
    }
}
