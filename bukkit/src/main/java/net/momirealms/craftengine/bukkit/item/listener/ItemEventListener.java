package net.momirealms.craftengine.bukkit.item.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.momirealms.craftengine.bukkit.api.event.CustomBlockInteractEvent;
import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.item.behavior.BlockItemBehavior;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.*;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.world.BlockHitResult;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Vec3d;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInteractBlock(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = Objects.requireNonNull(event.getClickedBlock());
        Object blockState = BlockStateUtils.blockDataToBlockState(block.getBlockData());
        int stateId = BlockStateUtils.blockStateToId(blockState);
        if (BlockStateUtils.isVanillaBlock(stateId)) {
            return;
        }

        // it's breaking the block
        if (action == Action.LEFT_CLICK_BLOCK && event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        CustomBlockInteractEvent interactEvent = new CustomBlockInteractEvent(
                event.getPlayer(),
                block.getLocation(),
                event.getInteractionPoint(),
                BukkitBlockManager.instance().getImmutableBlockStateUnsafe(stateId),
                block,
                event.getBlockFace(),
                event.getHand() == EquipmentSlot.HAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                action.isRightClick() ? CustomBlockInteractEvent.Action.RIGHT_CLICK : CustomBlockInteractEvent.Action.LEFT_CLICK,
                event.getItem()
        );
        if (EventUtils.fireAndCheckCancel(interactEvent)) {
            event.setCancelled(true);
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractAtBlock(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.useItemInHand() == Event.Result.DENY || event.useInteractedBlock() == Event.Result.DENY) return;
        Location interactionPoint = event.getInteractionPoint();
        if (interactionPoint == null) return;
        Player bukkitPlayer = event.getPlayer();
        Block clickedBlock = Objects.requireNonNull(event.getClickedBlock());
        BukkitServerPlayer player = this.plugin.adapt(bukkitPlayer);
        InteractionHand hand = event.getHand() == EquipmentSlot.HAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        // 如果同一tick已经处理过交互，则忽略
        if (cancelEventIfHasInteraction(event, player, hand)) {
            return;
        }

        // Gets the item in hand
        Item<ItemStack> itemInHand = player.getItemInHand(hand);
        if (itemInHand == null) return;
        Optional<List<ItemBehavior>> optionalItemBehaviors = itemInHand.getItemBehavior();

        // has custom item behavior
        // 物品类型是否包含自定义物品行为，行为不一定来自于自定义物品，部分原版物品也包含了新的行为
        if (optionalItemBehaviors.isPresent()) {
            BlockPos pos = LocationUtils.toBlockPos(clickedBlock.getLocation());
            Vec3d vec3d = new Vec3d(interactionPoint.getX(), interactionPoint.getY(), interactionPoint.getZ());
            Direction direction = DirectionUtils.toDirection(event.getBlockFace());
            BlockHitResult hitResult = new BlockHitResult(vec3d, direction, pos, false);
            boolean interactable = InteractUtils.isInteractable(BlockStateUtils.getBlockOwnerId(clickedBlock), bukkitPlayer, clickedBlock.getBlockData(), hitResult, itemInHand);

            // do not allow to place block if it's a vanilla block
            // 如果这个是自定义物品，那么会阻止玩家放置其对应的原版方块
            Optional<CustomItem<ItemStack>> optionalCustomItem = itemInHand.getCustomItem();
            if (itemInHand.isBlockItem() && optionalCustomItem.isPresent()) {
                // it's a custom item, but now it's ignored
                // 如果用户设置了允许放置对应的原版方块，那么直接返回。
                // todo 实际上这里的处理并不正确，因为判断玩家是否能够放置那个方块需要更加细节的判断。比如玩家无法对着树叶放置火把，但是交互事件依然触发，此情况下不可丢弃自定义行为。
                if (optionalCustomItem.get().settings().canPlaceRelatedVanillaBlock()) {
                    return;
                }
                // 如果玩家潜行放置或者交互对象不可交互，那么取消掉事件以防止玩家放置。
                // todo 这些处理应该要搬到BlockPlaceEvent?
                if (!interactable || player.isSecondaryUseActive()) {
                    event.setCancelled(true);
                }
            }

            if (!player.isSecondaryUseActive() && interactable) {
                // if it's interactable on server, cancel the custom behaviors
                return;
            }

            // no spectator interactions
            if (player.isSpectatorMode()) {
                return;
            }

            for (ItemBehavior itemBehavior : optionalItemBehaviors.get()) {
                InteractionResult result = itemBehavior.useOnBlock(new UseOnContext(player, hand, hitResult));
                if (result == InteractionResult.SUCCESS_AND_CANCEL) {
                    event.setCancelled(true);
                    return;
                }
                int maxY = player.world().worldHeight().getMaxBuildHeight() - 1;
                if (direction == Direction.UP
                        && result != InteractionResult.SUCCESS
                        && pos.y() >= maxY
                        && itemBehavior instanceof BlockItemBehavior
                ) {
                    player.sendActionBar(Component.translatable("build.tooHigh").arguments(Component.text(maxY)).color(NamedTextColor.RED));
                    return;
                }
                if (result != InteractionResult.PASS) {
                    return;
                }
            }
        }

        // it's a vanilla block
        // 这部分代码是处理放置原版方块“缺失的”声音和挥手动画
        if (itemInHand.isBlockItem() && !itemInHand.isCustomItem()) {
            // client won't have sounds if the fake block is interactable
            // so we should check and resend sounds on interact
            Object blockState = BlockStateUtils.blockDataToBlockState(clickedBlock.getBlockData());
            int stateId = BlockStateUtils.blockStateToId(blockState);
            ImmutableBlockState againstCustomBlock = BukkitBlockManager.instance().getImmutableBlockState(stateId);
            if (againstCustomBlock == null || againstCustomBlock.isEmpty()) {
                return;
            }

            BlockPos pos = LocationUtils.toBlockPos(clickedBlock.getLocation());
            Vec3d vec3d = new Vec3d(interactionPoint.getX(), interactionPoint.getY(), interactionPoint.getZ());
            Direction direction = DirectionUtils.toDirection(event.getBlockFace());
            BlockHitResult hitResult = new BlockHitResult(vec3d, direction, pos, false);
            try {
                BlockData craftBlockData = BlockStateUtils.fromBlockData(againstCustomBlock.vanillaBlockState().handle());
                if (InteractUtils.isInteractable(KeyUtils.namespacedKey2Key(craftBlockData.getMaterial().getKey()), bukkitPlayer, craftBlockData, hitResult, itemInHand)) {
                    if (!player.isSecondaryUseActive()) {
                        player.setResendSound();
                    }
                } else {
                    if (BlockStateUtils.isReplaceable(againstCustomBlock.customBlockState().handle()) && !BlockStateUtils.isReplaceable(againstCustomBlock.vanillaBlockState().handle())) {
                        player.setResendSwing();
                    }
                }
            } catch (ReflectiveOperationException e) {
                plugin.logger().warn("Failed to get CraftBlockData", e);
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
