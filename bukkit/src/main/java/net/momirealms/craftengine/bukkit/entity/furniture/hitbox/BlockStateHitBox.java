package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.Material;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.entity.furniture.AbstractHitBox;
import net.momirealms.craftengine.core.entity.furniture.Collider;
import net.momirealms.craftengine.core.entity.furniture.HitBox;
import net.momirealms.craftengine.core.entity.furniture.HitBoxFactory;
import net.momirealms.craftengine.core.entity.furniture.HitBoxTypes;
import net.momirealms.craftengine.core.entity.furniture.Seat;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.LazyReference;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.core.world.collision.AABB;

public class BlockStateHitBox extends AbstractHitBox {
    public static final Factory FACTORY = new Factory();
    
    private final LazyReference<BlockStateWrapper> lazyBlockState;
    private final boolean dropContainer;
    private WorldPosition placedPosition;
    private BlockStateWrapper originalBlockState;

    public BlockStateHitBox(Seat[] seats, Vector3f position, LazyReference<BlockStateWrapper> lazyBlockState, 
                           boolean canUseOn, boolean blocksBuilding, boolean canBeHitByProjectile, boolean dropContainer) {
        super(seats, position, canUseOn, blocksBuilding, canBeHitByProjectile);
        this.lazyBlockState = lazyBlockState;
        this.dropContainer = dropContainer;
    }

    public LazyReference<BlockStateWrapper> blockState() {
        return lazyBlockState;
    }

    public boolean dropContainer() {
        return dropContainer;
    }

    @Override
    public Key type() {
        return HitBoxTypes.BLOCKSTATE;
    }

    @Override
    public void initPacketsAndColliders(int[] entityId, WorldPosition position, Quaternionf conjugated, 
                                       BiConsumer<Object, Boolean> packets, Consumer<Collider> collider, 
                                       BiConsumer<Integer, AABB> aabb) {
        Vector3f offset = conjugated.transform(new Vector3f(position()));
        World world = position.world();
        int blockX = (int) Math.floor(position.x() + offset.x);
        int blockY = (int) Math.floor(position.y() + offset.y);
        int blockZ = (int) Math.floor(position.z() - offset.z);
        
        // Store the placed position for later removal
        this.placedPosition = new WorldPosition(world, blockX, blockY, blockZ);
        
        // Store the original block state before placing our block
        try {
            // Get the bukkit block data from the world
            org.bukkit.World bukkitWorld = (org.bukkit.World) world.platformWorld();
            org.bukkit.block.data.BlockData blockData = bukkitWorld.getBlockAt(blockX, blockY, blockZ).getBlockData();
            this.originalBlockState = BlockStateUtils.toPackedBlockState(blockData);
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to get original block state", e);
            // Fallback to air
            this.originalBlockState = CraftEngine.instance().blockManager().createPackedBlockState("minecraft:air");
        }
        
        // Place the block
        BlockStateWrapper blockStateWrapper = lazyBlockState.get();
        if (blockStateWrapper != null) {
            world.setBlockAt(blockX, blockY, blockZ, blockStateWrapper, 3); // UPDATE_ALL flags
        }

        // If the block can be used on, add AABB for interaction
        if (canUseItemOn()) {
            aabb.accept(entityId[0], new AABB(blockX, blockY, blockZ, blockX + 1, blockY + 1, blockZ + 1));
        }
    }

    @Override
    public void initShapeForPlacement(double x, double y, double z, float yaw, Quaternionf conjugated, Consumer<AABB> aabbs) {
        if (blocksBuilding()) {
            Vector3f offset = conjugated.transform(new Vector3f(position()));
            int blockX = (int) Math.floor(x + offset.x);
            int blockY = (int) Math.floor(y + offset.y);
            int blockZ = (int) Math.floor(z - offset.z);
            aabbs.accept(new AABB(blockX, blockY, blockZ, blockX + 1, blockY + 1, blockZ + 1));
        }
    }

    @Override
    public int[] acquireEntityIds(Supplier<Integer> entityIdSupplier) {
        return new int[] {entityIdSupplier.get()};
    }

    /**
     * Removes the placed block and handles container drops if needed
     */
    public void removePlacedBlock() {
        if (placedPosition == null) return;
        
        World world = placedPosition.world();
        int x = (int) placedPosition.x();
        int y = (int) placedPosition.y();
        int z = (int) placedPosition.z();
        
        // Drop container contents if the flag is enabled
        if (dropContainer) {
            dropContainerContents(world, x, y, z);
        }
        
        // Restore the original block state
        if (originalBlockState != null) {
            world.setBlockAt(x, y, z, originalBlockState, 3);
        } else {
            // Fallback to air if no original state was stored
            BlockStateWrapper airState = CraftEngine.instance().blockManager().createPackedBlockState("minecraft:air");
            if (airState != null) {
                world.setBlockAt(x, y, z, airState, 3);
            }
        }
    }

    /**
     * Drops the contents of a container block
     */
    private void dropContainerContents(World world, int x, int y, int z) {
        try {
            // Get the bukkit world and block
            org.bukkit.World bukkitWorld = (org.bukkit.World) world.platformWorld();
            if (bukkitWorld == null) return;
            
            org.bukkit.block.Block block = bukkitWorld.getBlockAt(x, y, z);
            if (block.getState() instanceof InventoryHolder inventoryHolder) {
                org.bukkit.inventory.Inventory inventory = inventoryHolder.getInventory();
                org.bukkit.Location dropLocation = block.getLocation().add(0.5, 0.5, 0.5);
                
                // Drop all items in the inventory
                for (ItemStack itemStack : inventory.getContents()) {
                    if (itemStack != null && itemStack.getType() != Material.AIR) {
                        bukkitWorld.dropItemNaturally(dropLocation, itemStack);
                    }
                }
                
                // Clear the inventory
                inventory.clear();
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to drop container contents for BlockStateHitBox", e);
        }
    }

    public static class Factory implements HitBoxFactory {

        @Override
        public HitBox create(Map<String, Object> arguments) {
            Vector3f position = net.momirealms.craftengine.core.util.MiscUtils.getAsVector3f(
                arguments.getOrDefault("position", "0"), "position");
            
            String blockStateString = ResourceConfigUtils.requireNonEmptyStringOrThrow(
                arguments.get("block-state"), "warning.config.furniture.hitbox.blockstate.missing_block_state");
            
            boolean canUseOn = ResourceConfigUtils.getAsBoolean(
                arguments.getOrDefault("can-use-item-on", false), "can-use-item-on");
            boolean blocksBuilding = ResourceConfigUtils.getAsBoolean(
                arguments.getOrDefault("blocks-building", true), "blocks-building");
            boolean canBeHitByProjectile = ResourceConfigUtils.getAsBoolean(
                arguments.getOrDefault("can-be-hit-by-projectile", false), "can-be-hit-by-projectile");
            boolean dropContainer = ResourceConfigUtils.getAsBoolean(
                arguments.getOrDefault("drop-container", true), "drop-container");
            
            LazyReference<BlockStateWrapper> lazyBlockState = LazyReference.lazyReference(
                () -> CraftEngine.instance().blockManager().createPackedBlockState(blockStateString));
            
            return new BlockStateHitBox(
                HitBoxFactory.getSeats(arguments),
                position,
                lazyBlockState,
                canUseOn,
                blocksBuilding,
                canBeHitByProjectile,
                dropContainer
            );
        }
    }
}
