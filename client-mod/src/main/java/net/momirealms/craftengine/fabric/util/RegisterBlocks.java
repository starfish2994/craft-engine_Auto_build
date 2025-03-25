package net.momirealms.craftengine.fabric.util;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.momirealms.craftengine.fabric.CraftEngineFabricMod;

import java.util.function.Function;

public class RegisterBlocks {
    @SuppressWarnings("UnusedReturnValue")
    public static Block register(String name, boolean canPassThrough, VoxelShape outlineShape) {
        AbstractBlock.Settings settings = Block.Settings.create().nonOpaque().strength(-1.0F, 3600000.0F);
        VoxelShape collisionShape;
        if (canPassThrough) {
            collisionShape = VoxelShapes.empty();
            settings.noCollision();
        } else {
            collisionShape = outlineShape;
        }
        return register(name, (settingsParam) -> new CustomBlock(settingsParam, outlineShape, collisionShape), settings);
    }

    public static Block register(String name, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings settings) {
        RegistryKey<Block> blockKey = keyOfBlock(name);
        Block block = blockFactory.apply(settings.registryKey(blockKey));

        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    private static RegistryKey<Block> keyOfBlock(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(CraftEngineFabricMod.MOD_ID, name));
    }

}

class CustomBlock extends Block {
    private final VoxelShape outlineShape;
    private final VoxelShape collisionShape;

    public CustomBlock(Settings settings, VoxelShape outlineShape, VoxelShape collisionShape) {
        super(settings);
        this.outlineShape = outlineShape;
        this.collisionShape = collisionShape;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.outlineShape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.collisionShape;
    }
}
