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
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StackableBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final Property<Integer> amountProperty;
    private final Set<Key> items;
    private final SoundData soundData;

    public StackableBlockBehavior(CustomBlock block,Property<Integer> amountProperty, Set<Key> items, SoundData soundData) {
        super(block);
        this.amountProperty = amountProperty;
        this.items = items;
        this.soundData = soundData;
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
        if (this.items.contains(item.id()) && state.get(this.amountProperty) < this.amountProperty.possibleValues().getLast()) {
            ImmutableBlockState nextStage = state.cycle(this.amountProperty);
            World world = context.getLevel();
            BlockPos pos = context.getClickedPos();
            Location location = new Location((org.bukkit.World) world.platformWorld(), pos.x(), pos.y(), pos.z());
            if (CraftEngineBlocks.place(location, nextStage, UpdateOption.UPDATE_NONE, false)) {
                if (soundData != null) {
                    location.getWorld().playSound(location, soundData.id().toString(), SoundCategory.BLOCKS, soundData.volume(), soundData.pitch());
                }
                FastNMS.INSTANCE.method$ItemStack$consume(item.getLiteralObject(), 1, player.serverPlayer());
                player.swingHand(context.getHand());
            }
            return InteractionResult.SUCCESS_AND_CANCEL;
        }
        return InteractionResult.PASS;
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        @SuppressWarnings("unchecked")
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Property<Integer> amount = (Property<Integer>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("amount"), "warning.config.block.behavior.stackable.missing_amount");
            SoundData soundData = arguments.containsKey("sound") ? SoundData.create(arguments.get("sound"), 1f, 0.8f) : null;
            Set<Key> items = new HashSet<>();
            if (arguments.get("items") instanceof List<?> list) {
                for (Object obj : list) {
                    if (obj == null) continue;
                    items.add(Key.of(obj.toString()));
                }
            }
            return new StackableBlockBehavior(block, amount, items, soundData);
        }
    }
}
