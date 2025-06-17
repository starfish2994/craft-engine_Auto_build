package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateOption;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StackableBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final Property<Integer> amountProperty;
    private final Set<Key> allowItems;

    public StackableBlockBehavior(CustomBlock block,Property<Integer> amountProperty, Set<Key> allowItems) {
        super(block);
        this.amountProperty = amountProperty;
        this.allowItems = allowItems;
    }

    @Override
    @SuppressWarnings("unchecked")
    public InteractionResult useOnBlock(UseOnContext context, ImmutableBlockState state) {
        Player player = context.getPlayer();
        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        Item<ItemStack> item = (Item<ItemStack>) context.getItem();
        if (item == null) {
            return InteractionResult.PASS;
        }
        if (this.allowItems.contains(item.id()) && state.get(this.amountProperty) < this.amountProperty.possibleValues().getLast()) {
            ImmutableBlockState nextStage = state.cycle(this.amountProperty);
            World world = context.getLevel();
            BlockPos pos = context.getClickedPos();
            Location location = new Location((org.bukkit.World) world.platformWorld(), pos.x(), pos.y(), pos.z());
            if (CraftEngineBlocks.place(location, nextStage, UpdateOption.UPDATE_NONE, true)) {
                FastNMS.INSTANCE.method$ItemStack$consume(item.getLiteralObject(), 1, player.serverPlayer());
                player.swingHand(context.getHand());
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        @SuppressWarnings("unchecked")
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Property<Integer> amount = (Property<Integer>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("amount"), "warning.config.block.behavior.stackable.missing_amount");
            Set<Key> allowItems = new HashSet<>();
            if (arguments.get("allow-items") instanceof List<?> list) {
                for (Object obj : list) {
                    if (obj == null) continue;
                    allowItems.add(Key.of(obj.toString()));
                }
            }
            return new StackableBlockBehavior(block, amount, allowItems);
        }
    }
}
