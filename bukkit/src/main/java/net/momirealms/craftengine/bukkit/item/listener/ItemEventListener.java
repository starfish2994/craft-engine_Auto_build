package net.momirealms.craftengine.bukkit.item.listener;

import net.momirealms.craftengine.bukkit.api.event.CustomBlockInteractEvent;
import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.*;
import net.momirealms.craftengine.bukkit.world.BukkitBlockInWorld;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.PlayerBlockActionContext;
import net.momirealms.craftengine.core.plugin.context.parameter.CommonParameters;
import net.momirealms.craftengine.core.plugin.event.EventTrigger;
import net.momirealms.craftengine.core.util.ClickType;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.world.BlockHitResult;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Vec3d;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ItemEventListener implements Listener {
    private final BukkitCraftEngine plugin;

    public ItemEventListener(BukkitCraftEngine plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInteractBlock(PlayerInteractEvent event) {
        Action action = event.getAction();
        Player player = event.getPlayer();
        if (
                (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) ||  /* block is required */
                (player.getGameMode() == GameMode.SPECTATOR) ||  /* no spectator interactions */
                (action == Action.LEFT_CLICK_BLOCK && player.getGameMode() == GameMode.CREATIVE) /* it's breaking the block */
        ) {
            return;
        }

        BukkitServerPlayer serverPlayer = this.plugin.adapt(player);
        InteractionHand hand = event.getHand() == EquipmentSlot.HAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        // 如果本tick内主手已被处理，则不处理副手
        // 这是因为客户端可能会同时发主副手交互包，但实际上只能处理其中一个
        if (this.cancelEventIfHasInteraction(event, serverPlayer, hand)) {
            return;
        }

        // some common data
        Block block = Objects.requireNonNull(event.getClickedBlock());
        BlockData blockData = block.getBlockData();
        Object blockState = BlockStateUtils.blockDataToBlockState(blockData);
        ImmutableBlockState immutableBlockState = null;
        int stateId = BlockStateUtils.blockStateToId(blockState);

        // 处理自定义方块
        if (!BlockStateUtils.isVanillaBlock(stateId)) {
            immutableBlockState = BukkitBlockManager.instance().getImmutableBlockStateUnsafe(stateId);
            // call the event if it's custom
            CustomBlockInteractEvent interactEvent = new CustomBlockInteractEvent(
                    player,
                    block.getLocation(),
                    event.getInteractionPoint(),
                    immutableBlockState,
                    block,
                    event.getBlockFace(),
                    hand,
                    action.isRightClick() ? CustomBlockInteractEvent.Action.RIGHT_CLICK : CustomBlockInteractEvent.Action.LEFT_CLICK,
                    event.getItem()
            );
            if (EventUtils.fireAndCheckCancel(interactEvent)) {
                event.setCancelled(true);
                return;
            }

            // run custom functions
            CustomBlock customBlock = immutableBlockState.owner().value();
            PlayerBlockActionContext context = PlayerBlockActionContext.of(serverPlayer, new BukkitBlockInWorld(block), ContextHolder.builder()
                    .withParameter(CommonParameters.BLOCK_STATE, immutableBlockState)
                    .withParameter(CommonParameters.PLAYER, serverPlayer)
                    .withParameter(CommonParameters.WORLD, serverPlayer.world())
                    .withParameter(CommonParameters.LOCATION, new Vec3d(block.getX(), block.getY(), block.getZ()))
                    .withParameter(CommonParameters.CLICK_TYPE, action.isRightClick() ? ClickType.RIGHT : ClickType.LEFT)
                    .build());
            customBlock.execute(context, EventTrigger.CLICK);
            if (action.isRightClick()) customBlock.execute(context, EventTrigger.RIGHT_CLICK);
            else customBlock.execute(context, EventTrigger.LEFT_CLICK);
        }

        Item<ItemStack> itemInHand = serverPlayer.getItemInHand(hand);
        Optional<CustomItem<ItemStack>> optionalCustomItem = itemInHand == null ? Optional.empty() : itemInHand.getCustomItem();
        boolean hasItem = itemInHand != null;
        boolean hasCustomItem = optionalCustomItem.isPresent();

        // interact block with items
        if (hasItem && action == Action.RIGHT_CLICK_BLOCK) {
            Location interactionPoint = Objects.requireNonNull(event.getInteractionPoint(), "interaction point should not be null");
            Direction direction = DirectionUtils.toDirection(event.getBlockFace());
            BlockPos pos = LocationUtils.toBlockPos(block.getLocation());
            Vec3d vec3d = new Vec3d(interactionPoint.getX(), interactionPoint.getY(), interactionPoint.getZ());
            BlockHitResult hitResult = new BlockHitResult(vec3d, direction, pos, false);

            // handle block item
            if (itemInHand.isBlockItem()) {
                // vanilla item
                if (!hasCustomItem) {
                    // interact a custom block
                    if (immutableBlockState != null) {
                        // client won't have sounds if the clientside block is interactable
                        // so we should check and resend sounds on BlockPlaceEvent
                        BlockData craftBlockData = BlockStateUtils.fromBlockData(immutableBlockState.vanillaBlockState().handle());
                        if (InteractUtils.isInteractable(player, craftBlockData, hitResult, itemInHand)) {
                            if (!serverPlayer.isSecondaryUseActive()) {
                                serverPlayer.setResendSound();
                            }
                        } else {
                            if (BlockStateUtils.isReplaceable(immutableBlockState.customBlockState().handle()) && !BlockStateUtils.isReplaceable(immutableBlockState.vanillaBlockState().handle())) {
                                serverPlayer.setResendSwing();
                            }
                        }
                    }
                }
                // custom item
                else {
                    if (optionalCustomItem.get().settings().canPlaceRelatedVanillaBlock()) {
                        // 如果用户设置了允许放置对应的原版方块，那么直接返回。
                        // 这种情况下最好是return，以避免同时触发多个behavior发生冲突
                        // 当用户选择其作为原版方块放下时，自定义行为可能已经不重要了？
                        return;
                    } else {
                        // todo 实际上这里的处理并不正确，因为判断玩家是否能够放置那个方块需要更加细节的判断。比如玩家无法对着树叶放置火把，但是交互事件依然触发，此情况下不可丢弃自定义行为。
                        if (serverPlayer.isSecondaryUseActive() || !InteractUtils.isInteractable(player, blockData, hitResult, itemInHand)) {
                            event.setCancelled(true);
                        }
                    }
                }
            }

            // 检查其他的物品行为，物品行为理论只在交互时处理
            Optional<List<ItemBehavior>> optionalItemBehaviors = itemInHand.getItemBehavior();
            // 物品类型是否包含自定义物品行为，行为不一定来自于自定义物品，部分原版物品也包含了新的行为
            if (optionalItemBehaviors.isPresent()) {
                // 检测是否可交互应当只判断原版方块，因为自定义方块早就判断过了，如果可交互不可能到这一步
                boolean interactable = immutableBlockState == null && InteractUtils.isInteractable(player, blockData, hitResult, itemInHand);
                // 如果方块可交互但是玩家没shift，那么原版的方块交互优先，取消自定义物品的behavior
                // todo 如果我的物品行为允许某些交互呢？是否值得进一步处理？
                if (!serverPlayer.isSecondaryUseActive() && interactable) {
                    return;
                }
                // 依次执行物品行为
                for (ItemBehavior itemBehavior : optionalItemBehaviors.get()) {
                    InteractionResult result = itemBehavior.useOnBlock(new UseOnContext(serverPlayer, hand, hitResult));
                    if (result == InteractionResult.SUCCESS_AND_CANCEL) {
                        event.setCancelled(true);
                        return;
                    }
                    // 非pass的情况直接结束
                    if (result != InteractionResult.PASS) {
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteractAir(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR)
            return;
        Player bukkitPlayer = event.getPlayer();
        BukkitServerPlayer player = this.plugin.adapt(bukkitPlayer);
        InteractionHand hand = event.getHand() == EquipmentSlot.HAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        if (cancelEventIfHasInteraction(event, player, hand)) {
            return;
        }
        if (player.isSpectatorMode()) {
            return;
        }

        // Gets the item in hand
        Item<ItemStack> itemInHand = player.getItemInHand(hand);
        // should never be null
        if (itemInHand == null) return;
        Optional<List<ItemBehavior>> optionalItemBehaviors = itemInHand.getItemBehavior();

        if (optionalItemBehaviors.isPresent()) {
            for (ItemBehavior itemBehavior : optionalItemBehaviors.get()) {
                InteractionResult result = itemBehavior.use(player.world(), player, hand);
                if (result == InteractionResult.SUCCESS_AND_CANCEL) {
                    event.setCancelled(true);
                    return;
                }
                if (result != InteractionResult.PASS) {
                    return;
                }
            }
        }
    }

    private boolean cancelEventIfHasInteraction(PlayerInteractEvent event, BukkitServerPlayer player, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND) {
            int currentTicks = player.gameTicks();
            // The client will send multiple packets to the server if the client thinks it should
            // However, if the main hand item interaction is successful, the off-hand item should be blocked.
            if (player.lastSuccessfulInteractionTick() == currentTicks) {
                event.setCancelled(true);
                return true;
            }
        }
        return false;
    }
}
