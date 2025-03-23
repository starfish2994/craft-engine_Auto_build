package net.momirealms.craftEngineFabricMod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.momirealms.craftEngineFabricMod.util.RegisterBlocks;
import net.momirealms.craftEngineFabricMod.util.YamlUtils;

import java.util.HashMap;
import java.util.Map;

public class CraftEngineFabricMod implements ModInitializer {
    public static final String MOD_ID = "craftengine";

    @Override
    public void onInitialize() {
        Map<String, String> mappings = YamlUtils.loadConfig();
        Map<String, Integer> blockCount = new HashMap<>();
        mappings.keySet().forEach(name -> {
            String blockName = YamlUtils.split(name);
            if (blockName != null) {
                if (blockCount.containsKey(blockName)) {
                    blockCount.put(blockName, blockCount.get(blockName) + 1);
                } else {
                    blockCount.put(blockName, 0);
                }
                RegisterBlocks.register(
                        blockName + "_" + blockCount.get(blockName),
                        Registries.BLOCK.get(keyOfBlock(blockName))
                );
            }
        });
        mappings.clear();
        blockCount.clear();
    }

    private static RegistryKey<Block> keyOfBlock(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of("minecraft", name));
    }
}
