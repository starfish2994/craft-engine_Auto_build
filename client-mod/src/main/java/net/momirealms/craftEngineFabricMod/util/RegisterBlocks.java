package net.momirealms.craftEngineFabricMod.util;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.momirealms.craftEngineFabricMod.CraftEngineFabricMod;

import java.util.function.Function;

public class RegisterBlocks {
    public static Block register(String name, Block block) {
        return register(name, Block::new, Block.Settings.copy(block));
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
