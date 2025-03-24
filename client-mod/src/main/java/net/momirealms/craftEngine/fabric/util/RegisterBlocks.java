package net.momirealms.craftEngine.fabric.util;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.momirealms.craftEngine.fabric.CraftEngineFabricMod;

import java.util.function.Function;

public class RegisterBlocks {
    @SuppressWarnings("UnusedReturnValue")
    public static Block register(String name, boolean canPassThrough) {
        AbstractBlock.Settings settings = Block.Settings.create().nonOpaque().strength(-1.0F, 3600000.0F);
        if (canPassThrough) settings.noCollision();
        return register(name, Block::new, settings);
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
